package com.matchi.idrottonline

import com.matchi.Facility
import com.matchi.IOSync
import org.apache.http.HttpStatus

class IdrottOnlineSyncResultService {

    IdrottOnlineLoggerService idrottOnlineLoggerService

    void handleResult(Facility facility, IdrottOnlineSyncResult syncResult) {
        if (syncResult.statusCode == HttpStatus.SC_OK && syncResult.status == IdrottOnlineSyncResult.SUCCESSFUL_JSON_STATUS) {
            handleSuccess(facility, syncResult)
        } else {
            handleError(facility, syncResult)
        }
    }

    private void handleSuccess(Facility facility, IdrottOnlineSyncResult syncResult) {
        idrottOnlineLoggerService.info("Import successful for ${facility.name}. BatchId: ${syncResult.batchId}")

        syncResult.customerIds.each { Long customerId ->
            IOSync.withTransaction {
                IOSync iosync = new IOSync()
                iosync.customerId = customerId
                iosync.batchId = syncResult.batchId
                iosync.save()
            }
        }

        syncResult.activityOccasionIds.each { Long activityId ->
            IOSync.withTransaction {
                IOSync iosync = new IOSync()
                iosync.activityOccasionId = activityId
                iosync.batchId = syncResult.batchId
                iosync.save()
            }
        }
    }

    private void handleError(Facility facility, IdrottOnlineSyncResult syncResult) {
        idrottOnlineLoggerService.error("Import failed for ${facility.name}. BatchId: ${syncResult.batchId}, status code: ${syncResult.statusCode}, message: ${syncResult.message}")
    }
}
