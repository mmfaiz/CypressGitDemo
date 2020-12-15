package com.matchi.facility

import com.matchi.GenericController
import com.matchi.PriceNotFoundException
import com.matchi.Subscription
import com.matchi.invoice.InvoiceRow
import grails.validation.Validateable
import org.apache.commons.lang.StringUtils
import org.springframework.validation.Errors

class FacilitySubscriptionInvoiceController extends GenericController {

    static scope = "prototype"

    def priceListService
    def subscriptionService
    def invoiceService

    def createSubscriptionInvoiceFlow = {

        start {
            action { FacilitySubscriptionFilterCommand cmd ->
                def facility = getCurrentUser().facility
                flow.facility = facility
                flow.returnUrl = params.returnUrl

                // get all articles
                flow.articles = invoiceService.getItems(facility)

                // get all subscriptions that are being invoiced
                if (params.allselected) {
                    def subscriptionsResult = subscriptionService.getSubscriptions(facility, cmd)
                    flow.subscriptionsIds = subscriptionsResult?.rows?.collect { it?.id }
                } else {
                    flow.subscriptionsIds = params.list("subscriptionId").collect { Long.parseLong(it) }
                }

                if(flow.subscriptionsIds.isEmpty()) {
                    return error()
                }

                try {
                    flow.confirmInformation = []
                    flow.rejectedSubscriptions = []

                    flow.subscriptionsIds.each { Long subscriptionId ->
                        try {
                            Subscription subscription = Subscription.read(subscriptionId)
                            def priceList = priceListService.getActivePriceList(
                                    subscription.firstSlot(), true)

                            if (!priceList) {
                                flow.missingSportPriceLists = [subscription.firstSlot().court.sport]
                                return
                            }

                            def account = priceList.getBookingPrice(
                                    subscription.customer, subscription.firstSlot())?.account
                            def price = subscription.getPrice(priceList)

                            def confirmationInfo = [
                                    subscriptionId: subscription.id,
                                    customerName: subscription.customer.fullName(),
                                    numSlots: subscription.slots?.size(),
                                    price: price,
                                    priceListName: priceList.name,
                                    priceListPrice: price,
                                    account: account
                            ]

                            if(price > 0) {
                                flow.confirmInformation << confirmationInfo
                            } else {
                                flow.rejectedSubscriptions << confirmationInfo
                            }
                        } catch (ex) {
                            log.error "Error during preocessing data for subscription (${subscriptionId}): $ex.message", ex
                        }
                    }

                    if (flow.missingSportPriceLists) {
                        return errorNoPriceLists()
                    }

                    flow.availableOrganizations = Organization.findAllByFacility(facility)

                } catch(PriceNotFoundException pnfe) {
                    flow.exception = pnfe
                    log.error(pnfe)
                    return errorNoPriceLists()

                } finally {
                    flow.persistenceContext.clear()
                }

                enterSubscriptionDetails()
            }
            on("enterSubscriptionDetails").to("enterSubscriptionDetails")
            on("errorNoPriceLists").to("errorNoPriceLists")
            on("error").to("cancel")
        }

        errorNoPriceLists {
            on("cancelToPriceLists").to("cancelToPriceLists")
            on("cancelToPriceListsCreate").to("cancelToPriceListsCreate")
            on("cancel").to("cancel")
        }

        enterSubscriptionDetails {
            on("next").to("processPriceLists")
            on("addOrganization").to("addOrganization")
            on("cancel").to("cancel")
        }

        addOrganization {
            action {
                flow.organization = params.organizationId && params.organizationId != "null" ? Organization.get(params.organizationId) : null
                if (flow.organization?.fortnoxAccessToken) {
                    flow.articles = invoiceService.getItemsForOrganization(flow.organization.id)
                }
            }
            on("success").to "enterSubscriptionDetails"
        }

        processPriceLists {

            action { InvoiceSubscriptionCommand invoiceSubscriptionCommand ->

                flow.invoiceSubscriptionCommand = invoiceSubscriptionCommand
                flow.confirmInformation.each { def confirmInfo ->

                    confirmInfo.price    = flow.invoiceSubscriptionCommand.getPriceBySubscriptionId(confirmInfo.subscriptionId);
                    confirmInfo.vatPercentage = invoiceSubscriptionCommand.vatPercentage
                    confirmInfo.discount = invoiceSubscriptionCommand.discount
                    confirmInfo.discountType = invoiceSubscriptionCommand.discountType
                    confirmInfo.text     = invoiceSubscriptionCommand.text
                    confirmInfo.articleId = invoiceSubscriptionCommand.articleId

                    if(confirmInfo?.price > 0) {
                        confirmInfo.total = confirmInfo?.price

                        if(confirmInfo?.discount > 0) {
                            confirmInfo.total -= confirmInfo.discountType == InvoiceRow.DiscountType.AMOUNT ?
                                    confirmInfo.discount : (confirmInfo.price * confirmInfo.discount / 100)
                        }
                    }
                }

                if(!invoiceSubscriptionCommand.validate()) {
                    error()
                } else {

                    flow.persistenceContext.clear()
                    success()
                }
            }

            on("error").to("enterSubscriptionDetails")
            on("success").to("confirmSubscriptions")
        }

        confirmSubscriptions() {
            on("error").to("confirmPriceLists")
            on("next").to("createInvoiceRows")
            on("back").to("enterSubscriptionDetails")
            on("cancel").to("cancel")
        }

        createInvoiceRows() {
            action {

                try {
                    Subscription.withTransaction {
                        flow.confirmInformation.each { def confirmInfo ->
                            def subscription = Subscription.get(confirmInfo.subscriptionId)
                            def description = "${confirmInfo.text}"

                            def invoiceRow = new InvoiceRow()
                            invoiceRow.customer = subscription.customer
                            invoiceRow.price = confirmInfo.price
                            invoiceRow.amount = 1
                            invoiceRow.discount = (confirmInfo.discount ?: 0)
                            invoiceRow.discountType = confirmInfo.discountType
                            invoiceRow.vat = (confirmInfo.vatPercentage ?: 0)
                            invoiceRow.description = description
                            if (flow.invoiceSubscriptionCommand.addAppendix) {
                                invoiceRow.description += addMetaDescription(subscription)
                            }
                            invoiceRow.description = StringUtils.abbreviate(invoiceRow.description, InvoiceRow.DESCRIPTION_MAX_SIZE)
                            invoiceRow.createdBy = getCurrentUser()
                            invoiceRow.externalArticleId = confirmInfo.articleId
                            invoiceRow.account = confirmInfo.account
                            invoiceRow.organization = flow.organization
                            invoiceRow.save(failOnError: true)

                            subscription.invoiceRow = invoiceRow
                            subscription.save(failOnError: true)
                        }
                    }
                } catch (ex) {
                    def errorField = ((Errors) ex.errors).allErrors?.getAt(0)
                    def errorMessage = ex.message
                    if (errorField) {
                        errorMessage = g.message(code: errorField.code, args: errorField.arguments, default: errorField.defaultMessage)
                    }
                    log.error "Error during creationg of Subscription Invoice: ${errorMessage}", ex
                    flow.flowException = errorMessage
                    error()
                }
            }
            on("error").to("enterSubscriptionDetails")
            on("success").to "done"
        }

        cancel {
            if (flow.returnUrl) {
                redirect(url: flow.returnUrl)
                return
            }
            redirect(controller: "facilitySubscription", action: "index")
        }

        cancelToPriceListsCreate {
            redirect(controller: "facilityPriceList", action: "create", params: ["subscriptions": Boolean.TRUE])
        }

        cancelToPriceLists {
            redirect(controller: "facilityPriceList", action: "index")
        }

        done {
            if (flow.returnUrl) {
                redirect(url: flow.returnUrl)
                return
            }
            redirect(controller: "facilitySubscription", action: "index")
        }

    }

    private def addMetaDescription(Subscription subscription) {

        def weekDays =  [ "Mån", "Tis", "Ons", "Tors", "Fre", "Lör", "Sön" ];

        def sb = new StringBuilder()
        .append(": ")
        .append(weekDays[subscription.weekday - 1]).append(" ")
        .append(subscription.time.toString("HH:mm")).append(" ")
        .append(subscription.court.name).append(" ")
        .append(subscription?.slots?.size()).append("tim")

        return sb.toString()
    }

    private def missingSportPriceLists(def pricelists, def sports) {
        def missingPriceListsForSport = []

        sports.each { def sport ->
            def sportPriceList = pricelists.findAll { it.sport.id.equals(sport.id) }
            if(sportPriceList.isEmpty()) {
                missingPriceListsForSport << sport
            }
        }

        missingPriceListsForSport
    }

}

@Validateable(nullable = true)
class InvoiceSubscriptionCommand implements Serializable {
    String text
    Long vatPercentage
    Long discount
    InvoiceRow.DiscountType discountType
    String articleId
    Map<Long, String> pricePerSubscription = [:]
    Map<Long, String> accountPerSubscription = [:]
    Boolean addAppendix

    def getPriceBySubscriptionId(def subscriptionId) {
        try {
            Long.parseLong(pricePerSubscription[subscriptionId].trim())
        } catch(NumberFormatException n) {
            return null
        }
    }

    static constraints = {
        text(blank: false, maxSize: 50)
        articleId(blank: true, nullable: true)
        pricePerSubscription(validator: {val, obj ->
            def result = true
            log.info(val)
            val.each {
                if(!it.value || !it.value.isNumber() || it.value == 0) {
                    result = "notValid"
                }
            }
            return result
        })
        discountType(nullable: false)
    }
}
