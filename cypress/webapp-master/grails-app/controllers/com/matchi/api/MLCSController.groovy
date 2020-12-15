package com.matchi.api


import grails.converters.JSON
import grails.validation.Validateable
import org.joda.time.Interval
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

class MLCSController {
    public static final DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime()
    private static final String ERR_CODE_KEY        = "code"
    private static final String ERR_MESSAGE_KEY     = "message"
    private static final String ERR_INVALID_REQUEST = "INVALID_REQUEST"

    def facilityService
    def mlcsService

    def schedule() {
        RetrieveScheduleCommand cmd = new RetrieveScheduleCommand()

        if(params.from && params.to) {
            cmd.from = new LocalDate(params.from)
            cmd.to = new LocalDate(params.to)
        }

        log.debug("Retrieved MLCS schedule requests: ${cmd} ${params.from}")


        def facility = facilityService.getFacility(request.facilityId)
        def result = [:]

        if(cmd.validate()) {
            result = mlcsService.buildScheduleResponse(facility, cmd.toInterval())
        } else {
            result = buildErrorResponse(cmd.buildErrorMessages())
        }

        mlcsService.updateFacilityMLCSHeartBeat(facility)

        render result as JSON
    }

    /**
     * Build a successfull MLCS booking schedule
     * @param cmd The retrieving schedule request
     * @return Response (Map)
     */
    private void buildErrorResponse(def errorMessages) {
        def result = [:]
        result.put(ERR_CODE_KEY, ERR_INVALID_REQUEST)

        response.status = 400
        result.put(ERR_MESSAGE_KEY, errorMessages.join(", "))
    }

    private void authorize() {
        def authString = request.getHeader('Authorization')
        if (authString) {
            def encodedPair = authString - 'Basic '
            def decodedPair = new String(encodedPair.decodeBase64());
            def credentials = decodedPair.split(':')

            log.info("AUTHORIZE: ${authString}")
            log.info(credentials[0])
        }
    }

}

@Validateable(nullable = true)
class RetrieveScheduleCommand {
    LocalDate from
    LocalDate to


    def buildErrorMessages() {
        def errorMessages = []
        errors.each {
            errorMessages = it.allErrors.collect { error ->
                errorMessages << error
            }
        }
        errorMessages
    }

    /**
     * Converts to interval and adds one day to end date (since call is end inclusive)
     * @return
     */
    def toInterval() {
        new Interval(from.toDateTimeAtStartOfDay(), to.plusDays(1).toDateTimeAtStartOfDay())
    }

    @Override
    public String toString() {
        return "from: ${from} to: ${to}"
    }
}
