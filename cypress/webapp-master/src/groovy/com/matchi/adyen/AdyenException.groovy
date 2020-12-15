package com.matchi.adyen

import com.matchi.User
import grails.util.Holders
import org.springframework.context.NoSuchMessageException

class AdyenException extends Exception {

    def errorType
    def errorCode
    def message
    def englishMessage
    def status
    def pspReference
    def refusalReason
    def resultCode
    def additionalData
    def messageSource = Holders.applicationContext.getBean("messageSource")
    def userService = Holders.applicationContext.getBean("userService")

    AdyenException() {
    }

    AdyenException(def response) {
        super()
        errorType = response.errorType
        errorCode = response.errorCode
        message = response.message
        englishMessage = response.message
        status = response.status
        pspReference = response.pspReference
        refusalReason = response.refusalReason
        resultCode = response.resultCode
        additionalData = response.additionalData

        if (response.refusalReason && response.refusalReason != response.resultCode) {
            message = i18nMessage(response.refusalReason)
        } else if (response.refusalReason && response.refusalReason == response.resultCode) {
            message = response.additionalData["refusalReasonRaw"]
        } else if (response.errorCode) {
            message = i18nMessage(response.errorCode)
        }
    }

    AdyenException(String s) {
        super(s)
    }

    AdyenException(String s, Throwable throwable) {
        super(s, throwable)
    }

    AdyenException(Throwable throwable) {
        super(throwable)
    }

    @Override
    String getMessage() {
        return message
    }

    String getEnglishMessage() {
        return englishMessage ?: getMessage()
    }

    String printException() {
        return "Adyen error - ${errorType} (${errorCode}) (${resultCode}): ${message} - ${status} => ${pspReference}"
    }

    String i18nMessage(def code) {
        User user = userService.getLoggedInUser()
        String erorCode = code?.toLowerCase()?.replaceAll("\\s", "")?.replaceAll("[^a-zA-Z0-9]+", "")
        try {
            message = messageSource.getMessage("adyen.error.${erorCode}", null, user?.language ? new Locale(user.language) : Locale.default)
            englishMessage = messageSource.getMessage("adyen.error.${erorCode}", null, user?.language, Locale.ENGLISH)
        }
        catch(NoSuchMessageException ne) {
            message = code
            englishMessage = code
        }
        return message
    }
}
