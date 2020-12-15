package com.matchi.idrottonline

import org.codehaus.groovy.grails.commons.GrailsApplication

class IdrottOnlineSettings {

    String url
    String username
    String password
    String contentTypeString = "application/json;charset=UTF-8"
    String acceptString = "application/json;charset=UTF-8"
    String appId
    final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    final String DATE_OF_BIRTH_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    public IdrottOnlineSettings(GrailsApplication grailsApplication){
        url = grailsApplication.config.matchi.io.url
        username = grailsApplication.config.matchi.io.username
        password = grailsApplication.config.matchi.io.password
        appId = grailsApplication.config.matchi.io.applicationId
    }
}
