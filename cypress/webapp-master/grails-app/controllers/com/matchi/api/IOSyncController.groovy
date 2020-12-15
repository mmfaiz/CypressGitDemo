package com.matchi.api

import com.matchi.IOSync
import org.apache.http.HttpStatus

class IOSyncController {

    def update() {
        log.debug("Update ${request.JSON}")

        def json = request.JSON
        def ioSync

        if (json.customerId) {
            ioSync = IOSync.findAllByBatchIdAndCustomerId(json.batchId, json.customerId)
        } else {
            ioSync = IOSync.findAllByBatchId(json.batchId)
        }

        if (!ioSync) {
            render status: HttpStatus.SC_NOT_FOUND
            return
        }

        ioSync.each {
            log.info("Incoming IOSync error on batch ${it?.batchId}: \"${json?.message}\"")

            it.message = json?.message
            it.status  = json?.status
            it.save()
        }

        render status: HttpStatus.SC_OK
    }
}
