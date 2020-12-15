package com.matchi

import ch.silviowangler.groovy.util.builder.ICalendarBuilder
import com.matchi.activities.*
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.coupon.CustomerCoupon
import com.matchi.dynamicforms.Submission
import com.matchi.enums.BookingGroupType
import com.matchi.enums.RefundOption
import com.matchi.membership.Membership
import com.matchi.mpc.CodeRequest
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.play.Recording
import com.matchi.requests.TrainerRequest
import grails.plugin.asyncmail.AsynchronousMailService
import grails.plugin.asyncmail.Validator
import groovyx.gpars.GParsPool
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.ComponentList
import net.fortuna.ical4j.model.component.Daylight
import net.fortuna.ical4j.model.component.Standard
import net.fortuna.ical4j.model.component.VTimeZone
import net.fortuna.ical4j.model.property.*
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.codehaus.groovy.grails.web.pages.discovery.GrailsConventionGroovyPageLocator
import org.joda.time.DateTime
import org.springframework.util.StopWatch

class NotificationService {

    static transactional = false
    static mailAddressNoReply = "MATCHi <no-reply@matchi.se>"
    static mailAddressNoReplyEmail = "<no-reply@matchi.se>"
    static mailAddressSupport = "MATCHi <support@matchi.se>"
    static templateRootURI = "/templates/emails/html"
    static PRIORITY_DEFAULT = 5
    static PRIORITY_HIGH = 10
    static PRIORITY_LOW = 1
    static MAX_INPUT_SIZE = 20000 //Unknown source of this value, probably set by a AntiSamy Policy, email breaks if longer

    GrailsConventionGroovyPageLocator groovyPageLocator
    AsynchronousMailService asynchronousMailService
    def grailsApplication
    LinkGenerator grailsLinkGenerator
    def fileArchiveService
    def ticketService
    def messageSource
    def activityService
    def dateUtil
    def scheduledTaskService
    def userService

    public static final String MATCHI_SUPPORT_EMAIL = "support@matchi.se"
    public static final String MATCHI_MPC_EMAIL = "mpc@matchi.se"
    public static final String EMAIL_STRING_SEPARATOR = ','

    public static final String AUTOMATIC_ACTIVITY_CANCELLATION_PARTIPICATION_TEMPLATE = "automaticParticipationCancelledMail"
    public static final String AUTOMATIC_ACTIVITY_CANCELLATION_LEADER_TEMPLATE = "automaticParticipationLeadCancelledMail"

    def sendNewParticipation(Participation participation) {

        def template = "participationNewMail"

        if (!participation.customer.canReceiveMail()) {
            return
        }

        def sendInvite = false
        def inviteLink

        if (!participation.customer.user) {
            sendInvite = true
            inviteLink = grailsLinkGenerator.link([controller: 'info',
                                                   action: "invite", absolute: 'true', params: ['ticket': ticketService.createCustomerInviteTicket(participation.customer)?.key]]).toString()
        }

        def mailLogoType = getNotificationLogoTypeImage(participation?.customer?.facility?.facilityLogotypeImage)
        def expires = String.valueOf((participation?.occasion?.activity as ClassActivity).cancelLimitWithFallback) ?: String.valueOf(participation.customer.facility.getBookingCancellationLimit())
        def mailLocale = participation.customer.locale

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(participation.customer)
            from assembleSender(participation?.customer?.facility?.name)
            replyTo fromFacility(participation?.customer?.facility)
            subject messageSource.getMessage("notifications.newParticipation.subject", null, mailLocale)
            html( view:"${templateRootURI}/${getWhiteLabelTemplate(participation.customer.facility, template)}",
                    model:[participation: participation, "cancelRule": expires, sendInvite: sendInvite, inviteLink: inviteLink, mailLogoType: mailLogoType])
            priority PRIORITY_DEFAULT
        }
    }

    def sendParticipationCancelledNotification(Participation participation, String template = "participationCancelledMail") {
        if (!participation.customer.canReceiveMail()) {
            return
        }

        def sendInvite = false
        def inviteLink

        if (!participation.customer.user) {
            sendInvite = true
            inviteLink = grailsLinkGenerator.link([controller: 'info',
                                                   action: "invite", absolute: 'true', params: ['ticket': ticketService.createCustomerInviteTicket(participation.customer)?.key]]).toString()
        }

        def mailLogoType = getNotificationLogoTypeImage(participation?.customer?.facility?.facilityLogotypeImage)
        def mailLocale = participation.customer.locale

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(participation?.customer)
            from assembleSender(participation?.customer?.facility?.name)
            replyTo fromFacility(participation?.customer?.facility)
            subject messageSource.getMessage("notifications.${template}.subject", [participation?.date?.format('dd MMMM'), participation?.customer?.facility] as String[], mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:[participation: participation, mailLogoType: mailLogoType, sendInvite: sendInvite, inviteLink: inviteLink])
            priority PRIORITY_DEFAULT
        }
    }

    def sendParticipationLeadCancelledNotification(Participation participation) {
        def template = "participationLeadCancelledMail"

        if (!participation.customer.canReceiveMail()) {
            return
        }

        def mailLocale = participation.customer.locale

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to activityService.getActivityEmail(participation)
            from assembleSender(participation?.customer?.facility?.name)
            replyTo fromFacility(participation?.customer?.facility)
            subject messageSource.getMessage("notifications.participationLeadCancelledNotification.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:[participation: participation, cancelRule: String.valueOf(participation.customer.facility.getBookingCancellationLimit())])
            priority PRIORITY_DEFAULT
        }
    }

    void sendAutomaticCancellationNotificationToLeader(ActivityOccasion activityOccasion) {
        if(!activityOccasion.activity instanceof ClassActivity) {
            return
        }

        String toEmail = (activityOccasion.activity as ClassActivity).email

        if(!toEmail) {
            return
        }

        String template = "automaticCancellationNotificationToLeader"
        Facility facility = activityOccasion.activity.facility
        Locale mailLocale = new Locale(facility.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to toEmail
            from assembleSender(facility.name)
            replyTo fromFacility(facility)
            subject messageSource.getMessage("notifications.automaticParticipationCancelledMail.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}", model: [activityOccasion: activityOccasion] )
            priority PRIORITY_DEFAULT
        }
    }

    def sendNewParticipationToAdmin(Participation participation) {
        String toEmail = activityService.getActivityEmail(participation)

        if (!toEmail) {
            return
        }

        String template = "participationNewAdminMail"
        Facility facility = participation?.occasion?.activity?.facility
        Locale mailLocale = new Locale(facility.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to toEmail
            from assembleSender(participation?.customer?.facility?.name)
            replyTo fromFacility(participation?.customer?.facility)
            subject messageSource.getMessage("templates.emails.html.participationNewAdminMail.subject", null, mailLocale)
            html(view: "${templateRootURI}/${template}", model: [participation: participation])
            priority PRIORITY_DEFAULT
        }
    }

    void sendNewRecordingPurchaseNotification(Recording recording, RecordingPurchase recordingPurchase) {
        String toEmail = recordingPurchase.customer.email

        if(!toEmail) {
            return
        }

        String template = "newRecordingPurchaseNotification"
        Facility facility = recordingPurchase.customer.facility
        Locale mailLocale = new Locale(facility.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to toEmail
            from assembleSender(facility.name)
            replyTo fromFacility(facility)
            subject messageSource.getMessage("notifications.recordingPurchase.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}", model: [recording: recording, recordingPurchase: recordingPurchase] )
            priority PRIORITY_DEFAULT
        }
    }

    def sendNewBookingNotification(Booking booking, Payment payment = null) {

        def template = "newBookingNotification"
        def customer = booking?.customer
        def facility = customer?.facility

        if (!customer.canReceiveMail()) {
            return
        }

        def sendInvite = false
        def inviteLink
        def remotePaymentPageLink
        def remotePaymentLink
        if (booking.showRemotePaymentNotificationInEmail()) {
            remotePaymentPageLink = grailsLinkGenerator.link([controller: 'userProfile',
                                                              action: "remotePayments", absolute: 'true']).toString()
            remotePaymentLink = grailsLinkGenerator.link([controller: 'userProfile',
                                                          action: "remotePayments", absolute: 'true', params: [showOrderId: booking?.orderId]]).toString()
        }


        if (!customer.user) {
            sendInvite = true
            inviteLink = grailsLinkGenerator.link([controller: 'info',
                                                   action: "invite", absolute: 'true', params: ['ticket': ticketService.createCustomerInviteTicket(customer)?.key]]).toString()
            log.info "Sending booking notification with invite to ${getCustomerEmailAddress(customer)}"
        } else {
            log.info "Sending booking notification to ${getCustomerEmailAddress(customer)}"
        }


        def expires = String.valueOf(facility.getBookingCancellationLimit())
        def mailLogoType = getNotificationLogoTypeImage(customer?.facility?.facilityLogotypeImage)

        // TODO: Consider facility timezone
        def ics = new ICalendarBuilder()
        ics.calendar {
            events {
                event(start: booking.slot.startTime,
                        end: booking.slot.endTime,
                        location: booking.slot.court?.facility?.getAddress(),
                        timezone: booking.slot.court?.facility?.getTimeZone()?.toString(),
                        summary: "$booking.slot.court.sport.name $booking.slot.court.facility.name, $booking.slot.court.name") {
                    organizer(name: customer.fullName(), email: customer.email)
                }
            }
        }

        def vTzComponent = createVTimezoneComponent()
        if (vTzComponent) {
            ics.cal.components.add(vTzComponent)
        }

        def mailLocale = customer.locale

        validateAndLogWrongEmails(customer)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(customer)
            from assembleSender(customer?.facility?.name)
            replyTo fromFacility(customer.facility)
            subject messageSource.getMessage("notifications.newBookingNotification.subject", null, mailLocale)
            html( view:"${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                    model:[booking: booking,
                           cancelRule: expires,
                           payment: payment,
                           mailLogoType:mailLogoType,
                           sendInvite: sendInvite,
                           inviteLink: inviteLink,
                           remotePaymentPageLink: remotePaymentPageLink,
                           remotePaymentLink: remotePaymentLink])
            attachBytes "matchi.ics", "text/calendar", ics.toString().getBytes("UTF-8")
            priority PRIORITY_HIGH
        }
    }

    void sendNewBookingPlayerNotification(Booking booking, Player player) {
        if (player.email) {
            def template = "newBookingNotification"
            def facility = booking.customer.facility
            def mailLocale = player.customer ? player.customer.locale : new Locale(facility.language)
            def mailLogoType = getNotificationLogoTypeImage(facility.facilityLogotypeImage)
            def greetingPerson = player.customer ?
                    (player.customer.user ? player.customer.user.fullName() : player.customer.fullName()) : ""
            def ics = new ICalendarBuilder()
            ics.calendar {
                events {
                    event(start: booking.slot.startTime,
                            end: booking.slot.endTime,
                            timezone: booking.slot.court?.facility?.getTimeZone()?.toString(),
                            summary: "$booking.slot.court.sport.name $booking.slot.court.facility.name, $booking.slot.court.name") {
                        organizer(name: booking.customer.fullName(), email: booking.customer.email)
                    }
                }
            }

            def vTzComponent = createVTimezoneComponent()
            if (vTzComponent) {
                ics.cal.components.add(vTzComponent)
            }

            asynchronousMailService.sendAsynchronousMail {
                locale mailLocale
                to player.email
                from assembleSender(facility?.name)
                replyTo fromFacility(facility)
                subject messageSource.getMessage("notifications.newBookingNotification.subject", null, mailLocale)
                html(view:"${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                        model: [booking: booking, mailLogoType:mailLogoType,
                                greetingPerson: greetingPerson, hidePayment: true])
                attachBytes "matchi.ics", "text/calendar", ics.toString().getBytes("UTF-8")
                priority PRIORITY_HIGH
            }
        }
    }

    def sendNewBookingGroupNotification(List<Booking> bookings) {

        def template = "newBookingGroupNotification"
        def customer = bookings[0].customer
        def facility = customer?.facility

        if (!customer.canReceiveMail()) {
            return
        }

        def remotePaymentPageLink
        if (bookings.any{ it.showRemotePaymentNotificationInEmail()}) {
            remotePaymentPageLink = grailsLinkGenerator.link([controller: 'userProfile',
                                                              action: "remotePayments", absolute: 'true']).toString()
        }

        log.info("Sending booking-group notification to ${getCustomerEmailAddress(bookings[0].customer)}")
        def expires = String.valueOf(facility.getBookingCancellationLimit())
        def isDefaultBookingGroup = bookings[0].group?.type?.equals(BookingGroupType.DEFAULT)

        if( isDefaultBookingGroup ) {
            log.debug("Booking-group notification send cause type is DEFAULT")

            def sendInvite = false
            def inviteLink

            if (!customer.user) {
                sendInvite = true
                inviteLink = grailsLinkGenerator.link([controller: 'info',
                                                       action: "invite", absolute: 'true', params: ['ticket': ticketService.createCustomerInviteTicket(customer)?.key]]).toString()
            }

            def mailLogoType = getNotificationLogoTypeImage(customer?.facility?.facilityLogotypeImage)
            def mailLocale = customer.locale

            asynchronousMailService.sendAsynchronousMail {
                locale mailLocale
                to getCustomerEmailAddress(customer)
                from assembleSender(customer?.facility?.name)
                replyTo fromFacility(customer.facility)
                subject messageSource.getMessage("notifications.newBookingGroupNotification.subject", null, mailLocale)
                html( view:"${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                        model:["bookings": bookings, "cancelRule": expires, mailLogoType: mailLogoType, sendInvite: sendInvite, inviteLink: inviteLink,
                       remotePaymentPageLink: remotePaymentPageLink])
                priority PRIORITY_HIGH
            }
        } else {
            log.debug("Booking-group notification not sent cause type is NOT DEFAULT")
        }
    }

    def sendNewBookingGroupPlayerNotification(List<Booking> bookings, Player player) {

        if(player.email) {
            def template = "newBookingGroupNotification"
            def customer = player.customer ?: new Customer(email: player.email)
            def facility = customer.facility

            if (!customer.canReceiveMail()) {
                return
            }

            log.info("Sending booking-group player notification to ${getCustomerEmailAddress(customer)}")
            def expires = String.valueOf(facility.getBookingCancellationLimit())
            def isDefaultBookingGroup = bookings[0].group?.type?.equals(BookingGroupType.DEFAULT)

            if( isDefaultBookingGroup ) {
                log.debug("Booking-group player notification send cause type is DEFAULT")

                def sendInvite = false
                def inviteLink

                if (!customer.user) {
                    sendInvite = true
                    inviteLink = grailsLinkGenerator.link([controller: 'info',
                                                           action: "invite", absolute: 'true', params: ['ticket': ticketService.createCustomerInviteTicket(customer)?.key]]).toString()
                }

                def mailLogoType = getNotificationLogoTypeImage(customer?.facility?.facilityLogotypeImage)
                def mailLocale = player.customer ? player.customer.locale : new Locale(facility.language)
                def greetingPerson = player.customer ?
                        (player.customer.user ? player.customer.user.fullName() : player.customer.fullName()) : ""

                asynchronousMailService.sendAsynchronousMail {
                    locale mailLocale
                    to getCustomerEmailAddress(customer)
                    from assembleSender(customer?.facility?.name)
                    replyTo fromFacility(customer.facility)
                    subject messageSource.getMessage("notifications.newBookingGroupNotification.subject", null, mailLocale)
                    html( view:"${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                            model:["bookings": bookings, "cancelRule": expires, mailLogoType: mailLogoType, greetingPerson: greetingPerson, sendInvite: sendInvite, inviteLink: inviteLink])
                    priority PRIORITY_HIGH
                }
            } else {
                log.debug("Booking-group player notification not sent cause type is NOT DEFAULT")
            }
        }
    }

    def sendMovedBookingNotification(Slot toSlot, Slot fromSlot, Customer customer, def message) {

        def template = "movedBookingNotification"
        def facility = customer?.facility

        if (!customer.canReceiveMail()) {
            return
        }

        def sendInvite = false
        def inviteLink

        if (!customer.user) {
            sendInvite = true
            inviteLink = grailsLinkGenerator.link([controller: 'info',
                                                   action: "invite", absolute: 'true', params: ['ticket': ticketService.createCustomerInviteTicket(customer)?.key]]).toString()
        }
        log.info "Sending moved booking notification to ${getCustomerEmailAddress(customer)}"
        def mailLogoType = getNotificationLogoTypeImage(facility?.facilityLogotypeImage)
        def mailLocale = customer.locale

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(customer)
            from assembleSender(customer?.facility?.name)
            replyTo fromFacility(customer.facility)
            subject messageSource.getMessage("notifications.movedBookingNotification.subject", null, mailLocale)
            html( view:"${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                    model:[fromSlot: fromSlot, toSlot: toSlot, customer:customer,
                           message:message,
                           mailLogoType: mailLogoType,
                           sendInvite: sendInvite,
                           inviteLink: inviteLink])
            priority PRIORITY_HIGH
        }
    }

    def sendBookingCanceledNotification(Booking booking, def message, Payment payment = null, RefundOption refundOption = null) {

        def template = "bookingCanceledNotification"
        def customer = booking.customer
        def facility = customer?.facility
        def hidePayment = userService?.getLoggedInUser() ? !userService?.getLoggedInUser()?.isInRole('ROLE_USER') : !customer?.user?.isInRole('ROLE_USER')

        if (!customer.canReceiveMail()) {
            return
        }

        def sendInvite = false
        def inviteLink

        if (!customer.user) {
            sendInvite = true
            inviteLink = grailsLinkGenerator.link([controller: 'info',
                                                   action: "invite", absolute: 'true', params: ['ticket': ticketService.createCustomerInviteTicket(customer)?.key]]).toString()
        }
        log.info "Sending booking cancel notification to ${getCustomerEmailAddress(customer)}"

        def mailLogoType = getNotificationLogoTypeImage(facility?.facilityLogotypeImage)
        def mailLocale = customer.locale
        def refundMessage
        if (refundOption){
            switch (refundOption) {
                case RefundOption.FULL_REFUND:
                    refundMessage = messageSource.getMessage("facilityBooking.facilityBookingCancel.message24", null, mailLocale)
                    break
                case RefundOption.CUSTOMER_PAYS_FEE:
                    refundMessage = messageSource.getMessage("facilityBooking.facilityBookingCancel.message25", null, mailLocale)
                    break
                case RefundOption.NO_REFUND:
                    refundMessage = messageSource.getMessage("facilityBooking.facilityBookingCancel.message26", null, mailLocale)
                    break
            }
        }
        if (message && refundMessage){
            message = refundMessage + " " + message
        } else if (refundMessage){
            message = refundMessage
        }

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(customer)
            from assembleSender(customer?.facility?.name)
            replyTo fromFacility(customer.facility)
            subject messageSource.getMessage("notifications.bookingCanceledNotification.subject", null, mailLocale)
            html( view:"${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                    model:["booking": booking, "message": message, "payment": payment, hidePayment: hidePayment,
                            mailLogoType: mailLogoType, sendInvite: sendInvite, inviteLink: inviteLink])
            priority PRIORITY_HIGH
        }
    }

    def sendBookingCanceledPlayerNotification(Booking booking, Player player, def message) {
        def template = "bookingCanceledNotification"
        def customer = player.customer ?: new Customer(email: player.email)
        def facility = booking.customer.facility

        if (!customer.canReceiveMail()) {
            return
        }

        def sendInvite = false
        def inviteLink
        if (customer.id && !customer.user) {
            sendInvite = true
            inviteLink = grailsLinkGenerator.link([controller: 'info', action: "invite", absolute: 'true',
                                                   params: ['ticket': ticketService.createCustomerInviteTicket(customer)?.key]]).toString()
        }
        log.info "Sending booking cancel notification to ${getCustomerEmailAddress(customer)}"

        def mailLogoType = getNotificationLogoTypeImage(facility?.facilityLogotypeImage)
        def mailLocale = player.customer ? player.customer.locale : new Locale(facility.language)
        def greetingPerson = player.customer ?
                (player.customer.user ? player.customer.user.fullName() : player.customer.fullName()) : ""

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(customer)
            from assembleSender(facility?.name)
            replyTo fromFacility(facility)
            subject messageSource.getMessage("notifications.bookingCanceledNotification.subject", null, mailLocale)
            html( view:"${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                    model: [booking: booking, message: message, hidePayment: true, greetingPerson: greetingPerson,
                            mailLogoType: mailLogoType, sendInvite: sendInvite, inviteLink: inviteLink])
            priority PRIORITY_HIGH
        }
    }

    def sendBookingCanceledTrainerNotification(Booking booking, Trainer trainer) {
        if (!trainer.email) {
            return
        }
        def template = "lessonCanceledNotification"
        def facility = booking.customer.facility
        def mailLocale = new Locale(facility.language)
        def greetingPerson = trainer.fullName()
        def mailLogoType = getNotificationLogoTypeImage(facility?.facilityLogotypeImage)
        def clientName = booking.customer.user ? booking.customer.user.fullName() : booking.customer.fullName()

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to trainer.email
            from assembleSender(facility?.name)
            replyTo fromFacility(facility)
            subject messageSource.getMessage("notifications.lessonCanceledNotification.subject", null, mailLocale)
            html( view:"${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                    model: [booking: booking, message: message, greetingPerson: greetingPerson,
                            mailLogoType: mailLogoType, clientName: clientName])
            priority PRIORITY_HIGH
        }
    }

    def sendChangeEmailTicketMail(User user, ChangeEmailTicket ticket) {
        log.info "Sending changeEmail mail to ${ticket.newEmail}"
        def template = "changeEmailMail"

        def link = grailsLinkGenerator.link([controller: 'changeEmail',
                                             action: "change", absolute: 'true', params: ['ticket': ticket.key , 'newEmail': ticket.newEmail]]).toString()

        def mailLocale = new Locale(user.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to ticket.newEmail
            from mailAddressNoReply
            subject messageSource.getMessage("notifications.changeEmailConfirmation.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:["user": user, "confirmationLink": link])
            priority PRIORITY_HIGH
        }
    }
    def sendActivationMail(User user, def showTerms, def params) {
        log.info "Sending activation-mail to ${user.email}"
        def template = "activationMail"

        def exturl = null
        if (params.f) {
            def facility = Facility.findById(Long.parseLong(params.f))
            template = getWhiteLabelTemplate(facility, template)
            exturl = facility.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.WHITE_LABEL_EXT_URL.toString())
        }

        def link = grailsLinkGenerator.link([controller: 'userRegistration', action: 'enable', absolute: 'true', params: ['ac': user.activationcode, wl:params?.wl?1:"", returnUrl: exturl?:(params?.returnUrl?:""), f:params?.f?:""]]).toString()
        def terms = grailsLinkGenerator.link([controller: 'home', action: 'useragreement', absolute: 'true' ]).toString()
        def mailLocale = new Locale(user.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to user.email
            from mailAddressNoReply
            subject messageSource.getMessage("notifications.activationMail.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:["user": user, "activationLink": link, 'userAgreement': terms, 'showTerms': showTerms])
            priority PRIORITY_HIGH
        }
    }

    def sendSlotWatchNotificationTo(User user, Slot slot) {
        log.info "Sending slot watch notification to ${user.email}"
        def template = "slotWatchNotification"

        def mailLocale = new Locale(user.language)
        def facility   = slot.court.facility

        def facilityLink = grailsLinkGenerator.link([controller: "facility", action: "show", absolute: 'true', params: [name: facility.shortname, date: new DateTime(slot.startTime).toString(dateUtil.DEFAULT_DATE_FORMAT)]])
        def bookingLink  = ""

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to user.email
            from mailAddressNoReply
            subject messageSource.getMessage("notifications.slotWatchNotification.subject",
                    [facility?.name] as String[], mailLocale)

            html( view:"${templateRootURI}/${template}",
                    model:[user:user, facility:facility, slot:slot, facilityLink:facilityLink, bookingLink:bookingLink])
            priority PRIORITY_HIGH
        }
    }

    void sendActivityWatchNotificationTo(User user, ActivityOccasion occasion) {
        log.info "Sending activity watch notification to ${user.email}"
        def template = "activityWatchNotification"

        def mailLocale = new Locale(user.language)
        def facility   = occasion.activity.facility

        def facilityLink = grailsLinkGenerator.link([controller: "facility", action: "show", absolute: 'true', params: [name: facility.shortname]])

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to user.email
            from mailAddressNoReply
            subject messageSource.getMessage("notifications.activityWatchNotification.subject",
                    [facility.name] as String[], mailLocale)

            html(view:"${templateRootURI}/${template}",
                    model: [user: user, facility: facility, occasion: occasion, facilityLink: facilityLink])
            priority PRIORITY_HIGH
        }
    }

    def sendRegistrationConfirmationMail(User user, Facility facility = null) {
        log.info "Sending registration confirm mail to ${user.email}"
        def template = "registrationConfirm"

        def link = grailsLinkGenerator.link([controller: 'home', absolute: 'true']).toString()

        if (facility) {
            template = getWhiteLabelTemplate(facility, template)
        }

        def mailLocale = new Locale(user.language)
        def subjectText = messageSource.getMessage(
                "notifications.registrationConfirmationMail.subject1", null, mailLocale)
        if(facility) {
            subjectText = messageSource.getMessage("notifications.registrationConfirmationMail.subject2",
                    [facility.name] as String[], mailLocale)
        }

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to user.email
            from mailAddressNoReply
            subject subjectText
            html( view:"${templateRootURI}/${template}",
                    model:["user": user, "link": link])
            priority PRIORITY_HIGH
        }
    }

    def sendResetPasswordTicketMail(User user, ResetPasswordTicket ticket) {
        log.info "Sending change password mail to ${user.email}"
        def template = "passwordResetMail"

        def link = grailsLinkGenerator.link([controller: 'resetPassword',
                                             action: "change", absolute: 'true', params: ['ticket': ticket.key]]).toString()

        // if user is not activated, attach the activation link in forgot password mail
        def activationLink = null
        if (!user.enabled) {
            activationLink = grailsLinkGenerator.link([controller: 'userRegistration', action: 'enable', absolute: 'true', params: ['ac': user.activationcode]]).toString()
        }

        def mailLocale = new Locale(user.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to user.email
            from mailAddressNoReply
            subject messageSource.getMessage("notifications.resetPasswordTicketMail.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:["user": user, "resetPasswordLink": link, activationLink: activationLink])
            priority PRIORITY_HIGH
        }
    }

    def sendCustomerInvitation(Customer customer, CustomerInviteTicket ticket) {
        log.info "Sending customer invite to ${customer.fullName()}, ${customer.email}"

        def template = "customerInvitationMail"
        def facility = customer?.facility

        if (customer.user) {
            log.debug("No customer invitation was sent cause customer already has a user")
            return
        }

        if (customer.clubMessagesDisabled) {
            log.debug("No customer invitation was sent cause customer does not want to get any emails")
            return
        }

        def mailLogoType = getNotificationLogoTypeImage(facility?.facilityLogotypeImage)
        def mailLocale = customer.locale
        def link = grailsLinkGenerator.link([controller: 'info',
                                             action: "invite", absolute: 'true', params: ['ticket': ticket.key]]).toString()

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to customer.email
            from mailAddressNoReply
            subject messageSource.getMessage("notifications.customerInvitation.subject",
                    [facility?.name] as String[], mailLocale)
            html( view:"${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                    model:["customer": customer, link: link, mailLogoType: mailLogoType])
            priority PRIORITY_LOW
        }
    }

    def sendCustomerMessage(Customer customer, def message, def mailSubject = null, def fMail = null, boolean sendToGuardianEmailAlso = false,
                            CustomerDisableMessagesTicket ticket = null) {
        def template = "customerMessage"
        def facility = customer.facility
        log.debug "Sending message from facility: ${facility.name} to ${customer.fullName()}"

        String customerEmailAdress = getCustomerEmailAddress(customer, sendToGuardianEmailAlso)

        if (customerEmailAdress=="" || !customer.canReceiveMail()) {
            return
        }

        def link
        if (ticket) {
            link = grailsLinkGenerator.link(controller: "facilityCustomer",
                    action: "showByTicket", absolute: "true", params: [ticket: ticket.key])
        }

        def mailLocale = customer.locale

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to customerEmailAdress?.split(EMAIL_STRING_SEPARATOR)?.toList()
            from assembleSender(facility?.name)
            replyTo fMail ?: facility.email
            subject mailSubject ?: messageSource.getMessage("notifications.customerMessage.subject",
                    [facility.name] as String[], mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:["customer": customer, "message": message, link: link])
            priority PRIORITY_LOW
        }
    }

    def sendCustomerInvoice(Customer customer, String message, byte[] attachment, String fMail) {
        def template = "customerInvoice"
        def facility = customer.facility
        log.info "Sending message from facility: ${facility.name} to ${customer.fullName()}"

        if (!customer.canReceiveMail()) {
            return
        }

        def mailLocale = customer.locale

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to customer.getCustomerInvoiceEmail()
            from assembleSender(facility?.name)
            replyTo fMail ?: facility.email
            subject messageSource.getMessage("notifications.customerInvoice.subject",
                    [facility.name] as String[], mailLocale)
            html(view: "${templateRootURI}/${template}",
                    model: ["customer": customer, "message": message])
            attachBytes "Invoice.pdf", "application/pdf", attachment
            priority PRIORITY_DEFAULT
        }
    }

    def sendSubscriptionInformationMessage(Customer customer, List<Subscription> subscriptions, def message, def fMail = null, def bccMail = null) {
        def template = "subscriptionInformationMessage"
        def facility = customer.facility
        log.info "Sending subscription message from facility: ${facility.name} to ${customer.fullName()}"

        if (!customer.canReceiveMail()) {
            return
        }

        def mailLocale = customer.locale

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(customer)
            from assembleSender(facility?.name)
            replyTo fMail ?: facility.email
            bcc bccMail
            subject messageSource.getMessage("notifications.subscriptionInformationMessage.subject",
                    [facility.name] as String[], mailLocale)
            html( view:"${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                    model:["customer": customer, "subscriptions": subscriptions, "message": message])
            priority PRIORITY_DEFAULT
        }
    }

    def sendUserMessage(User userFrom, User userTo, def message) {
        log.info "Sending message between users: ${userFrom.email} to ${userTo.email}"
        def template = "userMessage"

        def link = grailsLinkGenerator.link([controller: 'userProfile',
                                             action: "index", absolute: 'true', params: ['id': userFrom.id]]).toString()
        def inboxLink = grailsLinkGenerator.link([controller: 'userMessage',
                                                  action: "conversation", absolute: 'true', params: ['id': userFrom.id]]).toString()
        def mailLocale = new Locale(userTo.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to userTo.email
            from mailAddressNoReply
            subject messageSource.getMessage("notifications.userMessage.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:["fromUser": userFrom, "toUser": userTo, "message": message, link: link, inboxLink: inboxLink])
            priority PRIORITY_HIGH
        }
    }

    def sendMembershipRequestNotification(Membership membership, Facility facility, def message) {
        log.info "Sending membership request notification to: ${facility.name}"
        def template = "membershipRequestNotification"

        def link = grailsLinkGenerator.link([controller: 'facilityCustomer',
                                             action: "show", absolute: 'true', params: ['id': membership?.customer?.id]]).toString()

        def requestSettings = grailsLinkGenerator.link([controller: 'facilityAdministration',
                                                        action: "index", absolute: 'true']).toString()

        def toMail = facility.membershipRequestEmail ?: facility.email
        def mailLocale = new Locale(facility.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to toMail
            from mailAddressNoReply
            subject messageSource.getMessage("notifications.membershipRequestNotification.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:[customer: membership.customer, facility: facility, link: link, requestSettings:requestSettings, message:message])
            priority PRIORITY_DEFAULT
        }
    }

    // Trainer request to requester
    def sendTrainerRequestNotificationToRequester(TrainerRequest request) {
        log.info "Sending trainer request notification to requester: ${request.requester}"
        def template = "trainerRequestNotificationRequester"

        def link = grailsLinkGenerator.link([controller: 'userProfile',
                                             action: "home", absolute: 'true']).toString()

        def toMail = request?.requester?.email
        def mailLocale = new Locale(request?.requester?.language)

        def ics = new ICalendarBuilder()
        ics.calendar {
            events {
                event(start: request.start,
                        end: request.end,
                        location: request.trainer?.facility?.getAddress(),
                        timezone: request.trainer?.facility?.getTimeZone()?.toString(),
                        summary: "$request.trainer.facility.name, $request.trainer.firstName $request.trainer.lastName") {
                    organizer(name: request.requester.fullName(), email: request.requester.email)
                }
            }
        }
        def vTzComponent = createVTimezoneComponent()
        if (vTzComponent) {
            ics.cal.components.add(vTzComponent)
        }

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to toMail
            from assembleSender(request?.trainer?.facility?.name)
            replyTo fromFacility(request?.trainer?.facility)
            subject messageSource.getMessage("notifications.trainerRequestToRequester.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:[trainerRequest: request, link: link])
            attachBytes "matchi.ics", "text/calendar", ics.toString().getBytes("UTF-8")
            priority PRIORITY_HIGH
        }
    }

    // Trainer request to trainer
    def sendTrainerRequestNotificationToTrainer(TrainerRequest request) {
        log.info "Sending trainer request notification to trainer: ${request.trainer}"
        def template = "trainerRequestNotificationTrainer"

        def link = grailsLinkGenerator.link([controller: 'userTrainer',
                                             action: "requests", absolute: 'true']).toString()

        def toMail = getTrainerEmail(request)
        def mailLocale = new Locale(request?.trainer?.facility?.language)

        def ics = new ICalendarBuilder()
        ics.calendar {
            events {
                event(start: request.start,
                        end: request.end,
                        location: request.trainer?.facility?.getAddress(),
                        timezone: request.trainer?.facility?.getTimeZone()?.toString(),
                        summary: "$request.trainer.facility.name, $request.requester.firstname $request.requester.lastname") {
                    organizer(name: request.trainer.fullName(), email: getTrainerEmail(request))
                }
            }
        }
        def vTzComponent = createVTimezoneComponent()
        if (vTzComponent) {
            ics.cal.components.add(vTzComponent)
        }

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to toMail
            from assembleSender(request?.trainer?.facility?.name)
            replyTo fromFacility(request?.trainer?.facility)
            subject messageSource.getMessage("notifications.trainerRequestToTrainer.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:[trainerRequest: request, link: link])
            attachBytes "matchi.ics", "text/calendar", ics.toString().getBytes("UTF-8")
            priority PRIORITY_HIGH
        }
    }

    // Trainer request to trainer
    def sendDeletedTrainerRequestNotification(TrainerRequest request) {
        log.info "Sending deleted trainer request notification to trainer: ${request.trainer}"
        def template = "trainerRequestDeletedNotification"

        def link = grailsLinkGenerator.link([controller: 'userTrainer',
                                             action: "requests", absolute: 'true']).toString()

        def toMail = getTrainerEmail(request)
        def mailLocale = new Locale(request?.trainer?.facility?.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to toMail
            from assembleSender(request?.trainer?.facility?.name)
            replyTo fromFacility(request?.trainer?.facility)
            subject messageSource.getMessage("notifications.trainerRequestDeleted.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:[trainerRequest: request, link: link])
            priority PRIORITY_HIGH
        }
    }

    def sendActivitySubmissionNotification(Customer customer, Activity activity) {
        log.info "Sending course submission notification to: ${activity?.facility?.name}"
        def template = "courseSubmissionNotification"

        def link = grailsLinkGenerator.link([controller: 'facilityCustomer',
                                             action: "show", absolute: 'true', params: ['id': customer.id]]).toString()

        def toMail = activity?.getToMails()
        def mailLocale = new Locale(activity?.facility?.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to toMail
            from mailAddressNoReply
            replyTo activity?.facility?.email
            subject messageSource.getMessage("notifications.courseSubmissionNotification.subject",
                    [customer.fullName()] as String[], mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:[customer:customer, activity: activity, link:link])
            priority PRIORITY_HIGH
        }
    }

    def sendEventActivitySubmissionNotification(Customer customer, EventActivity activity) {
        log.info "Sending event submission notification to: ${activity.facility.name}"
        def template = "eventActivitySubmissionNotification"

        def link = grailsLinkGenerator.link([controller: 'facilityCustomer',
                                             action: "show", absolute: 'true', params: ['id': customer.id]]).toString()

        def mailLocale = new Locale(activity.facility.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to activity.facility.email
            from mailAddressNoReply
            replyTo activity?.facility?.email
            subject messageSource.getMessage("notifications.eventActivitySubmissionNotification.subject",
                    [customer.fullName()] as String[], mailLocale)
            html(view:"${templateRootURI}/${template}",
                    model: [customer: customer, activity: activity, link: link])
            priority PRIORITY_HIGH
        }
    }

    def sendFormSubmissionCompletedNotification(Customer customer, Activity activity) {
        log.info "Sending form submission notification to: ${customer.fullName()}"
        def template = "customerSubmissionNotification"

        def mailLocale = new Locale(activity?.facility?.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(customer, true)?.split(EMAIL_STRING_SEPARATOR)?.toList()
            from assembleSender(activity?.facility?.name)
            replyTo activity?.facility?.email
            subject messageSource.getMessage("notifications.customer.submissionNotification.subject",
                    [customer.fullName()] as String[], mailLocale)
            html( view:"${templateRootURI}/${getWhiteLabelTemplate(activity?.facility, template)}",
                    model: [customer: customer, activity: activity])
            priority PRIORITY_HIGH
        }
    }

    def sendCustomerUpdateRequest(Customer customer, CustomerUpdateRequestTicket ticket, def message) {
        log.info "Sending customer update request to ${customer.fullName()}, ${customer.email}"
        def template = "customerUpdateRequest"

        def link = grailsLinkGenerator.link([controller: "updateCustomerRequest",
                                             action: "index", absolute: 'true', params: ['ticket': ticket.key]]).toString()
        def mailLocale = customer.locale

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(customer)
            from assembleSender(customer?.facility?.name)
            replyTo fromFacility(customer.facility)
            subject messageSource.getMessage("notifications.customerUpdateRequest.subject",
                    [customer.facility.name] as String[], mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:[customer: customer, link: link, message: message])
            priority PRIORITY_LOW
        }
    }

    def sendCustomerUpdatedConfirm(Customer customer, Facility facility, def message) {
        log.info "Sending customer updated information notification to: ${facility.name}"
        def template = "customerUpdatedConfirmNotification"

        def link = grailsLinkGenerator.link([controller: 'facilityCustomer',
                                             action: "show", absolute: 'true', params: ['id': customer.id]]).toString()
        def mailLocale = new Locale(facility.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to facility.email
            from mailAddressNoReply
            subject messageSource.getMessage("notifications.customerUpdatedConfirm.subject", null, mailLocale)
            html( view:"${templateRootURI}/${template}",
                    model:[customer: customer, facility: facility, link: link, message:message])
            priority PRIORITY_LOW
        }
    }

    def sendOnlineCouponReceipt(CustomerCoupon customerCoupon) {

        def template = "couponPurchaseReceipt"
        def customer = customerCoupon.customer

        if (!customer.canReceiveMail()) {
            log.error("No customer attached to coupon when sending customer coupon receipt")
        } else {
            log.info "Sending customer online coupon purchase receipt"

            def link = grailsLinkGenerator.link([controller: 'userProfile',
                                                 action: "home", absolute: 'true']).toString()

            def purchaseAgreementLink = grailsLinkGenerator.link([controller: 'userProfile',
                                                                  action: "home", absolute: 'true']).toString()

            def mailLogoType = getNotificationLogoTypeImage(customer?.facility?.facilityLogotypeImage)
            def mailLocale = customer.locale

            asynchronousMailService.sendAsynchronousMail {
                locale mailLocale
                to getCustomerEmailAddress(customer)
                from assembleSender(customer?.facility?.name)
                replyTo fromFacility(customer.facility)
                subject messageSource.getMessage("notifications.onlineCouponReceipt.subject",
                        [customerCoupon.coupon.facility.name] as String[], mailLocale)
                html( view:"${templateRootURI}/${getWhiteLabelTemplate(customer.facility, template)}",
                        model:[ customerCoupon: customerCoupon, link: link,
                                purchaseAgreementLink: purchaseAgreementLink, mailLogoType: mailLogoType,
                                order: customerCoupon.order])
                priority PRIORITY_HIGH
            }
        }
    }

    def sendContactMeNotification(ContactMe contactMe) {

        def template = "contactMeNotification"

        if (contactMe.email) {
            log.info "Sending contact me notification from ${contactMe.email}"

            asynchronousMailService.sendAsynchronousMail {
                to mailAddressSupport
                from contactMe.email
                subject "MATCHi enquiry from ${contactMe?.facility}"
                html( view:"${templateRootURI}/${template}",
                        model:[ contactMe: contactMe ])
                priority PRIORITY_LOW
            }
        }
    }

    def sendOnlineMembershipReceipt(Membership membership) {
        def template = "membershipPurchaseReceipt"
        def customer = membership.customer

        if (!customer.canReceiveMail()) {
            log.error("No customer attached to coupon when sending customer membership receipt")
        } else {
            log.info "Sending customer online membership purchase receipt"

            def link = grailsLinkGenerator.link(controller: 'userProfile',
                    action: "home", absolute: 'true')
            def purchaseAgreementLink = grailsLinkGenerator.link(controller: 'userProfile',
                    action: "home", absolute: 'true')
            def mailLogoType = getNotificationLogoTypeImage(customer?.facility?.facilityLogotypeImage)
            def mailLocale = customer.locale

            asynchronousMailService.sendAsynchronousMail {
                locale mailLocale
                to getCustomerEmailAddress(customer)
                from assembleSender(customer?.facility?.name)
                replyTo fromFacility(customer.facility)
                subject messageSource.getMessage("notifications.onlineMembershipReceipt.subject",
                        [customer.facility.name] as String[], mailLocale)
                html(view: "${templateRootURI}/${getWhiteLabelTemplate(customer.facility, template)}",
                        model: [membership: membership, link: link, order: membership.order,
                                purchaseAgreementLink: purchaseAgreementLink, mailLogoType: mailLogoType])
                priority PRIORITY_HIGH
            }
        }
    }

    void sendMembershipRenewReceipt(Membership membership) {
        def template = "membershipRenewReceipt"
        def customer = membership.customer

        if (!customer.canReceiveMail()) {
            log.warn("Membership (ID: $membership.id) renewal receipt was not sent, because customer (ID: $customer.id) has no email specified")
        } else {
            log.info "Sending membership (ID: $membership.id) renewal receipt to customer (ID: $customer.id)"

            def mailLocale = customer.locale

            asynchronousMailService.sendAsynchronousMail {
                locale mailLocale
                to getCustomerEmailAddress(customer)
                from assembleSender(customer.facility.name)
                replyTo fromFacility(customer.facility)
                subject messageSource.getMessage("notifications.onlineMembershipReceipt.subject",
                        [customer.facility.name] as String[], mailLocale)
                html(view: "${templateRootURI}/${getWhiteLabelTemplate(customer.facility, template)}",
                        model: [membership: membership, order: membership.order])
                priority PRIORITY_HIGH
            }
        }
    }

    void sendMembershipPaymentFailedNotification(Order order, Membership membership, Boolean lastTry = false) {
        def template = "membershipPaymentFailed"
        def customer = membership.customer

        if (!customer.canReceiveMail()) {
            log.warn("Membership (ID: $membership.id) failed payment notification was not sent, because customer (ID: $customer.id) has no email specified")
        } else {
            log.info "Sending membership (ID: $membership.id) failed payment notification to customer (ID: $customer.id)"

            def mailLocale = customer.locale

            asynchronousMailService.sendAsynchronousMail {
                locale mailLocale
                to getCustomerEmailAddress(customer)
                from assembleSender(customer.facility.name)
                replyTo fromFacility(customer.facility)
                subject messageSource.getMessage("notifications.membershipPaymentFailed.subject",
                        null, mailLocale)
                html(view: "${templateRootURI}/${getWhiteLabelTemplate(customer.facility, template)}",
                        model: [membership: membership, order: order, lastTry: lastTry,
                                failedAttemptsThreshold: grailsApplication.config.matchi.membership.payment.failedAttemptsThreshold])
                priority PRIORITY_HIGH
            }
        }
    }

    void sendMembershipPaymentFailedAdminNotification(Facility facility, List customersData) {
        def template = "membershipPaymentFailedAdmin"

        log.info "Sending membership failed payment notification to facility $facility.name"

        def mailLocale = new Locale(facility.language)

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to facility.email
            from mailAddressNoReply
            subject messageSource.getMessage("notifications.membershipPaymentFailed.subject",
                    null, mailLocale)
            html(view: "${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                    model: [facility: facility, customersData: customersData])
            priority PRIORITY_HIGH
        }
    }

    void sendBookingReminder(Customer customer, List<Slot> slots, List<BookingCancelTicket> tickets) {
        def template = "bookingReminder"

        def links = []
        tickets.each {
            links << grailsLinkGenerator.link(controller: "userBooking",
                    action: "showByTicket", absolute: "true", params: [ticket: it.key])
        }

        def accessCode = slots.find { Slot s ->
            return s?.booking?.getAccessCode() != null
        }?.booking?.getAccessCode()

        if(!getCustomerEmailAddress(customer)?.equals("")) {
            def mailLocale = customer.locale

            asynchronousMailService.sendAsynchronousMail {
                locale mailLocale
                to getCustomerEmailAddress(customer)
                from assembleSender(customer?.facility?.name)
                replyTo fromFacility(customer.facility)
                subject messageSource.getMessage("notifications.bookingReminder.subject", null, mailLocale)
                html(view: "${templateRootURI}/${template}",
                        model: [customer: customer, slots: slots, links: links,
                                facilityText: customer.facility.getFacilityPropertyValue(
                                        FacilityProperty.FacilityPropertyKey.SUBSCRIPTION_REMINDER_TEXT.name()),
                                accessCode: accessCode])
                priority PRIORITY_DEFAULT
            }
        }
    }

    def sendFormSubmissionReceipt(Submission submission) {
        if (submission.submissionIssuer?.email) {
            def template = "formSubmissionReceipt"

            log.info "Sending form submission receipt to ${submission.submissionIssuer.email}"

            def mailLocale = new Locale(submission.submissionIssuer.language)

            asynchronousMailService.sendAsynchronousMail {
                locale mailLocale
                to submission.submissionIssuer.email
                from assembleSender(submission?.form?.facility?.name)
                replyTo fromFacility(submission?.form?.facility)
                subject messageSource.getMessage("notifications.formSubmissionReceipt.subject",
                        [submission.form.activity?.name ?: submission.form.name] as String[], mailLocale)
                html(view: "${templateRootURI}/${getWhiteLabelTemplate(submission.form.facility, template)}",
                        model: [submission: submission, form: submission.form, order: submission.order])
                priority PRIORITY_HIGH
            }
        }
    }

    void sendCourseOccasionInfo(Customer customer, Collection occasions, String message,
                                String fMail, boolean sendToGuardianEmailAlso,
                                Boolean includeTrainers = false, Boolean includeParticipants = false) {
        def template = "courseOccasionInfo"

        if (!customer.canReceiveMail()) {
            return
        }

        log.info "Sending course occasion information"

        def mailLocale = customer.locale

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(customer, sendToGuardianEmailAlso)?.split(EMAIL_STRING_SEPARATOR)?.toList()
            from assembleSender(customer?.facility?.name)
            replyTo fMail ?: customer.facility.email
            subject messageSource.getMessage("notifications.courseOccasionInfo.subject",
                    [occasions[0]?.activity?.name] as String[], mailLocale)
            html(view: "${templateRootURI}/${template}",
                    model: [customer: customer, occasions: occasions, message: message,
                            includeTrainers: includeTrainers, includeParticipants: includeParticipants])
            priority PRIORITY_LOW
        }
    }

    void sendUnverifiedCodeRequestNotification(CodeRequest codeRequest) {
        def template = "unverifiedCodeRequestNotification"

        def booking  = Booking.findById(codeRequest?.bookingId)
        def customer = booking?.customer

        if (!customer.canReceiveMail()) {
            return
        }

        def mailLocale = customer.locale

        log.info "Sending unverified CodeRequest notification"

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(customer)?.split(EMAIL_STRING_SEPARATOR)?.toList()
            from assembleSender(customer?.facility?.name)
            replyTo customer.facility.email
            subject messageSource.getMessage("notifications.unverifiedCodeRequestNotification.subject",
                    [customer?.facility?.name] as String[], mailLocale)
            html(view: "${templateRootURI}/${template}",
                    model: [ customer: customer, booking: booking ])
            priority PRIORITY_HIGH
        }
    }

    void sendAdminNodeStatusChangedNotification(Facility facility, FacilityProperty.MpcStatus mpcStatus) {
        List emailAddresses = facility.getMpcNotificationMails()

        if(emailAddresses.size() == 0 && facility.email) emailAddresses = [facility.email]

        if(emailAddresses.size() == 0) {
            log.info "Could not notify facility ${facility.name} on light/passage control system status changed"
        } else {
            sendNodeStatusChangedNotification(emailAddresses, facility, mpcStatus)
        }
    }

    private void sendNodeStatusChangedNotification(List emailAddresses, Facility facility, FacilityProperty.MpcStatus mpcStatus) {
        def mailLocale = new Locale(facility.language)

        String subjectKey
        String template

        if(mpcStatus == FacilityProperty.MpcStatus.OK) {
            subjectKey = "notifications.adminNodeStatusChangedNotification.subject_ok"
            template = "adminNodeStatusSuccessfulNotification"
        } else {
            subjectKey = "notifications.adminNodeStatusChangedNotification.subject_not_ok"
            template = "adminNodeStatusFailedNotification"
        }

        emailAddresses.each { String emailAddress ->
            log.info "Sending Node changed notification to admin at ${emailAddress}"
            asynchronousMailService.sendAsynchronousMail {
                locale mailLocale
                to emailAddress
                from MATCHI_MPC_EMAIL
                replyTo MATCHI_MPC_EMAIL
                subject messageSource.getMessage(subjectKey, [facility.name] as String[], mailLocale)
                html(view: "${templateRootURI}/${template}", model: [facility: facility])
                priority PRIORITY_HIGH
            }
        }
    }


    //Helper functions
    //------------------------------------------------------

    def getWhiteLabelTemplate(Facility facility, String template) {
        log.debug("Getting white label template ${template} for ${facility.name}")
        def facilityTemplate = groovyPageLocator.findViewByPath("${templateRootURI}/${facility.shortname}/${template}")

        if (facilityTemplate) {
            return "${facility.shortname}/${template}"
        } else {
            log.info("Could not find \"${template}\" for ${facility} in ${templateRootURI}/${facility.shortname}")
        }

        return template
    }

    def getCustomerEmailAddress(Customer customer, boolean addGuardianEmail = false) {
        String result = ""
        String guardianEmails = ""
        List guardianList = []

        // Customer email
        if (customer?.email) {
            result = customer?.email
        }

        guardianList.addAll(customer.getGuardianMessageInfo())
        guardianEmails = guardianList.collect { it.email }.unique().join(",")

        if(result && addGuardianEmail && customer.hasGuardianEmails()) {
            result += "," + guardianEmails
        } else if(!result && customer.hasGuardianEmails()) {
            result += guardianEmails
        }

        return result
    }

    def fromFacility(def facility) {
        if(facility.name && facility.email) {
            return "${facility.email}"
        } else {
            // fallback to MATCHi
            return mailAddressNoReply
        }

    }

    def getTrainerEmail(TrainerRequest request) {
        return request?.trainer?.email ?: (request?.trainer?.customer?.email ?: request?.trainer?.customer?.user?.email)
    }

    def getNotificationLogoTypeImage(MFile logoTypeImage) {
        if (logoTypeImage) {
            return fileArchiveService.getAbsoluteFileURL(logoTypeImage)
        }

        return "http://matchi.se/static/images/logo.png"
    }

    def assembleSender(String name) {
        return name ? "\"${name}\" ${mailAddressNoReplyEmail}" : mailAddressNoReply
    }

    def sendPaidRemoteOrderNotification(Order order) {
        String template = "paidRemoteOrderNotification"
        Customer customer = order.customer
        Facility facility = customer.facility
        User user = customer.user
        OrderPayment payment = order.payments.toList().first()

        if (!customer.canReceiveMail()) {
            return
        }

        log.info "Sending paid remote order notification to ${getCustomerEmailAddress(customer)}"
        def mailLocale = customer.locale

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to getCustomerEmailAddress(customer)
            from assembleSender(customer.facility.name)
            replyTo fromFacility(customer.facility)
            subject messageSource.getMessage("templates.emails.html.remotePaidConfirmation.subject", null, mailLocale)
            html( view:"${templateRootURI}/${getWhiteLabelTemplate(facility, template)}",
                    model:[customer:customer, order: order, payment: payment, user: user])
            priority PRIORITY_HIGH
        }
    }

    /**
     * Method to send emails threaded in background job
     * @param collectionToIterate
     * @param closure
     * @return
     */
    def executeSending(List collectionToIterate, String taskName, Facility facility, Closure closure) {
        scheduledTaskService.scheduleTask(taskName, facility.id, facility) {
            StopWatch stopWatch = new StopWatch("Sending emails to ${collectionToIterate?.size()} customers")
            stopWatch.start()

            int numberOfThreads = grailsApplication.config.matchi.threading.numberOfThreads
            int batchSize = grailsApplication.config.matchi.email.batchSize

            log.info("Sending " + collectionToIterate.size() + " emails for " + facility.name + " with " + numberOfThreads + " threads and in batches of " + batchSize)


            GParsPool.withPool(numberOfThreads) {
                collectionToIterate.collate(batchSize).eachParallel {
                    closure.call(it)
                }
            }

            stopWatch.stop()
            log.info stopWatch.shortSummary()
            log.info "Sent ${collectionToIterate.size()} emails in ${stopWatch.totalTimeSeconds} seconds (${collectionToIterate?.size() / stopWatch.totalTimeSeconds} / email)"
        }
    }

    private def createDayLightComponent() {
        Daylight daylight = new Daylight()
        daylight.properties.add(new TzOffsetFrom('+0100'))
        daylight.properties.add(new TzOffsetTo('+0200'))
        daylight.properties.add(new TzName('CEST'))
        daylight.properties.add(new DtStart('19700329T020000'))
        daylight.properties.add(new RRule('FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU'))

        return daylight
    }

    private def createStandardComponent() {
        Standard standard = new Standard()
        standard.properties.add(new TzOffsetFrom('+0200'))
        standard.properties.add(new TzOffsetTo('+0100'))
        standard.properties.add(new TzName('CET'))
        standard.properties.add(new DtStart('19701025T030000'))
        standard.properties.add(new RRule('FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU'))

        return standard
    }

    private def createComponentList(Component... components) {
        if (!components.size()) {
            return null
        }

        ComponentList componentList = new ComponentList()
        components.each { componentList.add(it) }

        return componentList
    }

    private def createVTimezoneComponent() {
        def dayLightComponent = createDayLightComponent()
        def standardComponent = createStandardComponent()
        def components = createComponentList(dayLightComponent, standardComponent)

        VTimeZone vTimeZone = new VTimeZone(components)
        vTimeZone.properties.add(new TzId('Europe/Stockholm'))
        vTimeZone.properties.add(new TzUrl(new URI('http://tzurl.org/zoneinfo-outlook/Europe/Stockholm')))

        return vTimeZone
    }

    //This method just helps us to track invalid emails and then correct them manually
    void validateAndLogWrongEmails(Customer customer) {
        if (!customer.email) {
            log.error("Email is null for customer: ${customer.id}")
        }
        if (customer.email.isEmpty()) {
            log.error("Email is empty for customer: ${customer.id}")
        }
        if (!Validator.isMailbox(customer.email)) {
            log.error("Email [${customer.email}] is not valid address for customer: ${customer.id}")
        }
    }

    def void sendChangedPaymentInfo(String facility, String plusgiro = null, String bankgiro = null) {
        String toEmail = 'finance@matchi.se'
        def mailLocale = new Locale('sv')
        String template = "paymentInfoAdminMail"

        asynchronousMailService.sendAsynchronousMail {
            locale mailLocale
            to toEmail
            from mailAddressNoReply
            subject messageSource.getMessage("templates.emails.html.paymentInfoChanged.text", null, mailLocale)
            html(view: "${templateRootURI}/${template}", model: [facility: facility, plusgiro: plusgiro, bankgiro: bankgiro])
            priority PRIORITY_DEFAULT
        }
    }
}
