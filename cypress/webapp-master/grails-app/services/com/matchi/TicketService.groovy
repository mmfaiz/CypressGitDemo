package com.matchi

import grails.transaction.NotTransactional
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.DateTime

class TicketService {
    def grailsApplication

    static transactional = true

    /**
     * Creates a reset password tickets for a user
     * @param user
     * @return A ResetPasswordTicket
     */
    def createResetPasswordTicket(User user) {
        checkUserValidForPasswordTicket(user)
        def expireDate = getTicketExpireDate()

        invalidateExistingResetPasswordTickets(user)

        def ticket = new ResetPasswordTicket()
        ticket.user     = user
        ticket.expires  = expireDate
        ticket.key      = generateKey()

        return ticket.save()
    }

    /**
     * Consumes a reset password ticket
     * @param key Ticket key
     * @return A Ticket if key and ticket is valid, otherwise null
     */
    def useResetPasswordTicket(def key) {
        ResetPasswordTicket ticket = ResetPasswordTicket.findByKeyAndConsumedIsNull(key)

        if(ticket) {
            if(!ticket.isValid())  {
                return null
            }

            ticket.consumed = new Date()
            ticket.save()
        }

        return ticket
    }

    def useChangeEmailTicket(def key) {
        ChangeEmailTicket ticket = ChangeEmailTicket.findByKeyAndConsumedIsNull(key)

        if(ticket) {
            if(!ticket.isValid())  {
                return null
            }

            ticket.consumed = new Date()
            ticket.save()
        }

        return ticket
    }

    /**
     * Connects user with customer and consumes a customer invite ticket
     * @param key Ticket key, User user
     * @return A Ticket if key and ticket is valid, otherwise null
     */
    def useInviteTicket(def key, User user) {
        CustomerInviteTicket ticket = CustomerInviteTicket.findByKey(key)

        if (ticket) {
            if (!ticket.isValid()) {
                return null
            }

            def customer = ticket.customer
            customer.user  = user
            customer.email = user.email
            customer.save()

            consumeInviteTicket(ticket)

            log.info("Invite ticket used by ${user.email} on ${customer.facility}")
        }

        return ticket
    }

    /**
     * Consumes a customer invite ticket
     * @param key Ticket to be consumed
     */
    def consumeInviteTicket(def ticket) {
        ticket.consumed = new Date()
        ticket.save()
    }

    /**
     * Creates a invite ticket for a customer
     * @param customer
     * @return A CustomerInviteTicket
     */
    def createCustomerInviteTicket(Customer customer) {
        checkCustomerValidForInviteTicket(customer)

        CustomerInviteTicket ticket = CustomerInviteTicket.createCriteria().list {
            eq('customer', customer)
            isNull('consumed')
            gt('expires', new Date())
        }?.getAt(0)
        if (!ticket) {
            invalidateExistingCustomerInviteTickets(customer)

            ticket = new CustomerInviteTicket()
            ticket.customer = customer
            ticket.expires = new DateTime().plusMonths(5).toDate() // Expires in two months
            ticket.key = generateKey()

            ticket.save(flush: true)
        }

        return ticket
    }

    /**
     * Consumes a customer update ticket
     * @param key Ticket to be consumed
     */
    def consumeUpdateRequestTicket(def ticket) {
        ticket.consumed = new Date()
        ticket.save()
    }

    /**
     * Creates a update ticket for a customer
     * @param customer
     * @return A CustomerUpdateRequestTicket
     */
    def createCustomerUpdateTicket(Customer customer) {
        invalidateExistingCustomerUpdateRequestTickets(customer)

        def ticket = new CustomerUpdateRequestTicket()
        ticket.customer = customer
        ticket.expires  = new DateTime().plusMonths(2).toDate() // Expires in two months
        ticket.key      = generateKey()

        return ticket.save()
    }

    @NotTransactional
    def isUpdateRequestTicketValid(def key) {
        CustomerUpdateRequestTicket ticket = CustomerUpdateRequestTicket.findByKeyAndConsumedIsNull(key)
        if(!ticket) {
            return false
        }

        return ticket.isValid()
    }

    @NotTransactional
    def isInviteTicketValid(def key) {
        CustomerInviteTicket ticket = CustomerInviteTicket.findByKeyAndConsumedIsNull(key)
        if(!ticket) {
            return false
        }

        return ticket.isValid()
    }

    @NotTransactional
    def isChangeMailTicketValid(def key) {
        ChangeEmailTicket ticket = ChangeEmailTicket.findByKeyAndConsumedIsNull(key)
        if(!ticket) {
            return false
        }

        return ticket.isValid()
    }

    @NotTransactional
    def isTicketValid(def key) {
        ResetPasswordTicket ticket = ResetPasswordTicket.findByKeyAndConsumedIsNull(key)
        if(!ticket) {
            return false
        }

        return ticket.isValid()
    }

    private def invalidateExistingChangeEmailTickets(User user) {
        def existingTickets = ChangeEmailTicket.findAllByUser(user)
        if (existingTickets) {
            existingTickets.each {
                it.consumed = new Date()
                it.save()
            }
        }
    }

    private def invalidateExistingResetPasswordTickets(User user) {
        def existingTickets = ResetPasswordTicket.findAllByUser(user)
        if (existingTickets) {
            existingTickets.each {
                it.consumed = new Date()
                it.save()
            }
        }
    }

    private def invalidateExistingCustomerUpdateRequestTickets(Customer customer) {
        def existingTickets = CustomerUpdateRequestTicket.findAllByCustomer(customer)
        if (existingTickets) {
            existingTickets.each {
                it.consumed = new Date()
                it.save()
            }
        }
    }

    private def invalidateExistingCustomerInviteTickets(Customer customer) {
        def existingTickets = CustomerInviteTicket.findAllByCustomer(customer)
        if (existingTickets) {
            existingTickets.each {
                it.consumed = new Date()
                it.save()
            }
        }
    }

    private def checkUserValidForPasswordTicket(User user) {
        if (!user) {
            throw new IllegalArgumentException("Unable to generate password ticket on null user")
        }
    }

    private def checkCustomerValidForInviteTicket(Customer customer) {
        if (customer.user) {
            throw new IllegalArgumentException("Unable to generate invite ticket since user already has a user")
        }
    }

    private def getTicketExpireDate() {
        def expiresInDays = grailsApplication.config.ticket.resetPassword.expiresInDays

        if (expiresInDays == null) {
            throw new IllegalStateException("Config 'ticket.resetPassword.expiresInDays' is not set")
        }

        if (!String.valueOf(expiresInDays).isInteger() || expiresInDays == 0) {
            throw new IllegalStateException("Unparsable integer in 'ticket.resetPassword.expiresInDays' (${expiresInDays})")
        }

        return new DateTime().plusDays(expiresInDays).toDate()
    }

    @NotTransactional
    void assertConfigurationSettings() {
        getTicketExpireDate()
    }

    CustomerDisableMessagesTicket createCustomerDisableMessagesTicket(Customer customer) {
        def ticket = new CustomerDisableMessagesTicket()
        ticket.customer = customer
        ticket.expires = new DateTime().plusMonths(1).toDate() // Expires in 1 month
        ticket.key = generateKey()

        return ticket.save()
    }

    void consumeCustomerDisableMessagesTicket(CustomerDisableMessagesTicket ticket) {
        ticket.consumed = new Date()
        ticket.save()
    }

    def generateKey() {
        return RandomStringUtils.random(30, true, true)
    }

    BookingCancelTicket createBookingCancelTicket(Booking booking, Integer hoursToExpire = 24) {
        def ticket = new BookingCancelTicket()
        ticket.bookingId = booking.id
        ticket.bookingCustomerId = booking.customer.id
        ticket.expires = new DateTime().plusHours(hoursToExpire).toDate() // Expires in 1 day by default
        ticket.key = generateKey()

        return ticket.save()
    }

    ChangeEmailTicket createChangeEmailTicket(User user, String newEmail) {
        def ticket      = new ChangeEmailTicket()
        ticket.user     = user
        ticket.newEmail    = newEmail
        ticket.expires  = new DateTime().plusHours(24).toDate() // Expires in 1 day by default
        ticket.key      = generateKey()

        return ticket.save()
    }


    void consumeBookingCancelTicket(BookingCancelTicket ticket) {
        ticket.consumed = new Date()
        ticket.save()
    }
}
