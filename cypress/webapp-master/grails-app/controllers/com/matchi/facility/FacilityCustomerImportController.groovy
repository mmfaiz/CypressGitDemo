package com.matchi.facility

import com.matchi.Customer
import com.matchi.GenericController

class FacilityCustomerImportController extends GenericController {

    static scope = "prototype"

    def customerService
    def importCustomerService
    def s3FileService
    def excelImportManager
    def scheduledTaskService

    def index() {
        redirect(action: "import")
    }

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
                    flow.error = ""
                } catch (Exception e) {
                    flow.error = message(code: "facilityCustomerImport.import.error")
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
            on("submit").to "getCustomerData"
        }
        getCustomerData {
            action {
                log.debug("Flow action get customers data")
                flow.customersData = importCustomerService.parseCustomerData(
                        excelImportManager.parseCustomerFileContents(s3FileService.downloadTemporaryFile(flow.fileName)))
                flow.persistenceContext.clear()
            }
            on("return").to "columns"
            on("error").to "columns"
            on("success").to "confirm"
        }
        confirm {
            log.info("Flow view confirm and create customers")
            on("cancel").to "cancel"
            on("previous").to "columns"
            on("submit").to "importCustomers"
        }
        importCustomers {
            action {
                flow.importedInfo = importCustomerService.importCustomers(flow.customersData)

                def userId = getCurrentUser().id
                def customerIds = [] as HashSet
                customerIds.addAll(flow.importedInfo.imported)
                customerIds.addAll(flow.importedInfo.existing)

                flow.persistenceContext.clear()

                if (customerIds) {
                    scheduledTaskService.scheduleTask(
                            message(code: "facilityCustomerImport.import.taskName"), userId, null) {
                        customerIds.each { id ->
                            def c = Customer.get(id)
                            if (c) {
                                customerService.linkCustomerToUser(c)
                            }
                        }
                    }
                }

                success()
            }
            on("return").to "confirm"
            on("error").to "confirm"
            on("success").to "confirmation"
        }
        confirmation()

        cancel {
            redirect(controller: "facilityCustomer", action: "index")
        }
    }


    public final static List VALID_FILE_COLUMNS = [
                [title: "Kund-/Medlemsnr", desc: ""],
                [title: "Medlemskap", desc: "(Här krävs en korrekt status, dvs ACTIVE, CANCEL, PENDING)"],
                [title: "Medlemstyp", desc: "(Namn på medlemstypen exempelvis Junior, Senior. Måste finnas på anläggningen.)"],
                [title: "Efternamn", desc: ""],
                [title: "Förnamn/Namn (om företag)", desc: ""],
                [title: "Kontaktperson (om företag)", desc: ""],
                [title: "Email", desc: ""],
                [title: "Telefon", desc: ""],
                [title: "Mobil", desc: ""],
                [title: "Adress 1", desc: ""],
                [title: "Adress 2", desc: ""],
                [title: "Postnummer", desc: ""],
                [title: "Stad", desc: ""],
                [title: "Födelse-/org.nr", desc: ""],
                [title: "Kön/Typ (Man, Kvinna, Företag)", desc: ""],
                [title: "Webbadress", desc: ""],
                [title: "Faktura-adress 1", desc: ""],
                [title: "Faktura-adress 2", desc: ""],
                [title: "Faktura-postnr.", desc: ""],
                [title: "Faktura-stad", desc: ""],
                [title: "Faktura-kontakt", desc: ""],
                [title: "Faktura-telefon", desc: ""],
                [title: "Faktura-epost", desc: ""],
                [title: "Anteckningar", desc: ""],
                [title: "Målsmans namn", desc: ""],
                [title: "Målsmans mejl", desc: ""],
                [title: "Målsmans telefon", desc: ""],
                [title: "Målsmans namn 2", desc: ""],
                [title: "Målsmans mejl 2", desc: ""],
                [title: "Målsmans telefon 2", desc: ""],
                [title: "Grupper", desc: "(Kommaseparerad lista på gruppnamn. Måste finnas på anläggningen.)"],
                [title: "Familj", desc: "(Skall innehålla kontaktpersonens kundnr)"],
                [title: "Land", desc: "(Landskod på formatet ISO 3166-1 exempelvis SE, NO, PL. Anläggningens land används om tomt.)"]
    ].asImmutable()
}
