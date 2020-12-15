package com.matchi.idrottonline.commands

import com.matchi.idrottonline.IdrottOnlineSportMapper
import grails.validation.Validateable

@Validateable()
class SportCommand {

    String optionType = OptionType.UPDATE.toString()
    String isActive
    String sportID

    static constraints = {
        optionType matches: OptionType.UPDATE.toString()
        isActive blank: false, inList: ["true", "false"]
        sportID blank: false, inList: [IdrottOnlineSportMapper.SportId.TENNIS.toString(),
                                       IdrottOnlineSportMapper.SportId.BADMINTON.toString(),
                                       IdrottOnlineSportMapper.SportId.SQUASH.toString(),
                                       IdrottOnlineSportMapper.SportId.TABLETENNIS.toString()]
    }

    enum OptionType {
        UPDATE("Update")

        OptionType(String value) { this.value = value }
        private final String value
        public String toString() { return value }
    }
}
