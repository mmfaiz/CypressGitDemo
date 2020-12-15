package com.matchi.idrottonline

import grails.validation.Validateable

@Validateable()
abstract class IdrottOnlineCommand {

    String applicationId = ""
    abstract void addSyncResultInfo(IdrottOnlineSyncResult syncResult)
    abstract String toJSON()
}




