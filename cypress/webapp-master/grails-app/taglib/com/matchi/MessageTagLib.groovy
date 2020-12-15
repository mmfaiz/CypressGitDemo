package com.matchi

import com.matchi.messages.FacilityMessage
import com.matchi.messages.FacilityMessage.Channel
import com.matchi.membership.Membership
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.joda.time.LocalDate
import com.matchi.LocaleHelper.Country

class MessageTagLib {

    static final String WELCOME_COOKIE = "welcomeMessage"
    static final String NOTIFY_MATCHABLE_COOKIE = "matchableNotified"

    def memberService
    def userService
    def frontEndMessageService

    def flashMessage = {
        out << render(template: "/templates/messages/flashMessage")
    }

    def flashError = {
        out << render(template: "/templates/messages/flashError")
    }

    def errorMessage = { attrs, body ->
        out << render(template: "/templates/messages/errorMessage",
                model:[bean: attrs.bean])
    }

    // Bootstrap 3 alerts
    def b3StaticFlashMessage = {
            out << render(template: "/templates/messages/bootstrap3/staticFlashMessage")
    }

    def b3StaticFlashError = {
        out << render(template: "/templates/messages/bootstrap3/staticFlashError")
    }

    def b3StaticErrorMessage = { attrs, body ->
        out << render(template: "/templates/messages/bootstrap3/staticErrorMessage",
                model:[bean: attrs.bean])
    }

    def b3GlobalMessage = { attrs, body ->
        def messages = GlobalNotification.createCriteria().list {
            le("publishDate", new Date())
            ge("endDate", new Date())
            eq("isForFacilityAdmins", false)
            join "notificationText"
        }

        out << render(template: "/templates/messages/bootstrap3/globalNotification",
                model:[messages: messages])
    }

    def b3FrontEndMessage = { attrs, body ->
        User user = userService.getLoggedInUser()

        def messages = FrontEndMessage.createCriteria().list {
            le("publishDate", new Date().clearTime())
            ge("endDate", new Date().clearTime())
        }

        String language = user?.language

        if (!language) {
            language = GrailsWebRequest.lookup()?.getLocale().language
        }

        Country country = Country.getByLangKey(language)

        if (country) {
            messages = messages.findAll {
                return it.countries.any { it.iso == country.iso }
            }
        }

        Map<Long,String> htmlOutput = new HashMap<Long,String>()
        messages.each {
            htmlOutput.put(it.id, frontEndMessageService.getHTML(it))
        }

        out << render(template: "/templates/messages/bootstrap3/frontEndMessage",
                model:[messages: messages, htmlOutputs: htmlOutput])
    }

    def facilityAdminGlobalMessage = { attrs, body ->
        def messages = GlobalNotification.createCriteria().list {
            le("publishDate", new Date())
            ge("endDate", new Date())
            eq("isForUsers", false)
            join "notificationText"
        }

        out << render(template: "/templates/messages/globalNotification",
                model: [messages: messages])
    }

    def b3FacilityMessage = { attrs, body ->
        def facility = attrs.facility
        if (!facility) {
            throwTagError("Tag [b3FacilityMessage] is missing required attribute [facility]")
        }

        def messages = FacilityMessage.createCriteria().list {
            eq("facility", facility)
            eq("channel", Channel.NOTIFICATION)
            le("validFrom", new Date())
            ge("validTo", new Date())
            order("listPosition", "asc")
        }

        out << render(template: "/templates/messages/bootstrap3/facilityNotification",
                model: [messages: messages, facility: facility, cssClass: attrs.cssClass])
    }

    def welcomeMessage = { attrs, body ->

        def head = g.message(code: "message.welcome.header", args: [ userService.getLoggedInUser().firstname ])
        def text = g.message(code: "message.welcome.text")

        def cookie = g.cookie(name:  WELCOME_COOKIE)

        if(!cookie) {
            out << render(template: "/templates/messages/welcomeMessage",
                model:[ head:head, text:text])
        }
    }

    def profileMessage = { attrs, body ->

        User user = userService.getLoggedInUser()
        def profileComplete = user.completeProfileAttrs().every { user."$it" }
        def sportProfileComplete = true

        def link = new ApplicationTagLib().createLink([controller: 'userProfile', action: 'account', absolute: 'true']).toString()

        if(!profileComplete || !sportProfileComplete || !user.enabled) {
            out << render(template: "/templates/messages/profileMessage",
                model:[ profileComplete:profileComplete, sportProfileComplete:sportProfileComplete, enabled:user.enabled ])
        }
    }

    def completeProfileMessage = { attrs, body ->

        User user = userService.getLoggedInUser()
        def profileComplete = user.completeProfileAttrs().every { user."$it" }
        def sportProfileComplete = user.completeSportProfileAttrs().every { user."$it" }

        if(user.matchable && (!profileComplete || !sportProfileComplete)) {
            log.debug("Missing profile attrs")
            out << render(template: "/templates/messages/completeMatchingMessage",
                model:[ profileComplete:profileComplete, sportProfileComplete:sportProfileComplete  ])
        } else {
            log.debug("Do not miss any profile attrs")
        }
    }

    def notifyMatchable = { attr, body ->
        def user = userService.getLoggedInUser()

        def cookie = g.cookie(name: NOTIFY_MATCHABLE_COOKIE)

        if(!user.matchable && !cookie) {
            out << render(template:"/templates/matching/notify", model: [ user:user, cookieName: NOTIFY_MATCHABLE_COOKIE])
        }
    }

    def requestMembership = { attrs, body ->
        Membership membership = attrs.membership
        Facility facility = attrs.facility
        Customer customer = attrs.customer

        if (facility.recieveMembershipRequests) {
            def upcomingMembershipAvailable = memberService.isUpcomingMembershipAvailableForPurchase(membership)
            if (!membership?.isActive() || upcomingMembershipAvailable) {
                out << render(template:"/templates/messages/requestMembershipMessage",
                        model: [ facility: facility, membership: membership, customer: customer,
                                upcomingMembershipAvailable: upcomingMembershipAvailable ])
            }
        }
    }

    def requestTraining = { attrs, body ->
        User user = attrs.user
        Facility facility = attrs.facility

        out << render(template:"/templates/messages/requestTrainingMessage", model: [ facility: facility, user: user ])
    }
}
