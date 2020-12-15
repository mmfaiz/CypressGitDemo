package com.matchi.idrottonline

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.FacilityProperty
import grails.transaction.NotTransactional
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.joda.time.LocalDate

class IdrottOnlineService {

    IdrottOnlineCommandBuilderService idrottOnlineCommandBuilderService
    IdrottOnlineCommandImporterService idrottOnlineCommandImporterService
    IdrottOnlineLoggerService idrottOnlineLoggerService
    GrailsApplication grailsApplication

    @NotTransactional
    void importCustomer(Customer customer){
        importCustomers(customer.facility, [customer])
    }

    @NotTransactional
    void importCustomers(Facility facility, List<Customer> customers, boolean skipInvalid = false){
        if (!facility?.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_IDROTT_ONLINE.name())) {
            return // Setting not activated.
        }
        IdrottOnlineSettings settings = new IdrottOnlineSettings(grailsApplication)
        idrottOnlineLoggerService.info("Import customers for ${facility?.name} (${customers?.size()})")
        IdrottOnlineMembershipCommand command = idrottOnlineCommandBuilderService.buildMembershipCommand(facility, settings, customers, skipInvalid)
        idrottOnlineCommandImporterService.importCommand(facility, command, settings)
    }

    @NotTransactional
    void importActivityOccasions(Facility facility, List<ActivityOccasionOccurence> activityOccasions, boolean skipInvalid = false){
        if (!facility?.hasIdrottOnlineActivitySync()) {
            return // Setting not activated.
        }

        IdrottOnlineSettings settings = new IdrottOnlineSettings(grailsApplication)
        idrottOnlineLoggerService.info("Import activity occasions (${activityOccasions?.size()})")
        IdrottOnlineActivitiesCommand command = idrottOnlineCommandBuilderService.buildActivitiesCommand(facility, settings, activityOccasions, skipInvalid)
        idrottOnlineCommandImporterService.importCommand(facility, command, settings)
    }

    List<Customer> getActiveOrTerminatedMembers(Facility facility) {
        def today = new LocalDate()
        List<Customer> getMembersToImport = Customer.createCriteria().listDistinct() {
            createAlias("memberships", "m")
            createAlias("m.type", "mt")
            eq("archived", Boolean.FALSE)
            or {
                isNull('type')
                eq("type", Customer.CustomerType.MALE)
                eq("type", Customer.CustomerType.FEMALE)
            }
            le("m.startDate", today)
            ge("m.gracePeriodEndDate", today)
            or {
                eq("facility", facility)
                if (facility.masterFacilities.size() > 0) {
                    and {
                        inList "facility.id", facility.masterFacilities*.id
                        eq("mt.groupedSubFacility", facility)
                    }
                }
            }
        }
    }

    List<Customer> getCustomersToImport(Facility facility) {
        List<Customer> customers = Customer.createCriteria().listDistinct() {
            createAlias("memberships", "m")
            createAlias("m.type", "mt")
            or {
                isNull('type')
                eq("type", Customer.CustomerType.MALE)
                eq("type", Customer.CustomerType.FEMALE)
            }
            le("m.startDate", new LocalDate())
            or {
                eq("facility", facility)
                if (facility.masterFacilities.size() > 0) {
                    and {
                        inList "facility.id", facility.masterFacilities*.id
                        eq("mt.groupedSubFacility", facility)
                    }
                }
            }
        }

        return customers
    }

    List<Customer> getAllMembers(Facility facility) {
        List<Customer> getMembersToImport = Customer.withCriteria {
            eq("facility", facility)
            eq("archived", Boolean.FALSE)
            or {
                isNull('type')
                eq("type", Customer.CustomerType.MALE)
                eq("type", Customer.CustomerType.FEMALE)
            }
        }
    }

    /**
     * Retrieves all customers updated since datetime based on customer.membership.lastupdated or
     * customer.lastupdated (!customer.archived && customer?.membership?.isActive()).
     * NOTE! Inlcudes archived members so we can remove them from IO when they are archived also.
     */
    List<Customer> getAllUpdatedCustomers(Facility facility, Date dateTime){
        List<Customer> customers = new ArrayList<Customer>()

        List<Customer> customersByLastUpdated = Customer.withCriteria {
            eq("facility", facility)
            gt("lastUpdated", dateTime)
            or {
                isNull('type')
                eq("type", Customer.CustomerType.MALE)
                eq("type", Customer.CustomerType.FEMALE)
            }
        }
        customers.addAll(customersByLastUpdated)

        def today = new LocalDate()
        List<Customer> customersByMembershipLastUpdated = Customer.withCriteria {
            createAlias("memberships", "m")
            createAlias("m.type", "mt")
            or {
                isNull('type')
                eq("type", Customer.CustomerType.MALE)
                eq("type", Customer.CustomerType.FEMALE)
            }
            gt("m.lastUpdated", dateTime)
            or {
                eq("facility", facility)
                if (facility.masterFacilities.size() > 0) {
                    and {
                        inList "facility.id", facility.masterFacilities*.id
                        eq("mt.groupedSubFacility", facility)
                    }
                }
            }
        }
        customers.addAll(customersByMembershipLastUpdated)
        customers.unique {it.id}
        customers
    }
}
