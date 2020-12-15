package com.matchi.activities
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.IArticleItem
import com.matchi.IReservation
import com.matchi.Payment
import com.matchi.orders.Order
import com.matchi.payment.ArticleType
import org.joda.time.DateTime

class Participation implements IArticleItem, IReservation {
    private static final long serialVersionUID = 12L

    Customer customer
    ActivityOccasion occasion
    DateTime joined
    Payment payment
    Order order

    def isRefundable() {
        return !occasion.getRefundableUntil().isBeforeNow()
    }

    def hasPayment() {
        return payment != null || order != null
    }

    static constraints = {
        customer(nullable: false)
        joined(nullable: false)
        occasion(nullable: false)
        payment(nullable: true)
        order(nullable: true)
    }

    static namedQueries = {
        byActivity { activity ->
            createAlias("occasion", "o")
            eq("o.activity", activity)
        }
    }

    static mapping = {
        order cascade: 'none'
    }

    @Override
    void replaceOrderAndSave(Order order) {
        this.order = order
        this.save(flush: true, failOnError: true)
    }

    @Override
    ArticleType getArticleType() {
        return ArticleType.ACTIVITY
    }

    @Override
    Date getDate() {
        return occasion?.getStartDateTime()?.toDate()
    }

    @Override
    Facility getFacility() {
        return occasion?.activity?.facility
    }
}
