package com.matchi.idrottonline

class IdrottOnlineLoggerService {

    private final String idrottOnlinePrefix = "IdrottOnline"

    void info(String message) {
        log.info(formatLogMessage(message))
    }

    void error(String message){
        log.error(formatLogMessage(message))
    }

    void warn(String message){
        log.warn(formatLogMessage(message))
    }

    void debug(String message) {
        log.debug(formatLogMessage(message))
    }

    private formatLogMessage(String message){
        idrottOnlinePrefix + " - " + message
    }
}
