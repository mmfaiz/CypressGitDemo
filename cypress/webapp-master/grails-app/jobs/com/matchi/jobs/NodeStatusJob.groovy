package com.matchi.jobs

import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.PhoneUtil
import org.springframework.util.StopWatch

class NodeStatusJob {

    def mpcService
    def notificationService
    def smsService
    def messageSource

    static triggers = {
        // simple repeatInterval: 30000l // execute job once in 30 seconds
        cron name: "NodeStatusJob.trigger", cronExpression: "20 0/6 * * * ?" // every 6 min (at 20 seconds of a minute)
    }

    def group = "NodeStatusJob"
    def sessionRequired = true

    def execute() {
        log.info "Running NodeStatusJob"
        def nodes = mpcService.listNodes()

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        nodes.each {
            def facility = Facility.findById(it.externalId)
            if(facility != null) {
                FacilityProperty.MpcStatus mpcStatus = FacilityProperty.MpcStatus."${it.provider?.status ?: FacilityProperty.MpcStatus.NOT_OK.toString()}"

                if(facility.getMpcStatus() != mpcStatus.toString()) {
                    facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.MPC_STATUS, mpcStatus.toString())

                    notificationService.sendAdminNodeStatusChangedNotification(facility, mpcStatus)
                    String notificationPhoneNumber = PhoneUtil.convertToInternationalFormat(facility?.getMpcNotificationPhoneNumber())

                    if(notificationPhoneNumber) {
                        Locale mailLocale = new Locale(facility.language)
                        String message = messageSource.getMessage("nodeStatusJob.smsWarning.${mpcStatus.toString()}", [facility.name] as String[], mailLocale)
                        smsService.send(notificationPhoneNumber, message)
                    }
                }
            }
        }

        stopWatch.stop()
        log.info("Finished NodeStatusJob in ${stopWatch.totalTimeMillis} ms")
    }
}
