package com.matchi
import grails.validation.Validateable
import org.joda.time.DateTime

@Validateable(nullable = true)
class CreateSubscriptionCommand implements Serializable {
    Long id
    Long customerId = null
    String description
    String dateFrom
    String dateTo
    String time
    Long courtId
    Long season
    int frequency = 1
    int interval
    boolean showComment = false
    String accessCode

    static constraints = {
        customerId(blank: false, nullable:false, validator: { customerId, obj ->
            def customer = Customer.findById(customerId)

            customer != null ? true : ['customer.notexists']
        })

        courtId(blank: false,nullable:false)
        time(blank: false,nullable: false)
        dateFrom(blank:false, nullable: false)

        dateTo(blank:false, nullable: false, validator: { dateTo, obj ->
			def dateFrom = obj.properties['dateFrom']
            dateFrom = new DateTime(dateFrom)
            dateTo = new DateTime(dateTo)
			dateFrom.isBefore(dateTo) || dateFrom.isEqual(dateTo) ? true : ['invalid.datemismatch']
		})

        frequency(nullable: true)
        interval(nullable: true)
        showComment(nullable: true)
        accessCode(nullable: true, maxSize: 255)
    }
}
