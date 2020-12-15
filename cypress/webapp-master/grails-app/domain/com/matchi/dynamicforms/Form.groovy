package com.matchi.dynamicforms

import com.matchi.Amount
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.IRequired
import com.matchi.User
import com.matchi.activities.Activity
import com.matchi.activities.EventActivity
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.price.Price
import com.matchi.requirements.RequirementProfile
import org.apache.commons.lang.RandomStringUtils
/**
 * Form that created by Facility and can be published during specific period of time.
 *
 * @author Michael Astreiko
 */
class Form implements Serializable, IRequired {

    private static final long serialVersionUID = 12L

    String name
    //a way for the facility to describe to submitters what to do
    String description
    //Generated Hashkey that will used to show Form to users
    String hash
    Date activeFrom
    Date activeTo

    Date dateCreated
    Date lastUpdated

    Integer maxSubmissions
    boolean membershipRequired
    boolean paymentRequired

    Integer price

    //If Form was created based on template it will be stored here
    FormTemplate relatedFormTemplate
    RequirementProfile requirementProfile

    List fields
    static hasMany = [fields: FormField, submissions: Submission]
    static belongsTo = [facility: Facility, course: CourseActivity, event: EventActivity]

    static constraints = {
        relatedFormTemplate(nullable: true)
        hash(unique: true)
        maxSubmissions(nullable: true, min: 1)
        description(nullable: true)
        course nullable: true
        event nullable: true
        price nullable: true, min: 0
        requirementProfile nullable: true
    }

    static mapping = {
        description type: 'text'
    }

    def beforeValidate() {
        if (!hash) {
            hash = RandomStringUtils.randomAlphanumeric(20)
        }
    }

    String createOrderDescription() {
        "${facility.name} ${name}"
    }

    Amount toAmount(Customer customer) {
        def amount = new Amount()
        def actualPrice = new BigDecimal(price)
        amount.amount = actualPrice

        if (actualPrice > 0) {
            amount.VAT = Price.calculateVATAmount(price, new Double((facility.vat ?: 0)))
        } else {
            amount.VAT = 0
        }

        amount
    }

    boolean isActiveNow() {
        def now = new Date().clearTime()
        return now >= this.activeFrom && now <= this.activeTo
    }

    Activity getActivity() {
        this.course ?: this.event
    }

    @Override
    boolean checkUser(User user) {
        if(!facility.hasRequirementProfiles()) {
            return true
        }

        if(!requirementProfile) {
            return true
        }

        Customer customer = Customer.findByUserAndFacility(user, facility)

        if(!customer) {
            return false
        }

        return requirementProfile.validate(customer)
    }

    Integer getAcceptedSubmissionsAmount() {
        course ? (course.participants ? course.participants.size() : 0) +
                Submission.countByFormAndStatus(this, Submission.Status.WAITING) :
                Submission.countByForm(this)
    }

    boolean isSubmissionAllowed() {
        !maxSubmissions || acceptedSubmissionsAmount < maxSubmissions
    }
}
