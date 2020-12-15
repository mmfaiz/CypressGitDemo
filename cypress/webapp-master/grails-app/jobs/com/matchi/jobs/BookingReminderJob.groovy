package com.matchi.jobs

import com.matchi.Facility
import com.matchi.Slot
import com.matchi.Subscription
import com.matchi.FacilityProperty
import org.springframework.util.StopWatch

/**
 * @author Sergei Shushkevich
 */
class BookingReminderJob {

    static triggers = {
        cron name: "BookingReminderJob.trigger", cronExpression: "0 7 * * * ?" // 7 minutes past every hour
    }

    def bookingService
    def notificationService
    def subscriptionService
    def ticketService

    def concurrent = false
    def group = "BookingReminderJob"
    def sessionRequired = true

    def execute() {
        log.info("Running Booking reminder job")
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        FacilityProperty.createCriteria().listDistinct {
            eq("key", FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_REMINDER.name())
            eq("value", "1")
            projections {
                property('facility')
            }
        }.each { Facility facility ->
            Integer hoursBeforeToSend = facility.getFacilityPropertyValue(
                    FacilityProperty.FacilityPropertyKey.SUBSCRIPTION_REMINDER_HOURS.name()) as Integer
            Date startTime = new Date()
            def hours = startTime[Calendar.HOUR_OF_DAY]
            startTime.clearTime()
            startTime[Calendar.HOUR_OF_DAY] = hours + hoursBeforeToSend
            Date endTime = startTime.clone()
            endTime[Calendar.HOUR_OF_DAY] += 1

            def subscriptions = subscriptionService.getSubscriptionsToRemind(startTime, endTime, facility)

            if (subscriptions) {
                log.info("Sending reminder for ${subscriptions.size()} subscriptions for facility ${facility.toString()}")
                subscriptions.each { Subscription s ->
                    def slots = s.slots.findAll { Slot slot ->
                        slot.startTime >= startTime && slot.startTime < endTime &&
                                s.customer.id == slot.booking?.customer?.id
                    } as List

                    if (slots) {
                        def tickets = slots.collect {
                            ticketService.createBookingCancelTicket(it.booking, hoursBeforeToSend)
                        }

                        notificationService.sendBookingReminder(s.customer, slots, tickets)

                        slots.each {
                            bookingService.disableReminder(it.booking)
                        }
                    }
                }
            }
        }

        stopWatch.stop()
        log.info("Finished BookingReminderJob in ${stopWatch.totalTimeMillis} ms")
    }
}
