package com.matchi

import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.springframework.util.StopWatch

import java.text.SimpleDateFormat
/**
 * Created with IntelliJ IDEA.
 * User: mattias
 * Date: 2013-06-25
 * Time: 13:01
 * To change this template use File | Settings | File Templates.
 */
class ImportSubscriptionService {
    static transactional = false

    def subscriptionService
    def springSecurityService

    /**
     * Creates CreateSubscriptionCommand according to the information in List<Map> data
     * @param data is map where each element contains data for a subscription (customernumber, description, dateFrom, dateTo,
     * time, weekDay, season, frequency, interval, showComment
     * @return subscription data [ command, weekDay, customerNumber ]
     */
    def parseSubscriptionData(List<Map> data) {

        User currentUser = (User)springSecurityService.currentUser
        Facility facility = currentUser?.facility

        def subscriptionssData   = []

        data.each {
            CreateSubscriptionCommand cmd = new CreateSubscriptionCommand()

            cmd.customerId = it.customernumber ? Customer.findByNumberAndFacility(Long.parseLong(it.customernumber), facility)?.id : null
            cmd.description = it.description ?: ""
            cmd.dateFrom = it.dateFrom
            cmd.dateTo = it.dateTo
            cmd.time = it.time
            cmd.courtId = it.court ? Court.findByNameAndFacility(it.court, facility)?.id : null
            cmd.season = it.season ?: null
            cmd.frequency = it.frequency ?: 1
            cmd.interval = it.interval ?: 1
            cmd.showComment = it.showComment ?: false

            def error = false

            if (cmd.hasErrors()) {
                error = true
            }

            subscriptionssData << [ cmd:cmd, weekDay:it.weekDay, customerNumber: it.customernumber, court: it.court, error: error ]
        }

        return subscriptionssData
    }

    /**
     * Add subscriptions to a facility
     * @param subscriptionData map with subsciption data
     * @return Created subscriptions
     */
    def importSubscriptions(def subscriptionData) {

        User currentUser = (User)springSecurityService.currentUser
        Facility facility = currentUser?.facility
        def failed = []
        def imported = []

        def stopWatch = new StopWatch("Import subscriptions")

        log.info("Starting import of ${subscriptionData?.size()} subsciptions on ${facility.name}")
        stopWatch.start()

        if (subscriptionData?.size() > 0 && facility) {
            subscriptionData.eachWithIndex { params, i ->

                if(i % 100 == 0) {
                    log.debug("Reached row ${i}")
                }

                def cmd = params.cmd

                if (!cmd.hasErrors()) {
                    Date from = new SimpleDateFormat("yyyy-MM-dd").parse(cmd.dateFrom)
                    Date to   = new SimpleDateFormat("yyyy-MM-dd").parse(cmd.dateTo)
                    DateTime dateFrom = new DateTime(from)
                    DateTime dateTo   = new DateTime(to)

                    def time         = new LocalTime(cmd.time)
                    def court        = Court.get(cmd.courtId)
                    def startTime    = dateFrom.withHourOfDay(time.hourOfDay).withMinuteOfHour(time.minuteOfHour)
                    def customer     = Customer.findById(cmd.customerId)
                    def slot         = Slot.findByCourtAndStartTime(court, startTime.toDate())

                    log.debug("${i}: ${customer?.number}, ${dateFrom.dayOfWeek}, ${time.getHourOfDay()}, ${court.name}, ${dateFrom.toString("yyyy-MM-dd")}. ${dateTo.toString("yyyy-MM-dd")}")

                    def subscription = null

                    if (slot) {
                        subscription = subscriptionService.createSubscription(cmd.description, cmd.showComment, dateFrom, dateTo, slot, params.weekDay, cmd.frequency, customer)
                        subscription?.save(failOnError: true)
                    }

                    if (!subscription) {
                        failed << subscription
                    } else {
                        imported << subscription
                    }
                }
            }
        }

        stopWatch.stop()

        log.info("Subsciption import finished with ${imported.size()} created and ${failed?.size()} filed in ${stopWatch.totalTimeSeconds} sec (${stopWatch.totalTimeSeconds / (imported.size()+failed.size())} / subsciption)")
        return [ imported: imported, failed:failed ]
    }
}
