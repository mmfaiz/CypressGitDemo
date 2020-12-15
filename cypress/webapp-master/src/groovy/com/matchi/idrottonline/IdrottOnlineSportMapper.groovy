package com.matchi.idrottonline

import com.matchi.Sport

class IdrottOnlineSportMapper {

    String getIdrottOnlineSportId(Sport sport){
        switch(sport.id) {
            case 1  : return SportId.TENNIS.toString()
            case 2  : return SportId.BADMINTON.toString()
            case 3  : return SportId.SQUASH.toString()
            case 4  : return SportId.TABLETENNIS.toString()
            default : return null
        }
    }

    enum SportId {
        TENNIS(39),
        BADMINTON(1),
        SQUASH(60),
        TABLETENNIS(8)

        SportId(int value) {
            this.value = value
        }
        private final int value

        public String toString() {
            return value
        }
    }
}
