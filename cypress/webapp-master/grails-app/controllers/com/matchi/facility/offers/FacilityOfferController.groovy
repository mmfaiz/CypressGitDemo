package com.matchi.facility.offers


import com.matchi.Customer
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.User
import com.matchi.conditions.DateSlotCondition
import com.matchi.conditions.SlotConditionSet
import com.matchi.coupon.Coupon
import com.matchi.coupon.CouponConditionGroup
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerCouponTicket
import com.matchi.coupon.GiftCard
import com.matchi.coupon.Offer
import com.matchi.facility.SaveRuleCommand
import grails.validation.Validateable
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.LocalDate

class FacilityOfferController extends GenericController {

    static def CONDITIONS_KEY = "com.matchi.coupons.slots.conditions_key"
    def couponService
    def customerService
    def facilityService

    def index(String type) {
        def facility = getUserFacility()

        if (!params.sort) {
            params.sort = "name"
            params.order = "asc"
        }

        def coupons = specifyOfferClass(type).findAllByFacilityAndArchived(facility, false, params)

        List<Facility> globalFacilities = facilityService.getAllHierarchicalFacilities(facility, false)
        List<Offer> globalCoupons = globalFacilities.collect {fac -> specifyOfferClass(type).findAllByFacilityAndArchived(fac, false, params)}.flatten()

        [ facility:facility, coupons:coupons, globalCoupons: globalCoupons ]
    }

    def archive(String type) {
        def facility = getUserFacility()

        if (!params.sort) {
            params.sort = "name"
            params.order = "asc"
        }

        def coupons = specifyOfferClass(type).findAllByFacilityAndArchived(facility, true, params)

        [facility: facility, coupons: coupons]
    }
    def edit(String type) {
        def facility = getUserFacility()
        def coupon = couponService.findCouponByTypeAndId(type, params.id)

        if (coupon) {
            assertFacilityAccessTo(coupon)
        }

        [ facility: facility, coupon: coupon ]
    }

    def add() {
        [ facility: getUserFacility() ]
    }

    def sold(String type) {
        def coupon = couponService.findCouponByTypeAndId(type, params.id)
        def customer

        if(params.customerId) {
            customer = User.get(params.customerId)
        }


        if (coupon) {
            assertFacilityAccessTo(coupon)
        }

        def customerCoupons = CustomerCoupon.findAllByCoupon(coupon, params)

        def active    = customerCoupons.findAll { it.isValid() }
        def locked    = customerCoupons.findAll { it.dateLocked }
        def archive   = customerCoupons - locked - active

        [ facility: getUserFacility(), coupon: coupon,
          active:active, locked:locked,  archive:archive, customerCoupons:customerCoupons , customer: customer ]
    }

    def update(CreateFacilityOfferCommand cmd, String type) {
        def facility = getUserFacility()

        if (cmd.hasErrors()) {
            render(view: 'edit', model: [ facility: facility, cmd:cmd] )
            return
        }

        Offer offer = couponService.findCouponByTypeAndId(type, cmd.couponId)

        if(offer) {
            assertFacilityAccessTo(offer)
            offer.name                  = cmd.name
            offer.description           = cmd.description
            offer.nrOfDaysValid         = cmd.nrOfDaysValid
            offer.endDate               = cmd.endDate
            offer.nrOfTickets           = cmd.unlimited ? 0 : cmd.nrOfTickets
            offer.unlimited             = cmd.unlimited
            offer.availableOnline       = cmd.availableOnline
            if (cmd.type == CreateFacilityOfferCommand.Type.Coupon.name()) {
                offer.conditionPeriod = cmd.conditionPeriod
                offer.nrOfBookingsInPeriod = cmd.nrOfBookingsInPeriod
                offer.nrOfPeriods = cmd.nrOfPeriods
                offer.totalBookingsInPeriod = cmd.totalBookingsInPeriod
            }
        }

        if (!offer.hasErrors() && offer.save(failOnError: true)) {
            flash.message = message(code: "facilityCoupon.update.success", args: [offer.name])
            redirect(action: "edit", id: offer.id, mapping: params.type)
            return
        }

        render(view: 'edit', params:[ cmd: cmd ] )
    }

    def save(CreateFacilityOfferCommand cmd) {
        def facility = getUserFacility()

        if (cmd.hasErrors()) {
            render(view: 'add', model: [ facility: facility, cmd:cmd] )
            return
        }


        Offer offer = new GiftCard()
        if (cmd.type == CreateFacilityOfferCommand.Type.Coupon.name()) {
            offer = new Coupon()
            offer.conditionPeriod = cmd.conditionPeriod
            offer.nrOfBookingsInPeriod = cmd.nrOfBookingsInPeriod
            offer.nrOfPeriods = cmd.nrOfPeriods
            offer.totalBookingsInPeriod = cmd.totalBookingsInPeriod
        }
        offer.name = cmd.name
        offer.description = cmd.description
        offer.nrOfDaysValid = cmd.nrOfDaysValid
        offer.endDate = cmd.endDate
        offer.nrOfTickets = cmd.unlimited ? 0 : cmd.nrOfTickets
        offer.unlimited = cmd.unlimited
        offer.availableOnline = cmd.availableOnline
        offer.facility = facility

        if (offer.save()) {
            flash.message = message(code: "facilityCoupon.save.success", args: [offer.name])
            redirect(action: "index", mapping: params.type)
            return
        }

        render(view: 'add', model: [ facility: facility, cmd:cmd] )
    }

    def addToCustomer(FacilityOfferAddToCustomerCommand cmd) {
        def customer = Customer.get(cmd.customerId)
        def coupon = Offer.get(cmd.couponId)
        def user = customer.user
        def facility = coupon.facility
        if (customer.facility != facility) {
            customer = customerService.getOrCreateUserCustomer(user, facility)
        }

        if (coupon) {
            if (customer?.facility?.hasLinkedFacilities()) {
                assertHierarchicalFacilityAccessTo(customer)
            } else {
                assertFacilityAccessTo(coupon)
            }
        }

        CustomerCoupon.link(getCurrentUser(), customer, coupon, cmd.nrOfTickets, new LocalDate(cmd.expireDate), cmd.note)
        flash.message = message(code: "facilityCoupon.addToCustomer.success", args: [coupon.name, customer.fullName()])

        redirect(controller: "facilityCustomer", action: "show", id: cmd.customerId)
    }

    def removeFromCustomer() {
        def customerCoupon = CustomerCoupon.get(params.id)
        def noneValidTickets = customerCoupon.couponTickets.findAll { it.nrOfTickets<0 }

        if (noneValidTickets.size() > 0) {
            flash.error = message(code: "facilityCoupon.removeFromCustomer.error")
            redirect(action: "sold", id: customerCoupon.coupon.id, mapping: params.type)
            return
        }

        customerCoupon.delete()
        flash.message = message(code: "facilityCoupon.removeFromCustomer.success")
        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "sold", id: customerCoupon.coupon.id, mapping: params.type)
        }
    }

    def addCouponForm(String type) {
        def customer = Customer.createCriteria().get {
            createAlias("facility", "f", CriteriaSpecification.LEFT_JOIN)

            eq("id", params.long("customerId"))
        } as Customer

        def coupons = [:]
        Offer.facilityCoupons(customer.facility).listOrderByName().each {
            if (!coupons[it.class]) {
                coupons[it.class] = []
            }
            coupons[it.class] << it
        }

        [ customer: customer, coupons: coupons]
    }

    def editCouponForm() {
        def customerCoupon = CustomerCoupon.get(params.id)

        [ customerCoupon: customerCoupon, customerId: params.customerId ]
    }

    def updateCustomerCoupon() {
        log.debug("UPDATE: ${params}")

        CustomerCoupon customerCoupon = CustomerCoupon.get(params.id)

        customerCoupon.note = params.note

        if (params.expireDate) {
            def newExpireDate = new LocalDate(params.expireDate)
            def oldExpireDate = customerCoupon.expireDate
            if (oldExpireDate != newExpireDate) {
                log.info("CustomerCoupon ${customerCoupon?.id} \"${customerCoupon?.coupon?.name}\" for customer ${customerCoupon?.customer?.id} \"${customerCoupon?.customer}\": Admin updated expiration date from ${oldExpireDate.toString()} to ${newExpireDate.toString()}")
                customerCoupon.expireDate = newExpireDate
            }
        }

        def ticketsDiff
        if (params.nrOfTickets && params.int("nrOfTickets") != customerCoupon.nrOfTickets) {
            log.info("CustomerCoupon ${customerCoupon?.id} \"${customerCoupon?.coupon?.name}\" for customer ${customerCoupon?.customer?.id} \"${customerCoupon?.customer}\": Admin updated nrOfTickets from ${customerCoupon.nrOfTickets} to ${params.int("nrOfTickets")}")
            ticketsDiff = params.int("nrOfTickets") - customerCoupon.nrOfTickets
            customerCoupon.nrOfTickets = params.int("nrOfTickets")
        }
        if (ticketsDiff) {
            customerCoupon.addToCouponTickets(new CustomerCouponTicket(issuer: getCurrentUser(),
                    type: CustomerCouponTicket.Type.ADMIN_CHANGE, nrOfTickets: ticketsDiff))
        }

        customerCoupon.save()

        flash.message = message(code: "facilityCoupon.updateCustomerCoupon.success")
        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "sold", id: customerCoupon?.coupon?.id, mapping: params.type)
        }
    }

    def lockCustomerCoupon() {
        def customerCoupon = CustomerCoupon.get(params.id)
        customerCoupon.note = params.note
        customerCoupon.dateLocked = new Date()
        customerCoupon.save()

        log.info("CustomerCoupon ${customerCoupon?.id} \"${customerCoupon?.coupon?.name}\" for customer ${customerCoupon?.customer?.id} \"${customerCoupon?.customer}\": Locked")

        flash.message = message(code: "facilityCoupon.lockCustomerCoupon.success")
        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "sold", id: customerCoupon?.coupon?.id, mapping: params.type)
        }
    }

    def unlockCustomerCoupon() {
        def customerCoupon = CustomerCoupon.get(params.id)
        customerCoupon.note = params.note
        customerCoupon.dateLocked = null
        customerCoupon.save()

        log.info("CustomerCoupon ${customerCoupon?.id} \"${customerCoupon?.coupon?.name}\" for customer ${customerCoupon?.customer?.id} \"${customerCoupon?.customer}\": Unlocked")

        flash.message = message(code: "facilityCoupon.unlockCustomerCoupon.success")
        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "sold", id: customerCoupon?.coupon?.id, mapping: params.type )
        }
    }

    def addCouponTicket() {
        def customerCoupon = CustomerCoupon.get(params.id)
        customerCoupon.addTicket(getCurrentUser())

        flash.message = message(code: "facilityCoupon.addCouponTicket.success",
                args: [customerCoupon?.coupon?.name, customerCoupon.customer.fullName()])
        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "sold", id: customerCoupon?.coupon?.id, mapping: params.type )
        }
    }

    def removeCouponTicket() {
        def customerCoupon = CustomerCoupon.get(params.id)
        if(customerCoupon.removeTicket(getCurrentUser())) {
            flash.message = message(code: "facilityCoupon.removeCouponTicket.success",
                    args: [customerCoupon?.coupon?.name, customerCoupon.customer.fullName()])
        } else {
            flash.message = message(code: "facilityCoupon.removeCouponTicket.noCoupons")
        }

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "sold", id: customerCoupon?.coupon?.id, mapping: params.type )
        }
    }

    def delete(String type) {
        Offer offer = couponService.findCouponByTypeAndId(type, params.couponId)

        if (offer) {
            assertFacilityAccessTo(offer)
            offer.delete()
            flash.message = message(code: "facilityCoupon.delete.success", args: [offer.name])
        }

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "index", mapping: params.type)
        }
    }

    def rules(String type) {
        Coupon coupon = couponService.findCouponByTypeAndId(type, params.id)

        if(!coupon) {
            flash.error = message(code: "facilityCoupon.rules.error")
            redirect(action: "index", mapping: params.type)
            return
        } else {
            assertFacilityAccessTo(coupon)
        }

        render(view: "rules", model: [coupon: coupon])

    }

    def ruleForm(String type) {
        def coupon = couponService.findCouponByTypeAndId(type, params.id)
        def group = new CouponConditionGroup()

        if(params.groupId) {
            group = CouponConditionGroup.get(params.groupId)
        }
        def availableConditions = [new DateSlotCondition()]

        CouponConditionGroup unsaved = getUnsaved()

        log.info("${unsaved.slotConditionSets.size()}")

        [coupon: coupon , facility: getUserFacility(), availableConditions: availableConditions,
         conditionGroup: unsaved, group: group]
    }

    def saveRule(SaveRuleCommand cmd) {

        Offer coupon = Offer.get(params.id)

        def unsaved = getUnsaved()

        CouponConditionGroup group = new CouponConditionGroup(name: cmd.name)

        unsaved.slotConditionSets.each {
            group.addToSlotConditionSets(it)
        }

        coupon.addToCouponConditionGroups(group)
        coupon.save()

        redirect(action:  "rules", id:  params.id, mapping: params.type)
    }

    def removeRule() {
        CouponConditionGroup group = CouponConditionGroup.get(params.groupId)

        if (group) {
            group.delete()
            flash.message = message(code: "facilityCoupon.removeRule.success", args: [group.name])
        }

        redirect(action:  "rules", id:  params.id, mapping: params.type)
    }

    def removeRuleSet() {
        def rule = getUnsaved().slotConditionSets.find { it.hashCode() == params.getInt("ruleId")}
        def unsaved = getUnsaved();


        log.info "Search for ${params.int("ruleId")}"

        rule.each {
            log.info("Removing ${rule}")
            unsaved.slotConditionSets.remove(rule)
        }

        flash.message = message(code: "facilityCoupon.removeRuleSet.success", args: [rule])

        //clearUnsaved()

        redirect(action: "ruleForm", id: params.id, params: [name: params.name], mapping: params.type)
    }

    def saveRuleSet() {

        SlotConditionSet conditionSet = new SlotConditionSet()
        addConditionsToSet(conditionSet)
        addUnsaved(conditionSet)

        log.info("Got rulename ${params.name}")

        flash.message = message(code: "facilityCoupon.saveRuleSet.success")

        redirect(action: "ruleForm", id: params.id, params: [name: params.name], mapping: params.type)
    }

    private void addConditionsToSet(SlotConditionSet conditionSet) {
        if(params.startDate && params.endDate) {
            conditionSet.addToSlotConditions(new DateSlotCondition(startDate: new LocalDate(params.startDate), endDate: new LocalDate(params.endDate)))
        }
    }

    private def addUnsaved(SlotConditionSet conditionSet) {
        def unsaved = getUnsaved()
        unsaved.slotConditionSets << conditionSet
        session.putValue(CONDITIONS_KEY, unsaved)
    }

    private def getUnsaved() {
        def unsavedGroup = session.getAttribute(CONDITIONS_KEY)
        if (!unsavedGroup) {
            unsavedGroup = new CouponConditionGroup()
            unsavedGroup.slotConditionSets = []
            session.putValue(CONDITIONS_KEY, unsavedGroup)
        }
        return unsavedGroup
    }

    private def clearUnsaved() {
        session.removeAttribute(CONDITIONS_KEY)
    }

    def putToArchive(String type) {
        Offer coupon = specifyOfferClass(type).findByIdAndFacility(params.long('id'), getUserFacility())
        if (coupon) {
            coupon.archived = true
            if (coupon.save()) {
                flash.message = message(code: 'coupon.to.archive.success', args: [coupon?.id])
                redirect(action: 'index', mapping: params.type)
            } else {
                flash.error = message(code: 'coupon.to.archive.error', args: [coupon?.errors])
                redirect(action: 'edit', id: coupon?.id, mapping: params.type)
            }
        } else {
            redirect(action: 'index', mapping: params.type)
        }
    }

    def getFromArchive() {
        if (params.couponsId) {
            def couponsIds = params.list("couponsId").collect { Long.parseLong(it) }
            if (couponsIds) {
                couponsIds.each { Long id->
                    Offer coupon = Offer.get(id)
                    coupon.archived = false
                    coupon.save()
                }
                flash.message = message(code: 'coupon.get.from.archive.success', args: [couponsIds.size()])
                redirect action: 'index', mapping: params.type
            } else {
                flash.error = message(code: 'coupon.get.from.archive.failure')
                redirect action: 'archive', mapping: params.type
            }
        } else {
            flash.eroor = message(code: 'coupon.get.from.archive.null.value')
            redirect action: 'archive', mapping: params.type
        }
    }

    def showUsageHistory(Long id) {
        def customerCoupon = CustomerCoupon.get(id)
        if (!customerCoupon) {
            render status: 404
            return
        }

        if (customerCoupon.customer?.facility?.hasLinkedFacilities()) {
            assertHierarchicalFacilityAccessTo(customerCoupon.customer)
        } else {
            assertFacilityAccessTo(customerCoupon, customerCoupon.customer.facility)
        }

        render template: "/templates/customer/customerOfferHistoryPopup",
                model: [tickets: CustomerCouponTicket.findAllByCustomerCouponAndNrOfTicketsIsNotNull(
                        customerCoupon, [sort: "dateCreated"])]
    }
}

@Validateable(nullable = true)
class CreateFacilityOfferCommand {
    Long couponId
    String name
    String description
    Integer nrOfDaysValid
    Date endDate
    Integer nrOfTickets
    boolean availableOnline = false
    boolean unlimited = false
    Coupon.ConditionPeriod conditionPeriod
    Integer nrOfPeriods
    Integer nrOfBookingsInPeriod
    boolean totalBookingsInPeriod = false
    String type = Type.Coupon.name()

    static constraints = {
        name(nullable: false, blank: false)
        description(nullable: true, blank: true, maxSize: 1000)
        nrOfDaysValid(nullable: true, blank: true)
        //min: 1,
        nrOfTickets(nullable: true, blank: true, validator: { nrOfTickets, obj ->
            if(obj.unlimited) { return true }
            nrOfTickets < 1 ? "createFacilityCouponCommand.nrOfTickets.min.error" : true
        })
        conditionPeriod(nullable: true, validator: { conditionPeriod, obj ->
            if (obj.type == Type.Coupon.name() && !conditionPeriod) {
                return ['invalid.conditionPeriod.someEmpty']
            }
            if (obj.nrOfPeriods || obj.nrOfBookingsInPeriod) {
                !conditionPeriod || !obj.nrOfPeriods || !obj.nrOfBookingsInPeriod ? ['invalid.conditionPeriod.someEmpty'] : true
            } else {
                return true
            }
        })
    }
    enum Type {
        Coupon, GiftCard, PromoCode
    }
}

@Validateable(nullable = true)
class FacilityOfferAddToCustomerCommand implements Serializable {
    Long couponId
    Long customerId
    Date expireDate
    Integer nrOfTickets = 0
    String note

    static constraints = {
        couponId(blank: false, nullable: false)
        customerId(blank: false, nullable: false)
        expireDate(nullable: true);
        nrOfTickets(blank: false, nullable: false, min: 0)
        note(nullable: true, blank: true)
    }
}
