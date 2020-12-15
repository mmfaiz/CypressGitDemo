package com.matchi.export

class CustomerExportHandler extends ExportHandler {

    @Override
    def getExportTitles() {
        return customerColumns().collect { it.title }
    }

    @Override
    def getExportData() {
        return customerColumns().collect { it.attr }
    }

    static def customerColumns() {
        return [[ title: "Nummer", attr: "number" ],
                [ title: "Efternamn", attr: "lastname" ],
                [ title: "Förnamn", attr: "firstname" ],
                [ title: "Företagsnamn", attr: "companyname" ],
                [ title: "Kontaktperson", attr: "contact"],
                [ title: "Typ", attr: "type" ],
                [ title: "Epost", attr: "email" ],
                [ title: "Telefon", attr: "telephone" ],
                [ title: "Mobil", attr: "cellphone" ],
                [ title: "Adress1", attr: "address1" ],
                [ title: "Adress2", attr: "address2" ],
                [ title: "Postnr", attr: "zipcode" ],
                [ title: "Stad", attr: "city" ],
                [ title: "Person-/Orgnr", attr: "securityNumber" ],
                [ title: "Webbadress", attr: "web"],
                [ title: "Faktura-adress1", attr: "invoiceAddress1" ],
                [ title: "Faktura-adress2", attr: "invoiceAddress2" ],
                [ title: "Faktura-stad", attr: "invoiceCity" ],
                [ title: "Faktura-postnr", attr: "invoiceZipcode" ],
                [ title: "Faktura-kontakt", attr: "invoiceContact" ],
                [ title: "Faktura-tele", attr: "invoiceTelephone" ],
                [ title: "Faktura-epost", attr: "invoiceEmail" ],
                [ title: "Anteckning", attr: "notes" ]]
    }
}
