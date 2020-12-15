package com.matchi.facility

import com.matchi.GenericController

/**
 * @author Sergei Shushkevich
 */
class FacilityAccessCodeImportController extends GenericController {

    static scope = "prototype"

    def s3FileService
    def excelImportManager
    def importAccessCodeService

    def importFlow = {
        entry {
            action {
                log.debug("Flow action entry")
            }
            on("success").to("upload")
        }
        upload {
            log.info("Flow view upload")
            on("cancel").to "cancel"
            on("submit").to "getTitles"
        }
        getTitles {
            action {
                log.debug("Flow action getTitles")
                try {
                    flow.fileName = s3FileService.uploadTemporaryFile(request).name
                    flow.titles = excelImportManager.retrieveColumnTitles(s3FileService.downloadTemporaryFile(flow.fileName))
                    flow.validColumns = validFileColumns()
                    flow.error = ""
                } catch (Exception e) {
                    flow.error = message(code: "facilityAccessCodeImport.import.error")
                    return Exception()
                }

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "columns"
            on("return").to "upload"
            on("error").to "upload"
            on("Exception").to "upload"
        }
        columns {
            log.debug("Flow view setColumns")
            on("cancel").to "cancel"
            on("previous").to "upload"
            on("submit").to "getAccessCodeData"
        }
        getAccessCodeData {
            action {
                log.debug("Flow action get access code data")
                flow.accessCodeData = importAccessCodeService.parseAccessCodeData(excelImportManager.parseAccessCodeFileContents(s3FileService.downloadTemporaryFile(flow.fileName)))
            }
            on("return").to "columns"
            on("error").to "columns"
            on("success").to "confirm"
        }
        confirm {
            log.info("Flow view confirm and create access codes")
            on("cancel").to "cancel"
            on("previous").to "columns"
            on("submit").to "importAccessCodes"
        }
        importAccessCodes {
            action {
                flow.importedInfo = importAccessCodeService.importAccessCodes(flow.accessCodeData)
                flow.persistenceContext.clear()
                success()
            }
            on("return").to "confirm"
            on("error").to "confirm"
            on("success").to "confirmation"
        }
        confirmation()
        cancel {
            redirect(controller: "facilityAccessCode", action: "index")
        }
    }

    private validFileColumns() {
        return [[title: message(code: 'facilityAccessCodeImport.import.confirm.validFrom'), desc: ""],
                [title: message(code: 'facilityAccessCodeImport.import.confirm.validTo'), desc: ""],
                [title: message(code: 'facilityAccessCodeImport.import.confirm.code'), desc: ""],
                [title: message(code: 'facilityAccessCodeImport.import.confirm.courts'), desc: ""]]
    }
}
