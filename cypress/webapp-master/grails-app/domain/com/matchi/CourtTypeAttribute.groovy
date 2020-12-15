package com.matchi

class CourtTypeAttribute implements Serializable {
    static hasOne = [court: Court]

    CourtTypeEnum courtTypeEnum
    String value

    static constraints = {
        court nullable: false
        courtTypeEnum nullable: false
        value nullable: true
    }

    static mapping = {
        version false
        id composite:['court', 'courtTypeEnum']
    }
}