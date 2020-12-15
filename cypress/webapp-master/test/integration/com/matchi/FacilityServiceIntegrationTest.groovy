package com.matchi

import com.matchi.facility.FacilityFilterCommand
import org.apache.commons.lang.RandomStringUtils
import org.hibernate.stat.Statistics

import static com.matchi.TestUtils.createMunicipality

class FacilityServiceIntegrationTest extends GroovyTestCase {

    def facilityService
    Facility facility
    def sessionFactory

    void setUp() {
        facility = new Facility(name: RandomStringUtils.randomAlphabetic(10),
                shortname: RandomStringUtils.randomAlphabetic(10),
                email: "${RandomStringUtils.randomAlphabetic(10)}@matchi.se", active: true, bookable: true,
                bookingRuleNumDaysBookable: 10, boxnet: false, lat: 57.7012, lng: 12.0261, vat: 0,
                municipality: createMunicipality(),
                country: "SV")
    }

    void testListCache() {
        facilityService.saveFacility(facility)

        int startCount =  facilityService.list().size()
        facility.delete()
        assert facilityService.list().size() == startCount
        assert Facility.list().size() == startCount - 1


    }

    void testActiveListCacheIfFacilityDeactivatedThroughDomain() {
        facilityService.saveFacility(facility)

        int startCount = facilityService.listActive().size()
        facility.setActive(false)
        facility.save()
        assert facilityService.listActive().size() == startCount
    }

    void testActiveListCacheIfFacilityDeactivatedCorrectly() {


        facilityService.saveFacility(facility)

        enableHibernateStatistic()
        int startCount = facilityService.listActive().size()
        assert printStatAndGetQueryCount() == 1

        facility.setActive(false)
        assert facilityService.saveFacility(facility)
        assert facilityService.listActive().size() == startCount - 1


    }

    void testFindActiveFacilitiesCache() {
        FacilityFilterCommand ffc =  new FacilityFilterCommand()
        int startCount = facilityService.findActiveFacilities(ffc).count
        //if we add facility through service, cache for this method become invalidated as well
        facilityService.saveFacility(facility)
        assert facilityService.findActiveFacilities(ffc).count == startCount + 1
    }

    void testFindActiveFacilitiesCacheQueryInvocationCount() {
        FacilityFilterCommand ffc =  new FacilityFilterCommand()
        int startCount = facilityService.findActiveFacilities(ffc).count
        enableHibernateStatistic()
        assert facilityService.findActiveFacilities(ffc).count == startCount
        assert printStatAndGetQueryCount() == 0
    }

    void enableHibernateStatistic() {
        Statistics stats = sessionFactory.statistics;
        if(!stats.statisticsEnabled) {stats.setStatisticsEnabled(true)}
        stats.clear()
    }

    int printStatAndGetQueryCount() {
        Statistics stats = sessionFactory.getStatistics()
        double queryCacheHitCount  = stats.getQueryCacheHitCount();
        double queryCacheMissCount = stats.getQueryCacheMissCount();
        double queryCacheHitRatio = (queryCacheHitCount / ((queryCacheHitCount + queryCacheMissCount) ?: 1))
        int result =stats.queryExecutionCount
        println """
######################## Hibernate Stats ##############################################
Transaction Count:${stats.transactionCount}
Flush Count:${stats.flushCount}
Total Collections Fetched:${stats.collectionFetchCount}
Total Collections Loaded:${stats.collectionLoadCount}
Total Entities Fetched:${stats.entityFetchCount}
Total Entities Loaded:${stats.entityFetchCount}
Total Queries:${stats.queryExecutionCount}
queryCacheHitCount:${queryCacheHitCount}
queryCacheMissCount:${queryCacheMissCount}
queryCacheHitRatio:${queryCacheHitRatio}
######################## Hibernate Stats ##############################################
"""
        stats.clear()

        return result
    }

}
