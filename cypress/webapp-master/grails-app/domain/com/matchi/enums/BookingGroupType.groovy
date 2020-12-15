package com.matchi.enums

public enum BookingGroupType {
    DEFAULT('Bokning'), NOT_AVAILABLE('Ej tillgänglig'), SUBSCRIPTION("Abonnemang"), TRAINING('Träning'), COMPETITION("Tävling"), ACTIVITY('Aktivitet'), BLOCKED('Blockerad'), PRIVATE_LESSON('Private Lesson')

    String name

    BookingGroupType(String name) {
        this.name = name
    }

    static list() {
        return [NOT_AVAILABLE, SUBSCRIPTION, TRAINING, COMPETITION, ACTIVITY, BLOCKED, PRIVATE_LESSON]
    }
}