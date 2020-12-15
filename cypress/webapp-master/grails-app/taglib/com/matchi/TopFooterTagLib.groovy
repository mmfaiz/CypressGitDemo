package com.matchi

class TopFooterTagLib {
    def topFooterAds = { attrs, body ->
        def ad1 = [img: "padel-sweden.png", link: "https://svenskpadel.se/"]
        def ad2 = [img: "sv-tennis-association.png", link: "https://www.tennis.se/"]
        def ad3 = [img: "sv-tennis-magasinet.png", link: "http://www.tennismagasinet.se/"]
        def ad4 = [img: "work-at-matchi.jpg", link: "http://jobs.matchi.se/"]

        // Object to sent back
        def ads = [ ad1, ad2, ad3, ad4 ]

        out << render(template:"/templates/general/topFooterAds", model: [ads:ads])
    }
    def topFooterStats = { attrs, body ->
        def users     = [ icon: "user", number: User.count(),  text: message(code: "topFooter.topFooterStats.users") ]
        def clubs     = [ icon: "flag-checkered", number: Facility.countByActive(true, [cache:true]),  text: message(code: "topFooter.topFooterStats.facilities") ]
        def courts    = [ icon: "map-marker", number: Court.countByArchived(false, [cache:true]), text: message(code: "book.facilities.bookableCourts") ]
        //def bookings  = [ icon: "check", number: Booking.count(), text: message(code: "topFooter.topFooterStats.bookings") ]

        // Object to sent back
        def stats = [ clubs, courts ]

        out << render(template:"/templates/general/topFooterStats", model: [stats:stats])
    }
}
