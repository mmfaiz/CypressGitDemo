package com.matchi.facility

import com.matchi.GenericController

class FacilitySubscriptionImportController extends GenericController {

    static scope = "prototype"

    def importSubscriptionService
    def s3FileService
    def excelImportManager

    def index() {
        redirect(action: "import")
    }

    def importFlow = {
        entry {
            action {
                log.debug("Flow action entry")
                flow.facility = getUserFacility()
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
                    flow.error = ""
                } catch (Exception e) {
                    log.debug(e.toString())
                    flow.error = "Felaktig fil"
                    return Exception()
                }

                flow.validColumns = validFileColumns()

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "columns"
            on("return").to "upload"
            on("error").to "upload"
            on("Exception").to "upload"
        }
        columns {
            log.debug("Flow view columns")
            on("cancel").to "cancel"
            on("previous").to "upload"
            on("submit").to "getSubscriptionData"
        }
        getSubscriptionData {
            action {
                log.debug("Flow action get subscriptions data")
                flow.subscriptionData = importSubscriptionService.parseSubscriptionData(excelImportManager.parseSubscriptionFileContents(s3FileService.downloadTemporaryFile(flow.fileName)))

                flow.persistenceContext.clear()
                success()
            }
            on("return").to "columns"
            on("error").to "columns"
            on("success").to "confirm"
        }
        confirm {
            log.info("Flow view confirm and create subscriptions")
            on("cancel").to "cancel"
            on("previous").to "columns"
            on("submit").to "importSubscriptions"
        }
        importSubscriptions {
            action {
                log.debug("Flow action import subsciptions on ${flow.subscriptionData.size()}")
                flow.importedInfo = importSubscriptionService.importSubscriptions(flow.subscriptionData)

                flow.persistenceContext.clear()
                success()
            }
            on("return").to "confirm"
            on("error").to "confirm"
            on("success").to "confirmation"
        }
        confirmation()

        cancel {
            redirect(controller: "facilitySubscription", action: "index")
        }
    }


    private def validFileColumns() {
        return [[title: "Kund-/Medlemsnr", desc: ""],
                [title: "Dag i veckan", desc: ""],
                [title: "Tid", desc: ""],
                [title: "Bana", desc: ""],
                [title: "Startdatum", desc: ""],
                [title: "Slutdatum", desc: ""] ]
    }
}
