package com.matchi.adyen


import com.matchi.TlsHttpBuilder
import grails.converters.JSON
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import org.apache.http.HttpResponse
import org.codehaus.groovy.grails.plugins.codecs.Base64Codec
import org.codehaus.groovy.grails.plugins.codecs.HexCodec

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.lang.reflect.UndeclaredThrowableException
import java.nio.charset.Charset
import java.security.SignatureException

class AdyenHttpService {

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256"
    private static final Charset C_UTF8 = Charset.forName("UTF8")
    def grailsApplication
    def userService
    def messageSource

    def request(String url, Map payload, Closure callback = null) throws AdyenException {
        def user = grailsApplication.config.adyen.user
        def password = grailsApplication.config.adyen.password
        def message = ""

        log.debug(payload as JSON)
        log.debug(url)

        def http = new TlsHttpBuilder(['TLSv1.2'])
        try {
            http.request(url, Method.POST, ContentType.JSON) {
                headers."Content-Type" = "application/json; charset=utf-8"
                headers."Authorization" = "Basic " + "${user}:${password}".bytes.encodeBase64().toString()

                if (payload) {
                    body = payload
                }

                response.success = { HttpResponse resp, json ->
                    log.debug("Success")
                    log.debug(json)
                    if (callback) {
                        callback.call(json)
                    }
                }

                response.failure = { HttpResponse resp, json ->
                    throw new AdyenException(json)
                }
            }
        }
        catch (UndeclaredThrowableException undeclaredThrowableException) {
            throw undeclaredThrowableException.getCause()
        }
    }

    // Payment url's
    def getAuthUrl() {
        return getPaymentUrl(AdyenService.AdyenProcessOperation.AUTHORISE)
    }

    def getAuth3DSUrl() {
        return getPaymentUrl(AdyenService.AdyenProcessOperation.AUTHORISE3D)
    }

    def getCaptureUrl() {
        return getPaymentUrl(AdyenService.AdyenProcessOperation.CAPTURE)
    }

    def getRefundUrl() {
        return getPaymentUrl(AdyenService.AdyenProcessOperation.REFUND)
    }

    def getCancelUrl() {
        return getPaymentUrl(AdyenService.AdyenProcessOperation.CANCEL)
    }

    def getPaymentUrl(AdyenService.AdyenProcessOperation operation) {
        return grailsApplication.config.adyen.paymentUrl + operation?.toString()?.toLowerCase()
    }

    // Reccuring url's
    def getRecurringDetailsUrl() {
        return getRecurringUrl(AdyenService.AdyenRecurringOperation.listRecurringDetails, false)
    }

    def getDisableRecurringUrl() {
        return getRecurringUrl(AdyenService.AdyenRecurringOperation.DISABLE)
    }

    def getRecurringUrl(AdyenService.AdyenRecurringOperation operation, boolean toLowerCase = true) {
        String operationFormatted = toLowerCase ? operation?.toString()?.toLowerCase() : operation?.toString()
        return grailsApplication.config.adyen.recurringUrl + operationFormatted
    }


    String generateMerchantSignature(SortedMap<String, String> map) {
        def result = ""

        byte[] hmackKey = HexCodec.decode(grailsApplication.config.adyen.hmac)

        def values = []
        def keys = []

        map.collect { k, v ->
            keys << k
            values << v
        }
        String joinedKeys = keys.join(":")
        String joinedValues = values.collect { escapeVal(it) }.join(":")
        String signingData = joinedKeys + ":" + joinedValues

        log.debug(signingData)

        // Create the signature and add it to the parameter map
        try {
            result = calculateHMAC(signingData, hmackKey)
        } catch (SignatureException e) {
            log.error(e.getMessage())
            return
        }
        log.debug("RESULT: ${result}")

        return result
    }

    private static String escapeVal(String val) {
        if (val == null) {
            return ""
        }
        return val.replace("\\", "\\\\").replace(":", "\\:")
    }

    private static String calculateHMAC(String data, byte[] key) throws SignatureException {
        try {
            // Create an hmac_sha256 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA256_ALGORITHM)

            // Get an hmac_sha256 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM)
            mac.init(signingKey)

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes(C_UTF8))

            // Base64-encode the hmac
            return Base64Codec.encode(rawHmac)

        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage())
        }
    }
}
