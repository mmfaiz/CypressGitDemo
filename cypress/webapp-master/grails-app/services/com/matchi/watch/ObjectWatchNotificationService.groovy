package com.matchi.watch

import com.matchi.Court
import com.matchi.Facility
import com.matchi.PhoneUtil
import com.matchi.Slot
import com.matchi.Sport
import com.matchi.User
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import org.joda.time.DateTime

class ObjectWatchNotificationService {

    def notificationService
    def smsService
    def tinyurlService
    def messageSource
    def grailsLinkGenerator
    def dateUtil

    def addNotificationFor(User user, Facility facility, Court court, DateTime from, DateTime to, boolean smsNotify) {
        addNotificationFor(user, facility, court, null, from, to, smsNotify)
    }

    def addNotificationFor(User user, Facility facility, Court court, Sport sport,
                           DateTime from, DateTime to, boolean smsNotify) {

        def toDate = to.toDate()
        def fromDate = from.toDate()

        def currentWatch = SlotWatch.withCriteria {
            eq("user", user)
            eq("facility", facility)
            eq("fromDate", fromDate)
            eq("toDate", toDate)
            if (court) {
                eq("court", court)
            } else {
                isNull("court")
            }
            if (sport) {
                eq("sport", sport)
            } else {
                isNull("sport")
            }
            maxResults(1)
        }[0]

        if (currentWatch) {
            return currentWatch
        }

        SlotWatch watch = new SlotWatch()
        watch.toDate = toDate
        watch.fromDate = fromDate
        watch.user = user
        watch.court = court
        watch.sport = sport
        watch.facility = facility
        watch.smsNotify = smsNotify

        return watch.save()

    }

    ClassActivityWatch addNotificationFor(User user, ClassActivity activity,
                                          Date fromDate, boolean smsNotify) {

        def currentWatch = ClassActivityWatch.withCriteria {
            eq("user", user)
            eq("classActivity", activity)
            eq("fromDate", fromDate)
            maxResults(1)
        }[0]

        if (currentWatch) {
            return currentWatch
        }

        new ClassActivityWatch(classActivity: activity, fromDate: fromDate,
                toDate: new DateTime(fromDate).plusHours(1).toDate(),
                user: user, smsNotify: smsNotify, facility: activity.facility).save()
    }

    def trySendNotificationsFor(String slotId) {

        def task = SlotWatch.async.task {
            try {
                Slot slot = Slot.get(slotId)
                boolean membersOnly = slot.court.membersOnly
                boolean offlineOnly = slot.court.offlineOnly

                boolean availableForAll = !(membersOnly || offlineOnly)

                if (!slot) {
                    throw new IllegalArgumentException("Could not find slot with id ${slotId}")
                }

                slot.merge()

                def facility = slot.court.facility

                def watches = SlotWatch.createCriteria().list {
                    eq("facility", facility)
                    lte("fromDate", slot.startTime)
                    gt("toDate", slot.startTime)
                }?.unique { it.user }?.toList()

                log.debug("Found ${watches.size()} slot watches for ${slotId}")

                watches.each { SlotWatch s ->
                    if (s.court == slot.court || (!s.court && (!s.sport || s.sport.id == slot.court.sport.id)
                            && (availableForAll || (membersOnly && (s.user.hasActiveMembershipIn(facility) || s.user.getMembershipIn(facility)?.inStartingGracePeriod))))) {
                        notificationService.sendSlotWatchNotificationTo(s.user, slot)

                        if (s?.smsNotify && s?.user?.telephone) {
                            String facilityLink = grailsLinkGenerator.link([controller: "facility", action: "show", absolute: 'true', params: [name: facility.shortname, date: new DateTime(slot.startTime).toString(dateUtil.DEFAULT_DATE_FORMAT)]])
                            def phoneNumber = PhoneUtil.convertToInternationalFormat(s.user.telephone)

                            smsService.send(phoneNumber,
                                    messageSource.getMessage(
                                            "templates.sms.queue.text",
                                            [new DateTime(slot.startTime).toString('HH:mm'),
                                             new DateTime(slot.startTime).toString('d/M'),
                                             facility.name, tinyurlService.tiny(facilityLink)] as String[],
                                            new Locale(s.user.language)))
                        }
                    }
                }
            } catch (err) {
                log.error("Error sending slot watch notification!", err)
            }
        }
    }

    void sendActivityNotificationsFor(Long occasionId) {
        def task = ClassActivityWatch.async.task {
            def occasion = ActivityOccasion.get(occasionId)
            if (occasion.hasNotClosedRegistration()) {
                def facility = occasion.activity.facility

                def watches = ClassActivityWatch.withCriteria {
                    eq("facility", facility)
                    eq("classActivity", occasion.activity)
                    eq("fromDate", occasion.getStartDateTime().toDate())
                }

                log.debug("Found ${watches.size()} activity watches for occasion ${occasion.id}")

                watches.each {
                    notificationService.sendActivityWatchNotificationTo(it.user, occasion)

                    if (it.smsNotify && it.user.telephone) {
                        String facilityLink = grailsLinkGenerator.link([controller: "facility", action: "show", absolute: 'true', params: [name: facility.shortname]])
                        def phoneNumber = PhoneUtil.convertToInternationalFormat(it.user.telephone)

                        smsService.send(phoneNumber,
                                messageSource.getMessage(
                                        "templates.sms.activityQueue.text",
                                        [occasion.startTime.toString('HH:mm'), occasion.date.toString('d/M'),
                                         facility.name, tinyurlService.tiny(facilityLink)] as String[],
                                        new Locale(it.user.language)))
                    }

                }
            }
        }
    }
}
