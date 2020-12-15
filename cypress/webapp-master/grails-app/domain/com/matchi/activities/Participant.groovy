package com.matchi.activities

import com.matchi.Customer
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.Submission

/**
 * @author Sergei Shushkevich
 */
class Participant implements Serializable {
    private static final long serialVersionUID = 12L

    Status status = Status.RESERVED

    Customer customer
    Activity activity
    Submission submission

    static belongsTo = [Activity, Customer, ActivityOccasion]
    static hasMany = [occasions: ActivityOccasion]

    static constraints = {
        customer unique: "activity"
        submission nullable: true
    }

    static namedQueries = {
        byFacility { facility ->
            createAlias("customer", "c")
            eq("c.facility", facility)
        }

        submittedValues { List courses, FormField.Type fieldType ->
            createAlias("activity", "a")
            createAlias("submission", "s")
            createAlias("s.values", "sv")
            inList("a.id", courses*.id)
            eq("sv.fieldType", fieldType.name())
            order("sv.value", "asc")
            projections {
                distinct("sv.value")
            }
        }
    }

    static enum Status {
        ACTIVE("label-success"),
        PAUSED("label-warning"),
        CANCELLED("label-danger"),
        QUEUED("label-info"),
        RESERVED("label-primary")

        final String cssClass

        Status(String cssClass) {
            this.cssClass = cssClass
        }

        static list() {
            [ACTIVE, QUEUED, PAUSED, CANCELLED, RESERVED]
        }

        static listUsed() {
            [ACTIVE, PAUSED, CANCELLED, RESERVED]
        }
    }

    def remove() {
        occasions.collect().each {
            it.removeFromParticipants(this)
        }

        this.delete()
    }

    String toString() {
        return "${customer.fullName()}"
    }

    String getNumberOfOccasionsFromSubmission() {
        return submission?.values?.find { it.fieldType.equals(FormField.Type.NUMBER_OF_OCCASIONS.toString()) }?.value ?: "0"
    }
}
