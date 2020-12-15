package com.matchi.idrottonline

import com.matchi.Facility
import grails.async.Promises
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.transaction.NotTransactional

class IdrottOnlineCommandImporterService {

    IdrottOnlineSyncResultService idrottOnlineSyncResultService
    IdrottOnlineLoggerService idrottOnlineLoggerService

    @NotTransactional
    void importCommand(Facility facility, IdrottOnlineCommand command, IdrottOnlineSettings settings) {

        if (!command.validate()) {
            idrottOnlineLoggerService.warn("Validation errors (${facility?.name}): ${command?.errors}")
            return
        }
        IdrottOnlineSyncResult syncResult = new IdrottOnlineSyncResult()
        command.addSyncResultInfo(syncResult)
        logImportingMessage(syncResult, facility)
        String json = command.toJSON()
        
        Promises.task {
            RestResponse restResponse = sendRequest(settings, json)
            syncResult.statusCode = restResponse.status
            syncResult.status = restResponse.json.Status
            syncResult.batchId = restResponse.json.BatchId
            syncResult.message = restResponse.json?.Message
            idrottOnlineSyncResultService.handleResult(facility, syncResult)
        }
    }

    private RestResponse sendRequest(IdrottOnlineSettings settings, String json) {
        RestResponse restResponse = new RestBuilder().post(settings.url) {
            auth settings.username, settings.password
            body json
            contentType settings.contentTypeString
            accept settings.acceptString
        }
        restResponse
    }

    private void logImportingMessage(IdrottOnlineSyncResult syncResult, Facility facility) {
        if (!syncResult.customerIds.empty)
            idrottOnlineLoggerService.info("Importing validated persons for ${facility?.name} (${syncResult.customerIds.size()})")

        if (!syncResult.activityOccasionIds.empty)
            idrottOnlineLoggerService.info("Importing validated activities for (${facility?.name}) (${syncResult.activityOccasionIds.size()})")
    }
}

