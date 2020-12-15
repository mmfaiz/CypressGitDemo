package com.matchi

import java.text.ParseException
import org.apache.commons.lang.StringUtils
import org.springframework.util.StopWatch

/**
 * @author Sergei Shushkevich
 */
class ImportAccessCodeService {
    static transactional = true

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

    def springSecurityService

    def parseAccessCodeData(List data) {
        data.collect {
            [cmd: [validFrom: it.validFrom, validTo: it.validTo, content: it.content, courts: it.courts],
                    error: StringUtils.isBlank(it.content) || !isValidDate(it.validFrom) || !isValidDate(it.validTo)]
        }
    }

    def importAccessCodes(List accessCodeData) {
        User currentUser = springSecurityService.currentUser
        Facility facility = currentUser?.facility
        List failed = []
        List imported = []

        StopWatch stopWatch = new StopWatch("Import access codes")

        log.info("Starting import of ${accessCodeData?.size()} access codes on ${facility.name}")
        stopWatch.start()

        if (accessCodeData?.size() > 0 && facility) {
            accessCodeData.eachWithIndex { params, i ->
                if(i % 100 == 0) {
                    log.debug("Reached row ${i}")
                }

                List courts = []
                def cmd = params.cmd

                cmd.courts.split(',').collect { it.trim() }.each {
                    Court tmp = Court.findByNameLikeAndFacilityAndArchived(it,facility,false)
                    if(tmp) courts.add(tmp)
                }

                def fm = new FacilityAccessCode(content: cmd.content, active: true, facility: facility, courts: courts)
                if (cmd.validFrom) {
                    try {
                        fm.validFrom = Date.parse(DATE_FORMAT, cmd.validFrom)
                    } catch (ParseException e) {
                        fm.errors.rejectValue("validFrom", "date.format.error",
                                [cmd.validFrom, DATE_FORMAT] as Object[], "")
                    }
                }
                if (cmd.validTo) {
                    try {
                        fm.validTo = Date.parse(DATE_FORMAT, cmd.validTo)
                    } catch (ParseException e) {
                        fm.errors.rejectValue("validTo", "date.format.error",
                                [cmd.validTo, DATE_FORMAT] as Object[], "")
                    }
                }

                if (!fm.hasErrors() && fm.save()) {
                    imported << fm
                } else {
                    failed << fm
                }
            }
        }
        stopWatch.stop()

        log.info("Access code import finished with ${imported.size()} created and ${failed.size()} failed in ${stopWatch.totalTimeSeconds} sec (${stopWatch.totalTimeSeconds / (imported.size() + failed.size())} / code)")
        return [imported: imported, failed: failed]
    }

    private Boolean isValidDate(String date) {
        if (date) {
            try {
                Date.parse(DATE_FORMAT, date)
            } catch (ParseException e) {
                return false
            }
        }

        return true
    }
}
