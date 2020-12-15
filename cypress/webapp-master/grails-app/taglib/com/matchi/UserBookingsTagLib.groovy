package com.matchi

class UserBookingsTagLib {
	def springSecurityService
    def bookingService
	
	def userBookings = { attrs, body ->
		def user = springSecurityService.getCurrentUser()
		def bookings = bookingService.getUserBookings(user)

		out << render(template:"/templates/userBookings", model: [bookings:bookings])
	}

    def userRequests = { attrs, body ->
		def user = springSecurityService.getCurrentUser()
        def bookings = bookingService.getUserBookings(user)

		out << render(template:"/templates/userRequests", model: [bookings:bookings])
	}

    def userWhiteLabelBookings = { attrs, body ->

        Facility facility = attrs.facility
        def user = springSecurityService.getCurrentUser()
        def bookings = bookingService.getUserBookings(user, [ facility ])

        out << render(template:"/templates/user/bookings", model: [ bookings:bookings ])
    }
}
