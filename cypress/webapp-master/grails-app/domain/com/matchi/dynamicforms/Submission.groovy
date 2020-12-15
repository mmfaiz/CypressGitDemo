package com.matchi.dynamicforms

import com.matchi.Customer
import com.matchi.IArticleItem
import com.matchi.User
import com.matchi.orders.Order

/**
 * @author Michael Astreiko
 */
class Submission implements Serializable, IArticleItem {
    private static final long serialVersionUID = 12L

    Date dateCreated
    Date originalDate
    Order order
    //these might differ from customer.user since application might be for another person than the actual user logged in at application submit
    //like when parents submit forms for their children
    User submissionIssuer
    Status status

    static belongsTo = [form: Form, customer: Customer, submissionIssuer: User]
    static hasMany = [values: SubmissionValue]

    static constraints = {
        order(nullable: true)
        customer(nullable: true)
        submissionIssuer(nullable: true)
        status(nullable: true)
        originalDate(nullable: true)
    }

    static mapping = {
        version false
        values batchSize: 20
        order cascade: 'none'
    }

    static namedQueries = {
        byFacility { facility ->
            form {
                eq("facility", facility)
            }
        }
    }

    enum Status {
        WAITING, DISCARDED, ACCEPTED

        List list() {
            return [WAITING, DISCARDED, ACCEPTED]
        }
    }

    boolean isProcessed() {
        this.status in [Status.ACCEPTED, Status.DISCARDED]
    }

    @Override
    void replaceOrderAndSave(Order order) {
        this.order = order
        this.save(flush: true, failOnError: true)
    }
}
