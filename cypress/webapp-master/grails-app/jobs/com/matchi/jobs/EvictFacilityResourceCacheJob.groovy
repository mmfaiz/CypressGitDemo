package com.matchi.jobs

import org.springframework.util.StopWatch

class EvictFacilityResourceCacheJob {
    def grailsCacheManager

    static triggers = {
        simple startDelay: 3600000L, repeatInterval: 3600000L // execute job every hour
    }

    def group = "EvictFacilityResourceCacheJob"

    def execute() {
        log.info("Running EvictFacilityResourceCacheJob...")

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        grailsCacheManager.getCache("facilities").clear()
        grailsCacheManager.getCache("queryFacilities").clear()
        grailsCacheManager.getCache("apiExtFacilities").clear()

        stopWatch.stop()
        log.info("Finished EvictFacilityResourceCacheJob in ${stopWatch.totalTimeMillis} ms")
    }

}
