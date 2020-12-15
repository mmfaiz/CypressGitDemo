package com.matchi.api

import com.matchi.*
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import com.matchi.activities.Participation
import com.matchi.coupon.GiftCard
import com.matchi.orders.Order
import com.matchi.payment.PaymentMethod
import grails.converters.JSON
import grails.validation.Validateable
import org.apache.http.HttpStatus
import org.joda.time.DateTime
import org.joda.time.LocalDate

class ClassActivitySearchResourceController extends GenericAPIController {

    ActivityService activityService

    def createActivityBooking() {
        def cmd = new BookingActivityCommand(request?.JSON)
        cmd.payment = new PaymentCommand(request?.JSON?.payment ?: [:])

        if (!cmd.payment.validate()) {
            error(400, Code.INPUT_ERROR, "Invalid payment information")
        } else {
            try {
                def user = getCurrentUser()
                def occasion = ActivityOccasion.get(cmd.activityOccasionId)
                if (!occasion) {
                    error(400, Code.RESOURCE_NOT_FOUND, "Could not locate activity (${cmd.activityOccasionId})")
                    return
                }
                if (occasion.isParticipating(user)) {
                    error(HttpStatus.SC_CONFLICT, Code.INPUT_ERROR, "You have been assigned to this activity already")
                    return
                }
                if (occasion.activity instanceof ClassActivity && ((ClassActivity) occasion.activity).terms && !cmd.acceptTerms) {
                    error(400, Code.INPUT_ERROR, "You must accept terms of use")
                    return
                }
                if (occasion.isFull()) {
                    error(400, Code.INPUT_ERROR, "This activity is full")
                    return
                } else {
                    ActivityOccasion bookingActivity = bookActivityAndHandlePayment(user, occasion, cmd)
                    render bookingActivity as JSON
                }
            } catch (APIException apiException) {
                error(apiException.status, apiException.errorCode, apiException.userMessage)
            }
        }
    }

    private def bookActivityAndHandlePayment(def user, def occasion, def cmd) {
        def order
        try {
            order = activityService.createActivityPaymentOrder(user, occasion, Order.ORIGIN_API)
            order.assertCustomer()
            if (cmd.userMessage) {
                order.metadata[Order.META_USER_MESSAGE] = cmd.userMessage
                order.save()
            }

            pay([order].toArray(), cmd.payment)

            activityService.book(order)

        } catch (Throwable t) {
            log.error("Error while processing activity booking order", t)

            Order.withTransaction {
                order.refund("Unable to process activity booking: ${t.message}")
            }
            throw new APIException(400, Code.UNKNOWN_ERROR, "Could not process activity booking")
        }
        return ActivityOccasion.get(order.metadata?.activityOccasionId)
    }

    boolean isActivityOccasionBookableForUser(ActivityOccasion occasion, Customer customer) {
        if (isUserParticipating(occasion, customer)) {
            return false
        }
        if (occasion.membersOnly) {
            if (!customer || !customer.hasMembership()) {
                return false
            }
        }
        return occasion.isUpcomingOnlineOccasion() && !occasion.isFull()
    }

    boolean isUserParticipating(ActivityOccasion occasion, Customer customer) {
        if (!customer) {
            return false
        }

        return occasion.participations.collect { it.customer }.contains(customer)
    }

    ActivityOccasionResponse fromActivityOccasion(User user, ActivityOccasion occasion) {
        // TODO Perhaps cache this as well...
        Customer customer = Customer.findByUserAndFacility(user, occasion.activity.facility)


        def res = new ActivityOccasionResponse()
        res.id = occasion.id
        res.activityId = occasion.activityId ?: occasion.activity.id
        res.message = occasion.message
        res.basePrice = occasion.price
        res.currency = occasion.activity.facility.currency
        res.courtId = occasion.courtId
        res.startTime = occasion.startDateTime
        res.endTime = occasion.endDateTime
        res.participants = occasion.participations.size()
        res.maxNumParticipants = occasion.maxNumParticipants
        res.minNumParticipants = occasion.minNumParticipants
        res.membersOnly = occasion.membersOnly
        res.automaticCancellationDateTime = occasion.automaticCancellationDateTime


        res.isBookable = isActivityOccasionBookableForUser(occasion, customer)
        res.isParticipating = isUserParticipating(occasion, customer)
        res.canBeCancelledByUser = occasion.activity.cancelByUser
        res.hasNotClosedRegistration = occasion.hasNotClosedRegistration()
        res.hasOpenedRegistration = occasion.hasOpenedRegistration()
        res.refundPolicy = new RefundPolicyResponse()
        res.refundPolicy.code = "cancellation.up.to.hours.before"
        res.refundPolicy.args = [occasion.activity.cancelLimitWithFallback]
        res.refundableServiceFee = activityService.getServiceFeeWithCurrency(occasion)

        return res
    }

    ClassActivityResponse fromClassActivity(ClassActivity activity) {
        def res = new ClassActivityResponse()
        res.id = activity.id
        res.facilityId = activity.facilityId
        res.name = activity.name
        res.teaser = activity.teaser
        res.imageUrl = activity.largeImage?.optimizedImageFullURL
        res.messageTitle = activity.userMessageLabel
        res.level = ["min": activity.levelMin, "max": activity.levelMax]
        res.occasions = new ArrayList<>()
        res.terms = activity.terms
        res.numOfOccasions = activity.occasions.size()
        return res
    }

    Collection<ActivityOccasion> filterOccasionsCollectionByCommand(Collection<ActivityOccasion> activityOccasions, Boolean hideBookedOccasions, User user) {

        activityOccasions.findAll {
            if (!it.isUpcomingOnlineOccasion()) {
                return false
            }
            if (user && hideBookedOccasions) {
                return !(it.participations.find {
                    Participation p ->
                        p.customer.user == user
                })
            }
            return true
        }
    }

    def activities(GetActivitiesCommand cmd) {

        def user = (User) getCurrentUser()
        Collection<ActivityOccasion> activityOccasions =
                filterOccasionsCollectionByCommand(activityService.searchForClassActivityOccasions(cmd.sportIds,
                        cmd.facilityIds,
                        new LocalDate(cmd.startDate),
                        new LocalDate(cmd.endDate),
                        cmd.locationSearch,
                        cmd.querySearch,
                        cmd.level),
                        cmd.hideBookedOccasions,
                        user)

        Map<Long, ClassActivityResponse> activities = [:]

        if (cmd.hideFullyBooked) {
            activityOccasions = activityOccasions.findAll { ActivityOccasion activityOccasion ->
                activityOccasion.participations.size() < activityOccasion.maxNumParticipants
            }
        }

        activityOccasions.each { ActivityOccasion it ->
            ClassActivityResponse activity
            if (activities.containsKey(it.activity.id)) {
                activity = activities.get(it.activity.id)
            } else {
                activity = fromClassActivity((ClassActivity) it.activity)
                activities.put(it.activity.id, activity)
            }

            if (cmd.exposeOccasions) {
                activity.occasions.add(fromActivityOccasion(user, it))
            }
        }
        activities.values().each { ClassActivityResponse car -> car.occasions.sort { ActivityOccasionResponse aor -> aor.startTime } }

        render activities.values() as JSON
    }

    def price() {
        ActivityOccasion occasion = ActivityOccasion.get(params.id as Long)
        def slots = occasion.bookings.collect { it.slot }
        def result = [
                price           : occasion.price,
                currency        : occasion.activity.facility.currency,
                promoCodeEnabled: false,
                paymentMethods  : getActivityPaymentMethods(getCurrentUser() as User, slots, new Long(occasion.price))
        ]
        render result as JSON
    }

    //temporary overriden until we use the same uproach for bookings
    List getActivityPaymentMethods(User user, List<Slot> slots, Long totalPrice) {
        List paymentMethods = []

        // valid coupons
        def validCoupons = couponService.getValidCouponsByUserAndSlots(user, slots, totalPrice).collect {
            [id: it.id, name: it.coupon.name, remaining: it.nrOfTickets]
        }
        // valid giftCards
        // def validGiftCards = customer ? couponService.getValidCouponsByCustomerAndSlots(customer, slots, totalPrice, GiftCard.class) : []
        def validGiftCards = couponService.getValidCouponsByUserAndSlots(user, slots, totalPrice, GiftCard.class).collect {
            [id: it.id, name: it.coupon.name, remaining: it.nrOfTickets]
        }

        // free courts
        if (totalPrice == 0l) {
            paymentMethods.push([type: PaymentMethod.FREE.name()])
        }

        def recurringPaymentInfo = getRecurringPaymentInfo()
        if (recurringPaymentInfo) {
            recurringPaymentInfo.put("type", PaymentMethod.CREDIT_CARD_RECUR.name())
            paymentMethods.push(recurringPaymentInfo)
        }

        if (!validCoupons.isEmpty()) {
            paymentMethods.addAll(validCoupons.collect { it.put("type", PaymentMethod.COUPON.name()); return it })
        }

        if (!validGiftCards.isEmpty()) {
            paymentMethods.addAll(validGiftCards.collect { it.put("type", PaymentMethod.GIFT_CARD.name()); return it })
        }

        return paymentMethods
    }
}

@Validateable(nullable = true)
class BookingActivityCommand {
    Long activityOccasionId
    PaymentCommand payment
    String userMessage
    Boolean acceptTerms

    static constraints = {
        activityOccasionId(nullable: false, blank: false)
        userMessage(nullable: true, maxSize: 255)
        acceptTerms(nullable: true)
    }
}

class ActivityOccasionResponse {
    Long id
    Long activityId
    String message
    int basePrice
    String currency
    Long courtId
    DateTime startTime
    DateTime endTime
    int participants
    int maxNumParticipants
    Integer minNumParticipants
    Boolean membersOnly
    DateTime automaticCancellationDateTime
    boolean isBookable
    boolean isParticipating
    boolean canBeCancelledByUser
    boolean hasNotClosedRegistration
    boolean hasOpenedRegistration
    RefundPolicyResponse refundPolicy
    String refundableServiceFee
}

class RefundPolicyResponse {
    String code
    List<Integer> args
}

class ClassActivityResponse {
    Long id
    Long facilityId
    String name
    String teaser
    String imageUrl
    String messageTitle
    Map<String, Integer> level
    String terms
    int numOfOccasions
    List<ActivityOccasionResponse> occasions
}