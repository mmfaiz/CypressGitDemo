package com.matchi.watch

import com.matchi.Court
import com.matchi.Sport

/**
 * Represents a user that wants notifications when a free time emerges
 * on a specific date on (optional) a specific court
 */
class SlotWatch extends ObjectWatch {

    Court court
    Sport sport

    static constraints = {
        court(nullable: true)
        sport(nullable: true)
    }

    static mapping = {
        discriminator "slot_watch"
    }
}
