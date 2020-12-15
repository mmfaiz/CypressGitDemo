package com.matchi

import com.matchi.coupon.CustomerCoupon
import com.matchi.payment.ArticleType
import com.matchi.payment.PaymentMethod
import com.matchi.payment.PaymentStatus
import org.joda.time.DateTime

class Payment implements Serializable {

    def grailsApplication

    static belongsTo = [ facility : Facility, customer: Customer, booking: Booking ]
    static hasMany = [ paymentTransactions:PaymentTransaction ]

    // To handle migrations of Payment => Order
    boolean migrated = false

    /*
        Represents type of product that is being paid for
     */
    ArticleType articleType;

    /*
        Indicates what type of method used to perform payment
     */
    PaymentMethod method
    PaymentStatus status

    Date dateCreated
    Date lastUpdated

    /**
        Indicates when the product is to be delivered (ie slot.startTime)
     */
    Date dateDelivery

    String orderNumber
    String orderDescription
    Map orderParameters

    /*
        Auriga nets information
     */
    String merchantId
    String paymentVersion
    String orderNr
    String transactionId
    String netsStatus
    String netsStatusCode
    String authCode
    String my3Dsec
    String batchId
    String currency
    String paymentMethod
    String expDate
    String cardType
    String cardNumber
    String riskScore
    String issueingBank
    String iPCountry
    String issueingCountry
    String amount
    String vat
    String feeAmount
    String mac

    /*
        Customer coupons
     */
    CustomerCoupon customerCoupon

    /*
        Date that indicated when the actual confirmation of the authorization was done
        That is, when the delivery date has been set at the payment gateway
     */
    Date dateConfirmed

    /*
        Date that indicates when and if an authorized reversal (ie. cancellation of booking) was done
     */
    Date dateReversed

    /*
        Date that indicates that the payment has been annulled
     */
    Date dateAnnulled

    Payment() {
    }

    Payment(PaymentOrder paymentOrder) {
        method = paymentOrder.method
        facility = paymentOrder.facility
        customer = getOrderCustomer(paymentOrder)
        articleType = paymentOrder.articleType
        orderNumber = paymentOrder.orderNumber
        orderDescription = paymentOrder.orderDescription
        orderParameters = new HashMap()
        orderParameters.putAll(paymentOrder.orderParameters)
    }

    static constraints = {
        status(nullable: false)
        articleType(nullable: false)
        dateConfirmed(nullable: true)
        dateReversed(nullable: true)
        dateDelivery(nullable: true)
        dateAnnulled(nullable: true)
        customer(nullable: true)
        method(nullable: true)
        customerCoupon(nullable: true)
        orderDescription(nullable: true)
        orderNumber(nullable: false)

        // nets properties
        transactionId(nullable: true)
        authCode(nullable: true)
        netsStatus(nullable: true)
        netsStatusCode(nullable: true)
        paymentVersion(nullable: true)
        merchantId(nullable: true)
        orderNr(nullable: true)
        paymentMethod(nullable: true)
        expDate(nullable: true)
        cardType(nullable: true)
        mac(nullable: true)
        amount(nullable: true)
        vat(nullable: true)
        currency(nullable: true)
        my3Dsec(nullable: true)
        batchId(nullable: true)
        riskScore(nullable: true)
        issueingBank(nullable: true)
        iPCountry(nullable: true)
        feeAmount(nullable: true)
        issueingCountry(nullable: true)
        cardNumber(nullable: true)
        booking nullable: true
    }

    static mapping = {
        autoTimestamp true
        sort 'dateCreated'
        paymentTransactions sort: "dateCreated", order: "desc"
        customerCoupon column: "coupon_id"
    }

    def valid() {
        return PaymentStatus.OK == status
    }

    def amountFormatted() {
        if ([PaymentMethod.COUPON, PaymentMethod.GIFT_CARD].contains(method)) {
            return "0 ${facility?.currency}";
        }

        if(amount) {
            Long amountValue = Long.valueOf(amount)
            return (amountValue / 100) + " " + facility?.currency
        } else {
            return null
        }
    }

    def amountFormattedAmountOnly() {
        if(amount) {
            Long amountValue = Long.valueOf(amount)
            return (amountValue / 100)
        } else {
            return null
        }
    }

    def statusMessage() {
        return NetsPaymentGatewayErrors.getErrorMessage(netsStatusCode, netsStatus)
    }

    def isRefundable() {
        DateTime start = new DateTime(dateDelivery)
        return !start.minusHours(this.facility.getBookingCancellationLimit()).isBeforeNow() && !dateConfirmed
    }

    public getPaymentStatus() {
        def totalAmountPaid = getTotalPaid()

        if (!totalAmountPaid) {
            return PaymentStatus.PENDING
        } else if (totalAmountPaid < amountFormattedAmountOnly()) {
            return PaymentStatus.PARTLY
        } else if (totalAmountPaid >= amountFormattedAmountOnly()) {
            return PaymentStatus.OK
        }

        return PaymentStatus.PENDING
    }

    public getTotalPaid() {
        def totalAmountPaid = 0

        if (this.paymentTransactions) {
            this.paymentTransactions.each {
                totalAmountPaid += it.paidAmount
            }
        }

        return totalAmountPaid
    }

    def isCreditCard() {
        return (hasPaymentMethod(PaymentMethod.CREDIT_CARD) || hasPaymentMethod(PaymentMethod.CREDIT_CARD_RECUR))
    }

    def isInvoice() {
        return hasPaymentMethod(PaymentMethod.INVOICE)
    }

    def isCoupon() {
        return hasPaymentMethod(PaymentMethod.COUPON) || hasPaymentMethod(PaymentMethod.GIFT_CARD)
    }

    def isCash() {
        return hasPaymentMethod(PaymentMethod.CASH)
    }

    def hasPaymentMethod(PaymentMethod method) {
        return (this.method != null &&
                (this.method.equals(method)))
    }

    def getOrderCustomer(PaymentOrder order) {
        return order?.customer ?: Customer.findByUserAndFacility(order.user, facility)
    }

    def paymentStatus() {
        if(netsStatusCode.equals("A")) {
            if(dateReversed) {
                return "CANCELLED"
            }

            if(dateConfirmed) {
                return "CONFIRMED"
            }

            if(dateAnnulled) {
                return "ANNULLED"
            }

            return "AUTHORIZED"
        } else {
            return "ERR"
        }
    }
}
