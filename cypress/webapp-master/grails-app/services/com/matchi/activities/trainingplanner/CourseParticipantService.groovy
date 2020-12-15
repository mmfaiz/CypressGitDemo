package com.matchi.activities.trainingplanner

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Season
import com.matchi.activities.Participant
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.Submission
import com.matchi.facility.FilterCourseParticipantCommand
import grails.gorm.DetachedCriteria
import org.apache.commons.lang3.StringUtils
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.LocalDate
/**
 * @author Sergei Shushkevich
 */
class CourseParticipantService {

    static transactional = false

    def customerService
    def groovySql

    List findParticipantsIds(Facility facility, FilterCourseParticipantCommand filter) {
        def dc = new DetachedCriteria(Submission).build {
            projections {
                distinct("id")
            }
            customer {
                eq("facility", facility)
            }
        }

        def pickupSubmissions = dc.build {
            values {
                eq("fieldType", FormField.Type.TEXT_CHECKBOX_PICKUP.name())
            }
        }
        def allergiesSubmissions = dc.build {
            values {
                eq("fieldType", FormField.Type.TEXT_CHECKBOX_ALLERGIES.name())
            }
        }

        Participant.withCriteria {
            createAlias("customer", "c")
            createAlias("activity", "crs")

            projections {
                distinct("id")
            }

            eq("c.facility", facility)
            eq("crs.class", CourseActivity.DISCRIMINATOR)

            if (filter.groups) {
                createAlias("c.customerGroups", "cg", CriteriaSpecification.LEFT_JOIN)
                or {
                    if (filter.groups.contains(0L)) {
                        isEmpty("c.customerGroups")
                    }
                    inList("cg.group.id", filter.groups)
                }
            }

            if (filter.seasons) {
                or {
                    Season.findAllByIdInList(filter.seasons).each { s ->
                        // Start after season start, end before season end
                        and {
                            ge("crs.startDate", s.startTime.clone().clearTime())
                            le("crs.endDate", s.endTime.clone().clearTime())
                        }
                        // Start before season start, end before season end
                        and {
                            le("crs.startDate", s.startTime.clone().clearTime())
                            ge("crs.endDate", s.startTime.clone().clearTime())
                            le("crs.endDate", s.endTime.clone().clearTime())
                        }
                        // Start after season start, end after season end
                        and {
                            ge("crs.startDate", s.startTime.clone().clearTime())
                            le("crs.startDate", s.endTime.clone().clearTime())
                            ge("crs.endDate", s.endTime.clone().clearTime())
                        }
                        // Start after season start, end before season end
                        and {
                            le("crs.startDate", s.startTime.clone().clearTime())
                            ge("crs.endDate", s.endTime.clone().clearTime())
                        }
                    }
                }
            } else if (filter?.courses?.isEmpty()) {
                ge("crs.endDate", new Date())
            }
            if (filter.courses) {
                inList("crs.id", filter.courses)
            }
            if (filter.statuses) {
                inList("status", filter.statuses)
            }
            if (filter.genders) {
                inList("c.type", filter.genders)
            }
            if (filter.q) {
                or {
                    def q = StringUtils.replace(filter.q, "_", "\\_")
                    ilike("c.email", "%${q}%")
                    ilike("c.firstname", "%${q}%")
                    ilike("c.lastname", "%${q}%")
                    ilike("c.companyname", "%${q}%")
                    ilike("c.telephone", "%${q}%")
                    ilike("c.cellphone", "%${q}%")
                    ilike("c.contact", "%${q}%")
                    ilike("c.notes", "%${q}%")
                    ilike("c.invoiceAddress1", "%${q}%")
                    ilike("c.invoiceContact", "%${q}%")
                    sqlRestriction("number like ?", ["%${q}%" as String])
                    sqlRestriction("concat(firstname,' ',lastname) like ?", ["%${q}%" as String])
                }
            }
            if (filter.occasions) {
                or {
                    filter.occasions.each { nr ->
                        sizeEq("occasions", nr)
                    }
                }
            }

            if(filter.hasSubmission) {
                isNotNull('submission')
            } else if(filter.hasSubmission != null && !filter.hasSubmission) {
                isNull('submission')
            }

            createAlias("submission", "s", CriteriaSpecification.LEFT_JOIN)
            createAlias("s.values", "sv", CriteriaSpecification.LEFT_JOIN)
            if ((filter.hasSubmission == null || filter.hasSubmission) && filter.pickup != null || filter.allergies != null) {
                or {
                    if (filter.pickup != null) {
                        if (filter.pickup) {
                            and {
                                eq("sv.fieldType", FormField.Type.TEXT_CHECKBOX_PICKUP.name())
                                isNotNull("sv.value")
                            }
                        } else {
                            not {
                                inList("s.id", pickupSubmissions.list() ?: [-1L])
                            }
                        }
                    }
                    if (filter.allergies != null) {
                        if (filter.allergies) {
                            and {
                                eq("sv.fieldType", FormField.Type.TEXT_CHECKBOX_ALLERGIES.name())
                                isNotNull("sv.value")
                            }
                        } else {
                            not {
                                inList("s.id", allergiesSubmissions.list() ?: [-1L])
                            }
                        }
                    }

                }
            }

            if (filter.wantedOccasions) {
                and {
                    inList("sv.value", filter.wantedOccasions.collect { it.toString() })
                    eq("sv.fieldType", FormField.Type.NUMBER_OF_OCCASIONS.name())
                }
            }

            if (filter.memberStatuses && filter.memberStatuses.size() == 1) {
                createAlias("c.memberships", "m", CriteriaSpecification.LEFT_JOIN)
                def today = new LocalDate()
                if (filter.memberStatuses.contains(FilterCourseParticipantCommand.MemberStatus.MEMBER)) {
                    and {
                        le("m.startDate", today)
                        ge("m.gracePeriodEndDate", today)
                    }
                } else {
                    not {
                        inList("c.id", customerService.getMembersIds(facility))
                    }
                }
            }
        }
    }

    List findParticipants(Facility facility, FilterCourseParticipantCommand filter) {
        def ids = findParticipantsIds(facility, filter)

        Participant.createCriteria().list {
            createAlias("customer", "c")
            createAlias("activity", "crs")

            inList("id", ids ?: [-1L])

            if (filter.sort) {
                order(filter.sort, filter.order ?: "asc")
            }
        }
    }

    Map findParticipantsWithOccasions(Facility facility, FilterCourseParticipantCommand filter) {
        def ids = findParticipantsIds(facility, filter)
        if (!ids) {
            return [participants: [], totalCount: 0]
        }

        def sql = """select p.id, p.status, concat(c.firstname, ' ' , c.lastname) as customer,
                    c.firstName as customerFirstName,
                    c.lastName as customerLastName,
                    c.id as customerId, crs.name as activity, count(aop.activity_occasion_id) as occasions,
                    c.birthyear as birthYear,
                    c.club as club,
                    p.submission_id as submission,
                    sv_wanted_occasion.value as wantedOccasions,
                    SUM(timestampdiff(minute, ao.start_time, ao.end_time)) as plannedMinutes
                from participant p
                left join submission_value as sv_wanted_occasion on sv_wanted_occasion.submission_id = p.submission_id AND field_type = '${FormField.Type.NUMBER_OF_OCCASIONS.name()}'
                join customer c on c.id = p.customer_id
                join activity crs on crs.id = p.activity_id
                left join form f on crs.form_id = f.id
                left join activity_occasion_participants aop on aop.participant_id = p.id
                left join activity_occasion ao on ao.id = aop.activity_occasion_id
                where p.id in (${ids.join(',')})
                group by p.id
        """
        if (filter.sort) {
            sql += "order by ${filter.sort} ${filter.order ?: 'asc'}"
        }
        def rows = groovySql.rows(sql, [], filter.offset,
                filter.allselected ? Integer.MAX_VALUE : filter.max)
        List<Long> customerIds = rows.collect {it.customerId}
        List<Customer> customerList = Customer.createCriteria().list {
            'in'("id", customerIds ?: [-1L])
        }

        rows = rows.collect { row ->
            row.customerData = customerList.find {it.id == row.customerId}
            row
        }

        groovySql.close()
        [participants: rows, totalCount: ids.size()]
    }
}
