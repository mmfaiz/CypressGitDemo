package com.matchi.external

class BoxnetTransaction {

    String tid
    String kundId //ExternalId
    String produktnr
    String titel
    String betalsatt
    String debPris
    String momssats
    String kassa
    String kassakod
    String kvittonr

    Date syncedDate

    Date dateCreated
    Date lastUpdated

    static constraints = {
        tid(nullable: true)
        kundId(nullable: true)
        produktnr(nullable: true)
        titel(nullable: true)
        betalsatt(nullable: true)
        debPris(nullable: true)
        momssats(nullable: true)
        kassa(nullable: true)
        kassakod(nullable: true)
        kvittonr(nullable: true)
        syncedDate(nullable: true)
    }
}
