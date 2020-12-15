package com.matchi.facility

import com.matchi.Court
import com.matchi.Facility
import com.matchi.FacilityAccessCode
import com.matchi.GenericController
import grails.validation.Validateable
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.ListUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

class FacilityAccessCodeController extends GenericController {

    def excelExportManager
    def facilityAccessCodeService

    def index() {
        Facility facility = getUserFacility()
        def codes = facilityAccessCodeService.getCodesAndCourts(facility)
        def courts = Court.available(facility).list()
        def cmd = new UpdateAccessCodesCommand()

        codes.each {
            cmd.codes.add(new AccessCodeCommand(id: it.id, code: it.content,
                    validFromDate: new DateTime(it.validFrom).toLocalDate(),
                    validFromTime: new DateTime(it.validFrom).toLocalTime(),
                    validToDate: new DateTime(it.validTo).toLocalDate(),
                    validToTime: new DateTime(it.validTo).toLocalTime(),
                    courts: it.courtIds))
        }

        if(codes.isEmpty() && params.add) {
            cmd.codes.add(new AccessCodeCommand(validFromDate: new LocalDate(), validFromTime: new LocalTime("00:00"), courts: []))
        }

        [cmd: cmd, courts: courts, activeFacility: facility]
    }

    def update(UpdateAccessCodesCommand cmd) {
        def courts = Court.available(getUserFacility()).list()

        if(!cmd.validate()) {
            render(view: "index", model: [cmd: cmd, courts: courts])
            return;
        }

        clear()

        cmd.codes.each {
            def code = new FacilityAccessCode()
            code.facility = getUserFacility()
            code.content = it.code
            code.validFrom = it.validFromDate.toDateTime(it.validFromTime).toDate()
            code.validTo   = it.validToDate.toDateTime(it.validToTime).toDate()
            code.active = true
            code.courts = it.courts?.collect { Court.findById(it) }
            code.save(failOnError: true);
        }

        flash.message = message(code: "facilityAccessCode.update.success")
        redirect(action: "index")
    }

    private void clear() {
        def codes = FacilityAccessCode.facilityAccessCodes(getUserFacility()).list()
        codes*.delete();
    }

    def export() {
        def facility = getUserFacility()
        def codeIds = params.list("ids").collect {Long.parseLong(it)}.unique()
        def codes = facilityAccessCodeService.getCodesAndCourts(getUserFacility(), codeIds)
        def date = new DateTime().toString("yyyy-MM-dd_HHmmss")

        log.info "Exporting ${codes.size()} access codes"

        response.contentType = "application/vnd.ms-excel"
        response.setHeader("Content-disposition",
                "attachment; filename=passerkoder_${facility.shortname}_${date}.xls")

        excelExportManager.exportAccessCodes(codes, response.outputStream)
    }

    def delete() {
        def facility = getUserFacility()

        FacilityAccessCode.findAllByFacilityAndIdInList(
                facility, params.list("ids").collect {Long.parseLong(it)}).each {
            it.delete()
        }

        flash.message = message(code: "facilityAccessCode.delete.success")

        redirect(action: "index")
    }

    def deleteUsed() {
        FacilityAccessCode.invalidAccessCode(getUserFacility()).list().each {
            it.delete()
        }

        flash.message = message(code: "facilityAccessCode.delete.success")

        redirect(action: "index")
    }
}

@Validateable(nullable = true)
class UpdateAccessCodesCommand implements Serializable {
    List<AccessCodeCommand> codes = [].withLazyDefault {new AccessCodeCommand()}

    void clearNullRows() {
        codes.removeAll { it == null }
    }

    static constraints = {
        codes(validator: {val, obj ->

            // remove null rows
            obj.clearNullRows()

            def result = true

            val.each {
                if(!it.validate()) {
                    result = false
                }
            }

            return result
        })
    }
}

@Validateable(nullable = true)
class AccessCodeCommand {
    Long id
    String code
    LocalDate validFromDate
    LocalDate validToDate
    LocalTime validFromTime
    LocalTime validToTime
    List<Long> courts

    static constraints = {
        id(nullable: true)
        code(blank: false)
        validFromDate(nullable: false)
        validFromTime(nullable: false)
        validToDate(nullable: false, validator:
                { val, obj ->
                    return val?.toDateTime(obj.validToTime).isAfter(obj.validFromDate?.toDateTime(obj.validFromTime))
                }
        )
        validToTime(nullable: false)
        courts(nullable: false)
    }
}

