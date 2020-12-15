package com.matchi.excel

import jxl.Sheet
import jxl.Workbook
import jxl.WorkbookSettings
import java.text.SimpleDateFormat
import jxl.DateCell

class ExcelImportManager {

    static def startRow = 1
    static def startCol = 0

    Workbook workbook
    WorkbookSettings ws

    public ExcelImportManager() {
        ws = new WorkbookSettings()
        ws.setEncoding("ISO-8859-1")
        //ws.setEncoding("UTF-8")
    }

    def retrieveColumnTitles(def file) {
        return parseFileTitles(file)
    }

    def parseFileTitles(File file) {
        workbook = Workbook.getWorkbook(file, ws)
        Sheet sheet = workbook.getSheet(0)

        def titles = getTitles(sheet)
        workbook.close()

        return titles
    }

    def List<Map> parseSubscriptionFileContents(File file) {
        workbook = Workbook.getWorkbook(file, ws)
        Sheet sheet = workbook.getSheet(0)

        def result = getSubscriptionContents(sheet)
        workbook.close()

        return result
    }

    def List<Map> parseCustomerFileContents(File file) {
        workbook = Workbook.getWorkbook(file, ws)
        Sheet sheet = workbook.getSheet(0)

        def result = getCustomerContents(sheet)
        workbook.close()

        return result
    }

    def List<Map> parseAccessCodeFileContents(File file) {
        workbook = Workbook.getWorkbook(file, ws)
        Sheet sheet = workbook.getSheet(0)

        def result = getAccessCodeContents(sheet)
        workbook.close()

        return result
    }

    private static def getTitles(Sheet sheet) {
        def result = []

        for (int col = startCol; col < sheet.columns; col++) {
            def cellContents = sheet.getCell(col, startCol).contents
            if (cellContents != null) {
                result << recodeToTheValidEncoding(cellContents)
            }
        }

        return result
    }

    private static String recodeToTheValidEncoding(String cellContents) {
        if (cellContents) {
            cellContents = cellContents.replaceAll("\\p{C}", "")
        }
        cellContents
    }

    private static def List<Map> getSubscriptionContents(Sheet sheet) {

        def result = []

        for (int row = startRow; row < sheet.rows; row++) {

            int column = 0 // column read index

            def customernumber = recodeToTheValidEncoding(sheet.getCell(column++, row).contents)
            def weekDay = recodeToTheValidEncoding(sheet.getCell(column++, row).contents)
            def time = recodeToTheValidEncoding(sheet.getCell(column++, row).contents)
            def court = recodeToTheValidEncoding(sheet.getCell(column++, row).contents)
            def dateFrom   = recodeToTheValidEncoding(new SimpleDateFormat("yyyy-MM-dd").format(((DateCell) sheet.getCell(column++, row)).getDate()))
            def dateTo = recodeToTheValidEncoding(new SimpleDateFormat("yyyy-MM-dd").format(((DateCell) sheet.getCell(column++, row)).getDate()))

            // check that we got a top and a type
            if (customernumber == null || customernumber == "") {
                // do nothing
            } else {

                Map subConfMap = [
                        customernumber: customernumber ?: "",
                        weekDay: weekDay ?: "",
                        time: time ?: "",
                        court: court ?: "",
                        dateFrom: dateFrom ?: "",
                        dateTo: dateTo ?: "" ]

                if(subConfMap)
                    result << subConfMap
            }
        }
        return result
    }

    private static def List<Map> getCustomerContents(Sheet sheet) {

        def result = []

        for (int row = startRow; row < sheet.rows; row++) {

            int column = 0 // column read index

            def number = sheet.getCell(column++, row)?.contents
            def membership = sheet.getCell(column++, row)?.contents
            def membershiptype = sheet.getCell(column++, row)?.contents
            def lastname = sheet.getCell(column++, row)?.contents
            def firstname = sheet.getCell(column++, row)?.contents
            def contact   = sheet.getCell(column++, row)?.contents
            def email = sheet.getCell(column++, row)?.contents
            def telephone = sheet.getCell(column++, row)?.contents
            def cellphone = sheet.getCell(column++, row)?.contents
            def address1 = sheet.getCell(column++, row)?.contents
            def address2 = sheet.getCell(column++, row)?.contents
            def zipcode = sheet.getCell(column++, row)?.contents
            def city = sheet.getCell(column++, row)?.contents
            def securityNumber = sheet.getCell(column++, row)?.contents
            def type = sheet.getCell(column++, row)?.contents
            def web = sheet.getCell(column++, row)?.contents
            def invoiceAddress1 = sheet.getCell(column++, row)?.contents
            def invoiceAddress2 = sheet.getCell(column++, row)?.contents
            def invoiceZipcode = sheet.getCell(column++, row)?.contents
            def invoiceCity = sheet.getCell(column++, row)?.contents
            def invoiceContact = sheet.getCell(column++, row)?.contents
            def invoiceTelephone = sheet.getCell(column++, row)?.contents
            def invoiceEmail = sheet.getCell(column++, row)?.contents
            def notes = sheet.getCell(column++, row)?.contents
            def guardianName = sheet.getCell(column++, row)?.contents
            def guardianEmail = sheet.getCell(column++, row)?.contents
            def guardianTelephone = sheet.getCell(column++, row)?.contents
            def guardianName2 = sheet.getCell(column++, row)?.contents
            def guardianEmail2 = sheet.getCell(column++, row)?.contents
            def guardianTelephone2 = sheet.getCell(column++, row)?.contents
            def groups = sheet.getCell(column++, row)?.contents
            def family = sheet.getCell(column++, row)?.contents
            def country = sheet.getCell(column++, row)?.contents

            Map customerConfMap = [
                    number: number ?: '',
                    membership: membership ? membership.toUpperCase() : '',
                    membershiptype: membershiptype ?: '',
                    lastname: lastname ? lastname.capitalize() : '',
                    firstname: firstname ? firstname.capitalize() : '',
                    contact: contact? contact : '',
                    email: email ? email.toLowerCase() : '',
                    telephone: telephone ?: '',
                    cellphone: cellphone ?: '',
                    address1: address1 ? address1.capitalize() : '',
                    address2: address2 ? address2.capitalize() : '',
                    zipcode: zipcode ?: '',
                    city: city ? city.capitalize() : '',
                    securityNumber: securityNumber ?: '',
                    type: type ? type.toUpperCase() : '',
                    web: web ? web : '',
                    invoiceAddress1: invoiceAddress1 ? invoiceAddress1.capitalize() : '',
                    invoiceAddress2: invoiceAddress2 ? invoiceAddress2.capitalize() : '',
                    invoiceCity: invoiceCity ? invoiceCity.capitalize() : '',
                    invoiceZipcode: invoiceZipcode ? invoiceZipcode.capitalize() : '',
                    invoiceContact: invoiceContact ? invoiceContact.capitalize() : '',
                    invoiceTelephone: invoiceTelephone ?: '',
                    invoiceEmail: invoiceEmail ?: '',
                    notes: notes ?: '',
                    guardianName: guardianName ?: '',
                    guardianEmail: guardianEmail ?: '',
                    guardianTelephone: guardianTelephone ?: '',
                    guardianName2: guardianName2 ?: '',
                    guardianEmail2: guardianEmail2 ?: '',
                    guardianTelephone2: guardianTelephone2 ?: '',
                    groups: groups ?: '',
                    family: family ?: '',
                    country: country ?: '']

            if(customerConfMap)
                result << customerConfMap
        }
        return result
    }

    private static List<Map> getAccessCodeContents(Sheet sheet) {
        def result = []

        for (int row = startRow; row < sheet.rows; row++) {
            int column = 0 // column read index

            def validFrom = sheet.getCell(column++, row).contents
            def validTo = sheet.getCell(column++, row).contents
            def content = sheet.getCell(column++, row).contents
            def courts = sheet.getCell(column++, row).contents

            result <<  [
                    validFrom: validFrom ?: "",
                    validTo: validTo ?: "",
                    content: content ?: "",
                    courts: courts ?: ""
            ]
        }
        return result
    }
}
