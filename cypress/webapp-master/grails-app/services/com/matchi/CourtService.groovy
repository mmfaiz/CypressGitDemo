package com.matchi

import com.matchi.api_ext.model.APIExtCourtSummary
import com.matchi.integration.events.CourtEventType
import com.matchi.integration.events.EventType
import com.matchi.integration.events.EventWrapper
import com.matchi.integration.events.InitiatorProvider
import grails.transaction.NotTransactional
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime

class CourtService {

    def groovySql
    def kafkaProducerService
    def springSecurityService

    static transactional = true


    def deleteCourt(Court court) {
        log.debug("Removing court ${court.name} (${court.id}) on facility ${court.facility.name}")

        SeasonCourtOpeningHours.findAllByCourt(court)*.delete()
        court.delete()
        sendCourtEvent(court, CourtEventType.DELETED)

        return court
    }

    def updateCourt(Court court) {
        log.debug("Updating court ${court.name} (${court.id}) on facility ${court.facility.name}")

        def c = court.save(flush: true, failOnError: true)
        sendCourtEvent(court, CourtEventType.UPDATED)
        return c
    }

    def createCourt(Court court) {
        log.debug("Creating court ${court.name} (${court.id}) on facility ${court.facility.name}")

        def c = court.save(flush: true, failOnError: true)
        sendCourtEvent(court, CourtEventType.CREATED)
        return c
    }

    void organizeSports(Facility facility) {
        facility?.courts?.each {court ->
            if (!facility.sports?.contains(court.sport)) {
                court.sport = Sport.get(6)
                court.save()
                sendCourtEvent(court, CourtEventType.UPDATED)
            }
        }
    }

    @NotTransactional
    def findUsersCourts(List<Facility> facilities = [], List<Sport> sports = null, List<Court.Surface> surfaces, Boolean indoor, User user, Boolean hasCamera = null, Map<String, String> courtTypesFilter = [:]) {
        def allCourts = findCourts(facilities, sports, surfaces, indoor, hasCamera, courtTypesFilter)

        def userCourts = allCourts.findAll { it.hasUserAccess(user) }
        return userCourts
    }

    @NotTransactional
    def findMembersCourts(List<Facility> facilities = [], List<Sport> sports = null, List<Court.Surface> surfaces, Boolean indoor) {
        return findCourts(facilities, sports, surfaces, indoor).findAll { it.isMembersOnly() }
    }

    @NotTransactional
    def getCourtGroupRestrictions(Customer customer, Facility facility, List<Slot> slots) {
        if (customer?.exludeFromNumberOfBookingsRule || !facility.hasBookingLimitPerCourtGroup()) {
            return
        }
        def restrictions = [:]
        Map<Long, Integer> courtBookings = customer ?
            Booking.upcomingBookings(customer).list().groupBy {Booking booking -> booking.slot.courtId}.collectEntries {[it.key, it.value.size()]} as Map<Long, Integer> : new HashMap<>()

        List<CourtGroup> groups = CourtGroup.facilityCourtGroups(facility).list()
        //calculate slots booked for each group
        Map<Long, Integer> courtSlots = slots.groupBy {it.court.id}.collectEntries {[it.key, it.value.size()]} as Map<Long, Integer>
        Map<Long, Integer> groupSlots = groups.collectEntries{ group ->
            [group.id, group.courts.inject(0) { result, court -> result + (courtSlots[court.id] ?: 0) + (courtBookings[court.id] ?: 0) }]
        } as Map<Long, Integer>

        slots.each { slot ->
            //group belongs to the slot
            List<CourtGroup> slotCourtGroups = groups.findAll { it.courts.collect { it.id }.contains(slot.court.id) && it.maxNumberOfBookings }

            slotCourtGroups.each {
                if (it.maxNumberOfBookings < groupSlots[it.id]) {
                    restrictions.put("courtGroup", it.name)
                    restrictions.put("maxBookings", it.maxNumberOfBookings)
                    return
                }
            }
        }
        return restrictions
    }

    @NotTransactional
    def findCourts(List facilities = [], List sports = [], List surfaces = [], Boolean indoor = null, Boolean hasCamera = null, Map<String, String> courtTypesFilter = [:]) {
        return Court.createCriteria().listDistinct {
            createAlias("cameras", "c", CriteriaSpecification.LEFT_JOIN)
            createAlias("courtTypeAttributes", "cta", CriteriaSpecification.LEFT_JOIN)
            if (facilities) {
                facility {
                    "in"("id", facilities*.id)
                }
            }
            if (sports) {
                sport {
                    'in'('id', sports*.id)
                }
            }

            if (surfaces) {
                "in"("surface", surfaces)
            }

            if (indoor != null) {
                eq("indoor", indoor)
            }

            if (hasCamera != null) {
                if (hasCamera) {
                    isNotNull("c.cameraId")
                }
                else {
                    isNull("c.cameraId")
                }
            }
            or {
                courtTypesFilter.each { map ->
                    and {
                        eq("cta.courtTypeEnum", CourtTypeEnum.valueOf(map.key))
                        eq("cta.value", map.value)
                    }
                }
            }

            eq("archived", false)

            order("listPosition")
        }
    }

    @NotTransactional
    void updateCourtInstanceWithCourtTypeAttributes(Court courtInstance, GrailsParameterMap params) {
        courtInstance.courtTypeAttributes?.clear()

        if (params.list("courtTypeAttribute")?.any()) {
            List<String> attributeNames = params.list("courtTypeAttributeNames") as List<String>
            params.list("courtTypeAttribute").eachWithIndex { value, int index ->
                String name = attributeNames[index]
                CourtTypeAttribute courtTypeAttribute = courtInstance.id ?
                    CourtTypeAttribute.findOrCreateWhere(court: courtInstance, courtTypeEnum: CourtTypeEnum[name] as CourtTypeEnum) :
                    new CourtTypeAttribute(court: courtInstance, courtTypeEnum: CourtTypeEnum[name] as CourtTypeEnum)
                courtTypeAttribute.value = value
                courtInstance.addToCourtTypeAttributes(courtTypeAttribute)
            }
        }
    }

    @NotTransactional
    def findFacilitySurfaces(Facility facility) {

        def facCourtInfos = []

        facility.sports.each { Sport sport ->

            def indoor = Court.executeQuery("select new map(surface as surface, count(surface) as count) from Court as c where c.facility = ? and c.sport = ? and c.indoor = true and c.archived = false group by surface", [facility, sport])
            def outdoor = Court.executeQuery("select new map(surface as surface, count(surface) as count) from Court as c where c.facility = ? and c.sport = ? and c.indoor = false and c.archived = false group by surface", [facility, sport])

            FacilityCourtInfo courtInfo = new FacilityCourtInfo()
            courtInfo.sport = sport

            courtInfo.indoors = indoor
            courtInfo.outdoors = outdoor

            if (courtInfo.indoors || courtInfo.outdoors) {
                facCourtInfos << courtInfo
            }
        }

        return facCourtInfos
    }

    void swapListPosition(Court court1, Court court2) {
        def pos1 = court1.listPosition
        court1.listPosition = court2.listPosition
        court1.save()
        court2.listPosition = pos1
        court2.save()
        sendCourtEvent(court1, CourtEventType.UPDATED)
        sendCourtEvent(court2, CourtEventType.UPDATED)

    }

    def getCourtSummary(Facility facility, def from, def to, def courts) {
        String courtSql = """select c.id as court_id,
                               c.name as court_name,
                               c.list_position as court_position,
                               c.members_only as members_only,
                               c.offline_only as offline_only,
                               c.indoor as indoor,
                               c.surface as court_surface,
                               sp.id as sport_id,
                               sp.name as sport_name,
                               s.id as slot_id,
                               s.start_time,
                               s.end_time,
                               (select group_concat(distinct concat(cam.camera_id, ':' , cam.name)) as cameras
                                from camera cam
                                         left join court c3 on cam.court_id = c3.id
                                where c3.id = c.id) as cameras,
                               b.id as booking_id,
                               b.customer_id as booking_customer_id,
                               b.comments as booking_comments,
                               bg.type as booking_type,
                               f.show_booking_holder,
                               coalesce(concat(cu.firstname, ' ', cu.lastname), cu.companyname) as booking_name,
                               a.name as activity_name,
                               (select group_concat(coalesce(concat(c2.firstname, ' ', c2.lastname), coalesce(c2.companyname, 'n/a'))) as players
                                from player p2
                                    left join customer c2 on p2.customer_id = c2.id
                                where p2.booking_id = b.id) as players
                        from court c
                            join facility f on c.facility_id = f.id
                            join slot s on c.id = s.court_id
                            join sport sp on c.sport_id = sp.id
                            left join booking b on s.id = b.slot_id
                            left join booking_group bg on b.group_id = bg.id
                            left join activity_occasion_booking on b.id = activity_occasion_booking.booking_id
                            left join activity_occasion ao on activity_occasion_booking.activity_occasion_id = ao.id
                            left join activity a on ao.activity_id = a.id
                            left join customer cu on b.customer_id = cu.id
                        where s.end_time >= ?
                          and s.end_time <= ?
                          and f.id = ?
                          and c.archived = false
                        group by c.list_position
                        order by c.list_position, s.start_time;"""

        def courtParams = [ from, to, facility.id ]
        def courtRows = groovySql.rows(courtSql, courtParams).findAll {
            if (courts.contains(it.court_id)) {
                return it
            }
        }

        String reservationSql = """
            select c.id as court_id,
                   s.start_time
            from slot s
                     join court c on s.court_id = c.id
                     join facility f on c.facility_id = f.id
                     left join booking b on s.id = b.slot_id
            where s.start_time >= ?
              and s.start_time <= ?
              and b.id is not null
              and f.id = ?
            group by c.id
            order by c.id;"""

        def reservationParams = [ from, to, facility.id ]
        def reservationRows = groovySql.rows(reservationSql, reservationParams).findAll {
            if (courts.contains(it.court_id)) {
                return it
            }
        }


        def courtResult = []
        courtRows.each {courtRow ->
            APIExtCourtSummary apiExtCourtSummary = new APIExtCourtSummary(courtRow)

            // Populate the timestamp for the next reservation on the current court.
            reservationRows.each {reservationRow ->
                if (reservationRow.court_id == courtRow.court_id) {
                    apiExtCourtSummary.nextReservation = new DateTime(reservationRow.start_time)
                }
            }

            courtResult << apiExtCourtSummary
        }

        groovySql.close()
        return courtResult
    }

    private def sendCourtEvent(Court court, EventType<Court> eventType) {
        if (kafkaProducerService != null) {
            EventWrapper event = new EventWrapper(new com.matchi.integration.events.Court(court), eventType, InitiatorProvider.from(springSecurityService.getCurrentUser()))
            kafkaProducerService.send(event)
        }
    }
}
