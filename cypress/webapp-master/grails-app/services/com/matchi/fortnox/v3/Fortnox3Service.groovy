package com.matchi.fortnox.v3

import com.google.common.util.concurrent.RateLimiter
import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.TlsHttpBuilder
import com.matchi.facility.Organization
import grails.converters.JSON
import grails.util.Holders
import groovyx.net.http.ContentEncoding
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import org.springframework.beans.factory.annotation.Value

import java.lang.reflect.Field
/**
 * Service to communicate with Fortnox V3 API.
 *
 * @author Michael Astreiko
 */
class Fortnox3Service {

    static final String BASE_URL = "https://api.fortnox.se/3"
    static final String FORTNOX_API_DATE_FORMAT = 'yyyy-MM-dd'
    static final String NULL_VALUE = 'API_BLANK'

    static transactional = false

    def fortnoxContactService

    @Value('${matchi.fortnox.api.rateLimit}')
    Double permitsPerSecond

    private Map<String, RateLimiter> rateLimiters = new HashMap<>()

    void doGet(Facility facility, String path, def id = null, Map params = [:], Closure closure) {
        executeAPICall(Method.GET, facility, null, path, closure, id, params)
    }

    void doGetForOrganization(Organization organization, String path, def id = null, Map params = [:], Closure closure) {
        executeAPICall(Method.GET, null, organization, path, closure, id, params)
    }

    void doPut(Facility facility, String path,
               def id = null, String action = null, def requestBody = null, Closure closure) {
        executeAPICall(Method.PUT, facility, null, path, closure, id, [:], action, requestBody)
    }

    void doPutForOrganization(Organization organization, String path,
               def id = null, String action = null, def requestBody = null, Closure closure) {
        executeAPICall(Method.PUT, null, organization, path, closure, id, [:], action, requestBody)
    }

    void doPost(Facility facility, String path, def requestBody = null, Closure closure) {
        executeAPICall(Method.POST, facility ,null, path, closure, null, [:], null, requestBody)
    }

    void doPostForOrganization(Organization organization, String path, def requestBody = null, Closure closure) {
        executeAPICall(Method.POST, null, organization, path, closure, null, [:], null, requestBody)
    }

    void doPreview(Facility facility, String path, def id = null, String action = null, Closure closure) {
        executeAPICall(Method.GET, null, null, path, closure, id, null, action, null, ContentType.ANY)
    }

    private void executeAPICall(Method method, Facility facility, Organization organization, String path,
                                Closure closure, String id = null, Map params = [:],
                                String action = null, def requestBody = null, ContentType contentType = ContentType.JSON) {
        def httpUrl = "${BASE_URL}/${path}${id ? '/' + id : ''}${action ? '/' + action : ''}"
        if (params) {
            httpUrl += "?${params.collect { k, v -> "$k=$v" }.join('&')}"
        }

        def clientSecret = Holders.config.matchi.fortnox.api.v3.clientSecret
        def accessToken

        if (organization) {
            accessToken = organization.fortnoxAccessToken
        } else if (!facility) { // On interal invoicing, this applies
            accessToken = Holders.config.matchi.fortnox.api.v3.accessToken
        } else {
            accessToken = FacilityProperty.findByKeyAndFacility(
                    FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN.name(), facility)?.value
        }

        // The override access token should always win. Otherwise, we risk syncing with organization
        // instances of fortnox when in development mode
        def accessTokenOverride = Holders.config.matchi.fortnox.api.v3.override?.accessToken
        accessToken = accessTokenOverride ?: accessToken
        acquirePermit(accessToken)

        def http = new TlsHttpBuilder(['TLSv1.2'])
        http.contentEncoding = ContentEncoding.Type.COMPRESS
        http.encoder.setCharset("UTF-8")

        log.info("Sending Fortnox request: ${httpUrl}")
        http.request(httpUrl, method, contentType) {
            headers.'Client-Secret' = clientSecret
            headers.'Access-Token' = accessToken

            if (contentType == ContentType.JSON) {
                headers.'Content-Type' = 'application/json; charset=utf-8'
                headers.'Accept' = 'application/json'
            }

            if (requestBody) {
                body = requestBody
            }

            response.failure = { resp ->
                def errorMessage
                if (resp.status == 429) {
                    errorMessage = "Fortnox API calls limit exceeded for ${facility?.name}"
                } else {
                    String fortnoxError = resp.entity.content.text
                    def errorInformation = JSON.parse(fortnoxError).ErrorInformation
                    errorMessage = errorInformation.Message ?: errorInformation.message
                    log.error("Error when calling Fortnox URL: ${httpUrl}, body: ${requestBody}, error: ${fortnoxError}")
                }
                throw new FortnoxException(errorMessage)
            }

            response.success = { resp, json ->
                if (contentType == ContentType.JSON) {
                    closure.call(json)
                } else {
                    closure.call(resp)
                }
            }
        }
    }

    public String retrieveAccessToken(Facility facility, String authCode) {
        String accessToken = getAccessToken(authCode)
        if (accessToken) {
            def accessTokenKey = FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN.name()
            def facilityProperty = FacilityProperty.findOrCreateWhere(facility: facility, key: accessTokenKey)
            facilityProperty.value = accessToken
            facilityProperty.save(flush: true)

            def authCodeKey = FacilityProperty.FacilityPropertyKey.FORTNOX3_AUTHORIZATION_CODE.name()
            facilityProperty = FacilityProperty.findOrCreateWhere(facility: facility, key: authCodeKey)
            facilityProperty.value = authCode
            facilityProperty.save(flush: true)
        }
        return accessToken
    }

    public String retrieveAccessToken(Organization organization, String authCode) {
        String accessToken = getAccessToken(authCode)
        if (accessToken) {
            organization.fortnoxAccessToken = accessToken
            organization.fortnoxAuthCode = authCode
            organization.save(flush: true, failOnError: true)
        }
        return accessToken
    }

    private String getAccessToken(String authCode) {
        String clientSecret = Holders.config.matchi.fortnox.api.v3.clientSecret
        String accessToken = null
        if (authCode && clientSecret) {
            def http = new TlsHttpBuilder(['TLSv1.2'])
            http.contentEncoding = ContentEncoding.Type.COMPRESS
            http.encoder.setCharset("UTF-8")
            http.request(BASE_URL, Method.GET, ContentType.JSON) {
                headers.'Client-Secret' = clientSecret
                headers.'Authorization-Code' = authCode
                headers.'Content-Type' = 'application/json; charset=utf-8'
                headers.'Accept' = 'application/json'

                response.failure = { resp ->
                    log.error(resp.entity.content.text)
                }

                response.success = { resp, json ->
                    accessToken = json.Authorization.AccessToken
                }
            }
        }
        accessToken
    }

    /**
     *
     * @param domainObject
     * @param clazz
     * @return
     */
    Map getPossiblePropertiesMap(def domainObject, Class clazz) {
        Map propertiesMap = [:]
        clazz.getDeclaredFields().each { Field property ->
            if (domainObject[property.name] != null && property.getModifiers() == 2 && property.name != 'InvoiceRows') {
                if (domainObject[property.name] instanceof Date) {
                    propertiesMap[property.name] = domainObject[property.name].format(FORTNOX_API_DATE_FORMAT)
                } else {
                    propertiesMap[property.name] = domainObject[property.name]
                }
            }
        }
        return propertiesMap
    }

    private void acquirePermit(String accessToken) {
        if (!rateLimiters.containsKey(accessToken)) {
            rateLimiters[accessToken] = RateLimiter.create(permitsPerSecond)
        }
        rateLimiters[accessToken].acquire()
    }
}
