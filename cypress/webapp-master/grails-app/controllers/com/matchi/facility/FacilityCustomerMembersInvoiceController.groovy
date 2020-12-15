package com.matchi.facility

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.invoice.InvoiceRow
import com.matchi.membership.Membership
import com.matchi.orders.InvoiceOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import grails.validation.Validateable
import org.joda.time.LocalDate

class FacilityCustomerMembersInvoiceController extends GenericController {

    static scope = "prototype"

    def memberService
    def membersFamilyService
    def membershipPaymentService
    def invoiceService
    def customerService

    def createMembershipInvoiceFlow = {
        start {
            action { FilterCustomerCommand cmd ->
                Facility facility = getCurrentUser().facility
                flow.returnUrl = params.returnUrl
                log.debug("Return URL: ${flow.returlUrl}")
                flow.cmd = cmd
                flow.vats = getCountryVats(facility)

                // get all customers that are being exported
                if (params.allselected) {
                    flow.customerIds = customerService.findCustomers(
                            cmd, facility, false, true)
                } else {
                    flow.customerIds = params.list("customerId").collect { Long.parseLong(it) }
                }

                flow.persistenceContext.clear()
                success()
            }
            on("success").to("selectType")
            on("error").to("cancel")
        }
        selectType {
            on("next") {
                flow.createNewMembership = params.boolean("createNewMembership")
            }.to("entry")
            on("cancel").to("cancel")
        }
        entry {
            action {
                def facility = getCurrentUser().facility

                // memberships to create invoice for
                def memberships = []
                def membershipIds = []
                Customer.getAll(flow.customerIds).each {
                    def m = it?.getMembershipByFilter(flow.cmd)
                    if (m) {
                        memberships << m
                        membershipIds << m.id
                    }
                }

                if(!membershipIds) {
                    return error()
                }

                boolean memberWithoutType = !memberships.every { Membership m -> m?.type }
                if(memberWithoutType) {
                    session[FacilityCustomerMembersController.MEMBERSHIP_TYPE_REQUIRED] = true
                    return error()
                }

                // get all articles
                flow.articles = invoiceService.getItems(facility)
                flow.confirmInformation = []
                flow.cancelMemberships = 0
                flow.upcomingDifferentMemberships = 0
                flow.upcomingInvoicedMemberships = 0
                flow.invoicedMemberships = 0
                flow.paidMemberships = 0
                flow.freeMemberships = 0

                memberships.each { Membership m ->
                    if (!m.type.price) {
                        flow.freeMemberships++
                    } else if (flow.createNewMembership && m.cancel) {
                        flow.cancelMemberships++
                    } else if (flow.createNewMembership
                            && m.customer.memberships.find {it.startDate > m.endDate && it.type != m.type}) {
                        flow.upcomingDifferentMemberships++
                    } else if (flow.createNewMembership
                            && m.customer.memberships.find {it.startDate > m.endDate && it.order.isInvoiced()}) {
                        flow.upcomingInvoicedMemberships++
                    } else if (!flow.createNewMembership && m.order?.isInvoiced()) {
                        flow.invoicedMemberships++
                    } else if ((!flow.createNewMembership && m.isPaid())
                            || (flow.createNewMembership && m.customer.memberships.find {it.startDate > m.endDate && it.isPaid()})) {
                        flow.paidMemberships++
                    } else {
                        def price = m.getPrice(flow.createNewMembership)

                        def memberInfo = [
                            membershipId: m.id,
                            customerNr: m.customer.number,
                            customerName: m.customer.fullName(),
                            typeName: m.type,
                            price: price,
                            isFamilyContact: m.isFamilyContact()
                        ]

                        if (m.family && m.isFamilyContact()) {
                            memberInfo.familyMembers = m.family.members.findAll {
                                !flow.createNewMembership || (it.activated && !it.cancel)
                            }.collect {
                                [fullName: it.customer.fullName(), birthyear: it.customer.birthyear,
                                        type: it.type?.name, price: it.type?.price]
                            }
                            flow.confirmInformation << memberInfo
                        } else if (!m.family) {
                            flow.confirmInformation << memberInfo
                        }
                    }
                }

                //clear out members not to recieve an invoice (e.g familiymembers)
                flow.membershipIds = flow.confirmInformation.collect { it.membershipId }
                flow.availableOrganizations = Organization.findAllByFacility(facility)

                flow.persistenceContext.clear()
                success()
            }
            on("success").to("enterDetails")
            on("error").to("cancel")
        }
        enterDetails {
            on("back").to("selectType")
            on("next").to("processDetails")
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
            on("success").to "enterDetails"
        }
        processDetails {
            action { InvoiceMembershipCommand invoiceMembershipCommand ->
                flow.invoiceMembershipCommand = invoiceMembershipCommand
                def memberships = Membership.findAllByIdInList(flow.membershipIds)

                log.debug("Memberships ${memberships.size()}")

                memberships.each { Membership membership ->
                    def confirmInfo       = flow.confirmInformation.find { it.membershipId == membership.id }
                    confirmInfo.price     = flow.invoiceMembershipCommand.getPriceByMembershipId(membership.id);
                    confirmInfo.vatPercentage = invoiceMembershipCommand.vatPercentage
                    confirmInfo.discount  = invoiceMembershipCommand.discount
                    confirmInfo.discountType = invoiceMembershipCommand.discountType
                    confirmInfo.text      = invoiceMembershipCommand.text
                    confirmInfo.articleId = invoiceMembershipCommand.articleId
                    confirmInfo.account = invoiceMembershipCommand.account

                    if(confirmInfo?.price > 0) {
                        confirmInfo.total = confirmInfo?.price

                        if(confirmInfo?.discount > 0) {
                            confirmInfo.total -= confirmInfo.discountType == InvoiceRow.DiscountType.AMOUNT ?
                                    confirmInfo.discount : (confirmInfo.price * confirmInfo.discount / 100)
                        }
                    }

                    log.debug("Price: ${confirmInfo?.price}")
                    log.debug("Discount: ${confirmInfo?.discount}")
                    log.debug("Total: ${confirmInfo?.total}")
                }

                if(!invoiceMembershipCommand.validate()) {
                    error()
                } else {
                    flow.persistenceContext.clear()
                    success()
                }
            }
            on("success").to("confirm")
            on("error").to("enterDetails")
        }
        confirm {
            on("back").to("enterDetails")
            on("next").to("createInvoiceRows")
            on("cancel").to("cancel")
        }
        createInvoiceRows {
            action {
                log.debug("CreateInvoiceRows")
                flow.confirmInformation.each { def confirmInfo ->
                    def membership = Membership.get(confirmInfo.membershipId)
                    def issuer = getCurrentUser()
                    def invoicedMembership
                    if (flow.createNewMembership) {
                        invoicedMembership = membership.customer.memberships.find {
                            it.startDate > membership.endDate && !it.order?.isInvoiced()
                        }
                        if (!invoicedMembership) {
                            def today = new LocalDate()
                            def startDate = membership.endDate.plusDays(1)
                            invoicedMembership = memberService.addMembership(
                                    membership.customer, membership.type,
                                    (startDate < today && !membership.inGracePeriod) ? today : startDate)
                            if (!invoicedMembership) {
                                return
                            }

                            if (membership.isFamilyContact() && !invoicedMembership.family) {
                                def family = membersFamilyService.createFamily(invoicedMembership)
                                membership.family.membersNotContact.each { fm ->
                                    if (fm.activated && !fm.cancel) {
                                        def start = fm.endDate.plusDays(1)
                                        def m = memberService.addMembership(fm.customer,
                                                fm.type, start < today ? today : start,
                                                invoicedMembership.order)
                                        if (m) {
                                            membersFamilyService.addFamilyMember(m, family)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        invoicedMembership = membership
                    }

                    def description = "${confirmInfo.text}"

                    def invoiceRow = new InvoiceRow()
                    invoiceRow.customer = membership.customer
                    invoiceRow.price  = confirmInfo.price
                    invoiceRow.amount = 1
                    invoiceRow.account = confirmInfo.account
                    invoiceRow.discount = (confirmInfo.discount?:0)
                    invoiceRow.discountType = confirmInfo.discountType
                    invoiceRow.vat    = (confirmInfo.vatPercentage?:0)
                    invoiceRow.externalArticleId = confirmInfo.articleId
                    invoiceRow.description = description
                    invoiceRow.createdBy = issuer
                    invoiceRow.organization = flow.organization
                    invoiceRow.save(failOnError: true)

                    def payment = new InvoiceOrderPayment(issuer: issuer,
                            amount: invoiceRow.price, vat: invoiceRow.vat, invoiceRow: invoiceRow)
                            .save(failOnError: true)
                    invoicedMembership.order.addToPayments(payment)
                    invoicedMembership.order.save(failOnError: true)

                    if (invoicedMembership.isFamilyContact()) {
                        invoicedMembership.family.setSharedOrder(invoicedMembership.order)
                    }
                }
            }
            on("success").to("cancel")
            on("error").to("cancel")
        }
        cancel {
            if (flow.returnUrl) {
                redirect(url: flow.returnUrl)
                return
            }
            redirect(controller: "facilityCustomerMembers", action: "index")
        }
        done {
            if (flow.returnUrl) {
                redirect(url: flow.returnUrl)
                return
            }
            redirect(controller: "facilityCustomerMembers", action: "index")
        }
    }
}

@Validateable(nullable = true)
class InvoiceMembershipCommand implements Serializable {
    Long account
    String text
    Long vatPercentage
    Long discount
    InvoiceRow.DiscountType discountType
    Map<Long, String> pricePerMembership = [:]
    String articleId

    def getPriceByMembershipId(def membershipId) {
        try {
            Long.parseLong(pricePerMembership[membershipId].trim())
        } catch(NumberFormatException n) {
            return null
        }
    }

    static constraints = {
        text(blank: false)
        pricePerMembership(validator: {val, obj ->
            def result = true
            log.info(val)
            val.each {
                if(!it.value || !it.value.isNumber() || it.value == 0 || it.value == "0") {
                    log.info("${it} DID NOT passed validated")
                    result = "notValid"
                } else {
                    log.info("${it} passed validated")
                }
            }
            return result
        })
        discountType(nullable: false)
    }
}
