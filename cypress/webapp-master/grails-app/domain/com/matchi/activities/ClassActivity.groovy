package com.matchi.activities

import com.matchi.GetActivitiesCommand
import com.matchi.MFile
import com.matchi.Sport

class ClassActivity extends Activity {
    private static final long serialVersionUID = 12L

    String teaser
    String terms
    String userMessageLabel
    String email
    MFile largeImage

    boolean archived = false
    boolean onlineByDefault = true
    Boolean membersOnly

    Integer price
    Integer maxNumParticipants
    Integer signUpDaysInAdvanceRestriction
    Integer signUpDaysUntilRestriction

    boolean cancelByUser = true     // indicates whether activity can be cancelled by user
    Integer cancelLimit             // min hours in advance when activity is still refundable

    Integer minNumParticipants
    Integer cancelHoursInAdvance

    GetActivitiesCommand JSONoptions = null
    Boolean notifyWhenSignUp = false
    Boolean notifyWhenCancel = false

    static transients = ['JSONoptions']

    static constraints = {
        teaser(nullable: true)
        terms(nullable: true)
        userMessageLabel(nullable: true, maxSize: 255)
        email nullable: true, email: true
        largeImage(nullable: true)
        archived(nullable: false)
        membersOnly(nullable: true)
        price(nullable: true)
        maxNumParticipants(nullable: true)
        signUpDaysInAdvanceRestriction(nullable: true)
        signUpDaysUntilRestriction(nullable: true)
        cancelByUser(nullable: false)
        cancelLimit(nullable: true, min: 0)
        minNumParticipants(nullable: true, min:1, validator: { val, obj ->
            (val <= obj.maxNumParticipants) ?: ['saveActivityCommand.minNumParticipants']
        })
        cancelHoursInAdvance(nullable: true)
        name nullable: false, blank:false
        notifyWhenSignUp(nullable: false)
        notifyWhenCancel(nullable: false)
    }

    static mapping = {
        teaser type: 'text'
        terms type: 'text'
        discriminator "class_activity"
    }

    def getOnlineOccasions() {
        occasions.findAll {
            it.availableOnline
        }
    }

    def getUpcomingOnlineOccasions() {
        occasions.findAll {
            it.isUpcomingOnlineOccasion()
        }
    }

    @Override
    Sport guessSport() {
        this.getUpcomingOnlineOccasions()?.first()?.bookings[0]?.slot?.court?.sport
    }

    @Override
    String[] getToMails() {
        return email ? [email] : [facility?.email]
    }

    @Override
    Integer getCancelLimitWithFallback() {
        cancelLimit != null ? cancelLimit : super.getCancelLimitWithFallback()
    }
}
