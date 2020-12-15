package com.matchi

enum CourtTypeEnum {
    PADEL_SIZE([5L], ["Double", "Single", "JuniorMini"], true, true),
    TENNIS_SIZE([1L], ["Standard", "Single", "Mini"], true, true)

    List<Long> sportsIds
    List<String> options
    boolean required
    boolean affectMultiplePlayersPrice

    CourtTypeEnum(List<Long> sportsIds, List<String> options, boolean required, boolean affectMultiplePlayersPrice = false) {
        this.sportsIds = sportsIds
        this.options = options
        this.required = required
        this.affectMultiplePlayersPrice = affectMultiplePlayersPrice
    }

    static List<CourtTypeEnum> getBySport(Sport sport) {
        return CourtTypeEnum.findAll { CourtTypeEnum it -> it.sportsIds.contains(sport.id) }
    }

    static List<CourtTypeEnum> getByNotSport(Sport sport) {
        return CourtTypeEnum.findAll { CourtTypeEnum it ->
            !it.sportsIds.contains(sport.id)
        }
    }

    static List<CourtTypeEnum> getByMultiplePlayersPrice() {
        return CourtTypeEnum.findAll { CourtTypeEnum it -> it.affectMultiplePlayersPrice }
    }
}