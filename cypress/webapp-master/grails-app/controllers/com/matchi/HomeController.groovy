package com.matchi


import grails.validation.Validateable
import org.springframework.web.servlet.support.RequestContextUtils

class HomeController extends GenericController {

    def fileArchiveService
    def notificationService

    def index() {
        if(getCurrentUser()) {
            redirect(controller: "userProfile", action: "home")
        }
    }
    def about() {
        [locale: g.locale()]
    }
    def privacypolicy() {} // NOTE! Used in TermsTagLib.
    def integritypolicy() {redirect(action: "privacypolicy")}
    def useragreement() {} // NOTE! Used in TermsTagLib.

    def customers() {
        def facInfo = []
        def query = "from Facility as f where f.active=true and f.bookable=true"
        def facilities = Facility.findAll(query, [cache:true])

        facilities.each {
            facInfo << [
                nrCourts: it.courts.findAll { !it.offlineOnly && !it.archived }.size(),
                logotype: it.facilityLogotypeImage ? fileArchiveService.getFileURL(it.facilityLogotypeImage) : resource(dir:"/images", file:"facility_tmp_logo.png")

            ]
        }

        [ facInfo: facInfo ]
    }
    def getmatchi() {

        def facInfo = []
        def query = "from Facility as f where f.active=true and (f.id=22 or f.id=44 or f.id = 20 or f.id = 12)"
        def facilities = Facility.findAll(query, [cache: true])

        facilities.each {
            facInfo << [
                nrCourts: it.courts.findAll { !it.offlineOnly }.size(),
                logotype: it.facilityLogotypeImage ? fileArchiveService.getFileURL(it.facilityLogotypeImage) : resource(dir:"/images", file:"facility_tmp_logo.png")
            ]
        }

        [ facInfo: facInfo ]
    }

    def interested(InterestedCommand cmd) {
        if (cmd.hasErrors()) {
            redirect(action: "getmatchi", params: [cmd:cmd])
            return
        }

        /**
         * TODO: Add InterestedCommand name to ContactMe
         */
        def contactMe = new ContactMe(name: cmd.name, email: cmd.email, facility: cmd.facility, phone: cmd.phone).save()
        notificationService.sendContactMeNotification(contactMe)

        flash.message = message(code: "home.interested.message")
        redirect(action: "getmatchi")
    }
}

@Validateable(nullable = true)
class SignUpCommand {
    String type
    String email
    String facility
}
@Validateable(nullable = true)
class InterestedCommand {
    String name
    String email
    String facility
    String phone

    static constraints = {
        name(nullable: true)
        email(email: true, nullable: false, blank: false)
        facility(nullable: false, blank: false)
        phone(nullable: true)
    }
}
