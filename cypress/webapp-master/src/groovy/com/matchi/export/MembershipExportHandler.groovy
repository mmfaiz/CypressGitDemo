package com.matchi.export

class MembershipExportHandler extends ExportHandler {
    @Override
    def getExportTitles() {
        return membershipColumns().collect { it.title }
    }

    @Override
    def getExportData() {
        return membershipColumns().collect { it.title }
    }

    static membershipColumns() {
        return [[ title: "Medlemstyp", attr: "type" ],
                [ title: "Medlemskapstatus", attr: "status" ]]
    }
}
