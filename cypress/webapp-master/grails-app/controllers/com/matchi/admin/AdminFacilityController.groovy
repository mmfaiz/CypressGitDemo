package com.matchi.admin

import com.matchi.*
import com.matchi.enums.BookingGroupType
import com.matchi.idrottonline.ActivityOccasionOccurence
import com.matchi.idrottonline.IdrottOnlineService
import com.matchi.membership.MembershipType
import com.matchi.mpc.CodeRequest
import com.matchi.price.CustomerPriceCondition
import com.matchi.price.MemberPriceCondition
import com.matchi.price.PriceListCustomerCategory
import grails.converters.JSON
import grails.plugins.rest.client.RestResponse
import grails.validation.Validateable
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.LocalDate
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

class AdminFacilityController {

    def fileArchiveService
    def facilityService
    def fortnox3CustomerService
    def fortnox3Service
    def mpcService
    def courtService
    IdrottOnlineService idrottOnlineService
    ActivityService activityService
    NotificationService notificationService
    def messageSource

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index(AdminFacilityCommand cmd) {
        def activeFacilities = findAdminFacilities(true, cmd)
        [facilities: activeFacilities, facilityInstanceTotal: activeFacilities.size(), cmd: cmd]
    }

    def archivedFacilities(AdminFacilityCommand cmd) {
        def archivedFacilities = findAdminFacilities(false, cmd)
        render(view: "index", model: [facilities: archivedFacilities, facilityInstanceTotal: archivedFacilities.size(), cmd: cmd])
    }

    def create() {
        def facility = new Facility()
        facility.bookingRuleNumDaysBookable = 10
        facility.properties = params
        facility.vat = 6

        return [facility: facility, regions: Region.getAll(), sports: facility?.listSports()]
    }

    def save() {

        def facilityInstance = new Facility()

        copyProperties(facilityInstance, params);

        params.sports.each() {
            facilityInstance.addToSports(Sport.findById(it))
        }

        facilityService.updateAvailability(facilityInstance, params)

        facilityInstance.municipality = Municipality.findById(params.municipality)

        facilityInstance.multisport = params.multisport

        if (!facilityInstance.hasErrors() && facilityService.saveFacility(facilityInstance)) {
            processImages(facilityInstance)

            String memberString = messageSource.getMessage("default.member.label", null, new Locale(facilityInstance.language))
            String customerString = messageSource.getMessage("customer.label", null, new Locale(facilityInstance.language))
            String standardString = messageSource.getMessage("standard.label", null, new Locale(facilityInstance.language))

            PriceListCustomerCategory stdCategory = new PriceListCustomerCategory(name: standardString, defaultCategory: true)
            CustomerPriceCondition customer = new CustomerPriceCondition(name: customerString, description: "Desc")

            PriceListCustomerCategory membCategory = new PriceListCustomerCategory(name: memberString, defaultCategory: true)
            MemberPriceCondition member = new MemberPriceCondition(name: memberString, description: "Desc")

            membCategory.addToConditions(member)
            stdCategory.addToConditions(customer)
            facilityInstance.addToPriceListCustomerCategories(stdCategory)
            facilityInstance.addToPriceListCustomerCategories(membCategory)

            MembershipType seniorType = new MembershipType(name: "Senior", facility: facilityInstance)
            MembershipType juniorType = new MembershipType(name: "Junior", facility: facilityInstance)
            facilityInstance.addToMembershipTypes(seniorType)
            facilityInstance.addToMembershipTypes(juniorType)

            facilityInstance.setFacilityProperty(FacilityProperty.FacilityPropertyKey.PAYOUT_PLUSGIRO, params.plusgiro)
            facilityInstance.setFacilityProperty(FacilityProperty.FacilityPropertyKey.PAYOUT_BANKGIRO, params.bankgiro)
            /*Add default properties*/
            facilityInstance.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_SMS, FacilityProperty.FacilityPropertyKey.FEATURE_SMS.defaultValue)
            facilityInstance.setFacilityProperty(FacilityProperty.FacilityPropertyKey.SMS_FROM, facilityInstance.name)
            facilityInstance.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT, FacilityProperty.FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT.defaultValue)
            facilityInstance.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_GIFT_CARDS, FacilityProperty.FacilityPropertyKey.FEATURE_GIFT_CARDS.defaultValue)
            facilityInstance.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE, FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE.defaultValue)
            facilityInstance.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FACILITY_UPCOMING_OCCASIONS_NUMBER, FacilityProperty.FacilityPropertyKey.FACILITY_UPCOMING_OCCASIONS_NUMBER.defaultValue)


            facilityService.saveFacility(facilityInstance)

            flash.message = "${message(code: 'default.created.message', args: [message(code: 'facility.label', default: 'Facility'), facilityInstance.id])}"
            redirect(action: "index")
        } else {
            def allregions = Region.getAll()
            def allSports = facilityInstance?.listSports()
            render(view: "create", model: [facility: facilityInstance, regions: allregions, sports: allSports])
        }
    }

    def edit() {
        def facility = facilityService.getFacility(params.id)

        facility.facilityLogotypeImage
        facility.facilityOverviewImage
        facility.facilityWelcomeImage

        if (!facility) {
            flash.message = "Hittade inte anläggningen"
            redirect(action: "index")
        } else {
            def allregions = Region.list([cache: true])
            def allSports = facility?.listSports()
            return [facility: facility, regions: allregions, sports: allSports]
        }
    }

    def update() {

        def facilityInstance = Facility.get(params.id)
        if (facilityInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (facilityInstance.version > version) {
                    facilityInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'facility.label', default: 'Facility')] as Object[], "Another user has updated this Facility while you were editing")
                    render(view: "edit", model: [facilityInstance: facilityInstance])
                    return
                }
            }

            facilityInstance.municipality = Municipality.findById(params.municipality)
            copyProperties(facilityInstance, params);

            facilityInstance.multisport = params.multisport

            def sports = []
            params.list("sports").each {
                sports << Sport.findById(it)
            }
            def sportsToRemove = facilityInstance.sports - sports
            def sportsToadd = sports - facilityInstance.sports
            sportsToRemove?.each { Sport s ->
                facilityInstance.removeFromSports(s)
            }
            sportsToadd?.each { Sport s ->
                facilityInstance.addToSports(s)
            }

            sendPaymentInfo(facilityInstance, params.plusgiro, params.bankgiro)
            facilityInstance.setFacilityProperty(FacilityProperty.FacilityPropertyKey.PAYOUT_PLUSGIRO, params.plusgiro)
            facilityInstance.setFacilityProperty(FacilityProperty.FacilityPropertyKey.PAYOUT_BANKGIRO, params.bankgiro)

            courtService.organizeSports(facilityInstance)

            facilityService.updateAvailability(facilityInstance, params)

            if (params.defaultBookingCustomerId) {
                facilityInstance.defaultBookingCustomer = Customer.findById(params.defaultBookingCustomerId)
            } else {
                facilityInstance.defaultBookingCustomer = null
            }

            facilityInstance.relatedBookingsCustomer = params.relatedBookingsCustomerId ?
                    Customer.findById(params.relatedBookingsCustomerId) : null

            if (!facilityInstance.hasErrors() && facilityService.saveFacility(facilityInstance)) {
                processImages(facilityInstance)

                flash.message = message(code: "adminFacility.update.success")
                redirect(action: "edit", params: [id: facilityInstance.id])
            } else {
                def allRegions = Region.getAll()
                def allSports = facilityInstance?.listSports()
                render(view: "edit", model: [facility: facilityInstance, regions: allRegions, sports: allSports])
            }
        } else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'facility.label', default: 'Facility'), params.id])}"
            redirect(action: "index")
        }
    }

    def deleteImage() {
        def textId = params.textId
        def facility = Facility.get(params.id)

        log.info("Delete facility image (${textId}) on facility ${facility.name}")

        if (textId) {
            def fileToRemove

            if (facility?.facilityOverviewImage?.textId?.equals(textId)) {
                fileToRemove = facility.facilityOverviewImage
                facility.facilityOverviewImage = null;
            }

            if (facility?.facilityLogotypeImage?.textId?.equals(textId)) {
                fileToRemove = facility.facilityLogotypeImage
                facility.facilityLogotypeImage = null;
            }

            if (facility?.facilityWelcomeImage?.textId?.equals(textId)) {
                fileToRemove = facility.facilityWelcomeImage
                facility.facilityWelcomeImage = null;
            }

            if (fileToRemove) {
                fileArchiveService.removeFile(fileToRemove)
            }

        }
        redirect(url: params.returnUrl)
    }

    def mpc() {
        def facility = facilityService.getFacility(params.id)

        [facility: facility, mpcSettings: mpcService.getNode(facility.id), providers: mpcService.listProviders()]
    }

    def updateMpc() {
        def map = [:]

        String searthString = "configurationKey_"

        params.each {
            String paramKey = it.key.toString()
            if (paramKey.startsWith(searthString)) {
                int i = paramKey.substring(searthString.length()).toInteger()

                def key = params.get("configurationKey_" + i)
                def value = params.get("configurationValue_" + i)

                map.put(key, value)
            }
        }

        mpcService.updateNode(params.long("facilityId"), map)

        flash.message = "Updated MPC"
        redirect(action: "mpc", id: params.facilityId)
    }

    def createMpc() {
        mpcService.createNode(params.long("facilityId"), params.long("provider"))

        flash.message = "Created MPC"
        redirect(action: "mpc", id: params.facilityId)
    }

    def testFortnox3Values(Long id) {
        def facility = Facility.load(id)

        if (facility.hasFortnox() && fortnox3CustomerService.isFortnoxEnabledForFacility(facility)) {
            render "Successfully connected to Fortnox 3 API"
        } else {
            render "ERR"
        }
    }

    def renewAccessToken(Long id, String authCode) {
        def facility = Facility.load(id)
        render fortnox3Service.retrieveAccessToken(facility, authCode) ?: 'ERR'
    }


    private void copyProperties(Facility facility, def params) {
        log.debug(params)
        facility.name = params.name
        facility.shortname = params.shortname
        facility.description = params.description
        facility.email = params.email
        facility.address = params.address
        facility.lat = Double.parseDouble(params.lat)
        facility.lng = Double.parseDouble(params.lng)
        facility.zipcode = params.zipcode
        facility.city = params.city
        facility.country = params.country
        facility.telephone = params.telephone
        facility.fax = params.fax
        facility.iban = params.iban
        facility.bic = params.bic
        facility.orgnr = params.orgnr
        facility.facebook = params.facebook
        facility.twitter = params.twitter
        facility.instagram = params.instagram
        facility.invoicing = params.invoicing != null
        facility.showBookingHolder = params.showBookingHolder ?: false
        facility.whetherToSendEmailConfirmationByDefault = params.whetherToSendEmailConfirmationByDefault ?: false
        facility.vat = params.getInt("vat")
        facility.apikey = facilityService.generateApiKey(params.shortname)

        facility.enabled = params.enabled != null
        facility.active = params.active != null
        facility.bookable = params.bookable != null
        facility.boxnet = params.boxnet != null

        facility.bookingRuleNumDaysBookable = params.getInt("bookingRuleNumDaysBookable")

        facility.language = params.language
        facility.currency = params.currency

        facility.requireSecurityNumber = params.requireSecurityNumber ?: false
        facility.salesPerson = params.salesPerson

    }

    private void processImages(Facility facility) {

        def facilityOverviewImage = retrieveUploadedImage("facilityOverviewImage");
        def facilityWelcomeImage = retrieveUploadedImage("facilityWelcomeImage");
        def facilityLogotypeImage = retrieveUploadedImage("facilityLogotypeImage");

        if (facilityOverviewImage) {
            facility.facilityOverviewImage = facilityOverviewImage
        }

        if (facilityWelcomeImage) {
            facility.facilityWelcomeImage = facilityWelcomeImage
        }

        if (facilityLogotypeImage) {
            facility.facilityLogotypeImage = facilityLogotypeImage
        }
    }

    private def retrieveUploadedImage(def name) {
        MultipartHttpServletRequest mpr = (MultipartHttpServletRequest) request;
        CommonsMultipartFile image = (CommonsMultipartFile) mpr.getFile(name);

        if (!image.isEmpty()) {
            return fileArchiveService.storeFile(image);
        }

        return null
    }

    def delete() {
        def facilityInstance = Facility.get(params.id)
        if (facilityInstance) {
            try {
                facilityInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'facility.label', default: 'Facility'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'facility.label', default: 'Facility'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        } else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'facility.label', default: 'Facility'), params.id])}"
            redirect(action: "list")
        }
    }

    def addCodeRequest(Long id) {
        def facilityInstance = Facility.get(id)
        Date now = new Date()

        List<Long> existingCodeRequestIds = CodeRequest.withCriteria {
            createAlias("booking", "b")
            createAlias("b.slot", "s")
            createAlias("s.court", "c")
            eq("c.facility", facilityInstance)
            gt("s.startTime", now)
            projections {
                property("b.id")
            }
        }

        List<Booking> bookingsToSend = Booking.withCriteria {
            createAlias("slot", "s")
            createAlias("s.court", "c")
            createAlias("group", "g", CriteriaSpecification.LEFT_JOIN)
            eq("c.facility", facilityInstance)
            gt("s.startTime", now)

            or {
                isNull("group")
                ne("g.type", BookingGroupType.NOT_AVAILABLE)
            }

            not {
                inList("id", existingCodeRequestIds ?: [-1L])
            }
        }

        log.info "Queueing ${bookingsToSend?.size()} for facility ${facilityInstance.name}"

        mpcService.queue(bookingsToSend)

        flash.message = message(code: "adminFacility.addCodeRequest.success")

        redirect(action: "edit", params: [id: facilityInstance.id])
    }

    def clearMissingCodeRequests(Long id) {
        Facility facilityInstance = Facility.get(id)
        Date now = new Date()

        if (facilityInstance) {
            RestResponse response = mpcService.listFuture(id)

            List<String> mpcIds = []

            response.json.each {
                String mpcId = it.id
                mpcIds << mpcId
            }

            // Delete future, local CodeRequests for this facility not having an mpc id in the list fetched
            List<CodeRequest> existingCodeRequestsToDelete = CodeRequest.withCriteria {
                createAlias("booking", "b")
                createAlias("b.slot", "s")
                createAlias("s.court", "c")
                eq("c.facility", facilityInstance)
                gt("s.endTime", now)

                // If not any mpc ids, delete them all!
                if (mpcIds?.size() > 0) {
                    not {
                        inList('mpcId', mpcIds)
                    }
                }
            }

            log.info "Deleting ${existingCodeRequestsToDelete?.size()} code requests missing in MPC"

            existingCodeRequestsToDelete.each { CodeRequest codeRequest ->
                codeRequest.delete()
            }

            flash.message = "Deleted ${existingCodeRequestsToDelete?.size()} that were missing locally"
        }

        redirect(action: "edit", params: [id: facilityInstance.id])
    }

    def resendMPC(MpcEditCommand cmd) {
        if (cmd.hasErrors()) {
            flash.error = "Error"
            redirect(action: "index")
        }

        Facility facilityInstance = Facility.get(cmd.facilityId)

        if (facilityInstance) {
            RestResponse response = mpcService.resend(facilityInstance.id)

            if (response.statusCode == HttpStatus.OK) {
                log.info "Resending MPC CRs to facility ${facilityInstance.name}"
                flash.message = "Resent MPC CRs to facility ${facilityInstance.name}. It will take a few minutes before they arrive."
            } else {
                log.error "Error on resending MPC CRs to facility ${facilityInstance.name}"
                flash.error = "Error when resending MPC CRs to facility ${facilityInstance.name}"
            }
        }

        redirect(action: "mpc", params: [id: facilityInstance.id])
    }

    def resetMPC(MpcEditCommand cmd) {
        if (cmd.hasErrors()) {
            flash.error = "Error"
            redirect(action: "index")
        }

        Facility facilityInstance = Facility.get(cmd.facilityId)
        log.info "Resetting MPC for ${facilityInstance.name}"

        if (facilityInstance) {
            RestResponse response = mpcService.reset(facilityInstance.id)

            if (response.statusCode == HttpStatus.OK) {
                FacilityProperty.withTransaction {
                    List<CodeRequest> codeRequests = CodeRequest.createCriteria().list {
                        createAlias('booking', 'b')
                        createAlias('b.customer', 'c')

                        eq('c.facility', facilityInstance)
                    }

                    log.info "Deleting ${codeRequests?.size()} code requests in MATCHi"

                    CodeRequest.deleteAll(codeRequests)

                    FacilityProperty fp = FacilityProperty.findByFacilityAndKey(facilityInstance, FacilityProperty.FacilityPropertyKey.FEATURE_MPC.name())
                    facilityInstance.removeFromFacilityProperties(fp)
                    fp.delete()
                }

                log.info "Clearing MPC for facility ${facilityInstance.name}"
                flash.message = "Clearing MPC for facility ${facilityInstance.name} went well. Communicate back to provider for clearing on their part."
            } else {
                log.error "Clearing MPC for facility ${facilityInstance.name}"
                flash.error = "Error when resending MPC CRs to facility ${facilityInstance.name}"
            }
        }

        redirect(action: "edit", params: [id: facilityInstance.id])
    }

    def ioAllMembersSync(Long id) {
        Facility facility = getFacilityOrRedirectError(id)
        if (!facility) return

        idrottOnlineService.importCustomers(facility, idrottOnlineService.getAllMembers(facility), true)
        ioSyncSuccessAndRedirect(facility.id)
    }

    def ioActiveOrTerminatedMembersSync(Long id) {
        Facility facility = getFacilityOrRedirectError(id)
        if (!facility) return

        idrottOnlineService.importCustomers(facility, idrottOnlineService.getActiveOrTerminatedMembers(facility), true)
        ioSyncSuccessAndRedirect(facility.id)
    }

    def ioActivitiesSync(Long id, String startDateString, String endDateString) {
        Facility facility = getFacilityOrRedirectError(id)
        if (!facility) return

        LocalDate startDate = new LocalDate(startDateString)
        LocalDate endDate = new LocalDate(endDateString)

        List<ActivityOccasionOccurence> activityOccasions = activityService.findCourseOccasionByRange(facility, startDate, endDate)
        idrottOnlineService.importActivityOccasions(facility, activityOccasions, true)

        ioSyncSuccessAndRedirect(facility.id)
    }

    private Facility getFacilityOrRedirectError(Long id) {
        def facility = facilityService.getFacility(id)
        if (facility) return facility

        flash.message = message(code: 'default.not.found.message', args: [message(code: 'facility.label', default: 'Facility'), id])
        redirect(action: "index")
        return null
    }

    private void ioSyncSuccessAndRedirect(Long facilityId) {
        flash.message = message(code: "adminFacility.ioSync.success")
        redirect(action: "edit", params: [id: facilityId])

    }

    def sports(Boolean multisport) {
        def facility = facilityService.getFacility(params.id)
        def result = []

        def sports = multisport ? Sport.list() : Sport.coreSportAndOther.list()

        sports.each {
            result << [sport: it, selected: (facility ? facility?.sports?.contains(it) : false)]
        }

        render result as JSON
    }

    private def findAdminFacilities(boolean enabled, AdminFacilityCommand cmd) {
        def facilities = Facility.list().findAll { it.enabled == enabled }
        if (cmd.facilityName || cmd.fortnoxId) {
            facilities = Facility.createCriteria().listDistinct() {
                eq("enabled", enabled)
                if (cmd.facilityName) {
                    or {
                        like("name", "%${cmd.facilityName}%")
                    }
                }
            }
            if (cmd.fortnoxId) {
                facilities = facilities.findAll { cmd.fortnoxId.equals(it.getFortnoxCustomerId()) }
            }
        }
        facilities
    }

    private void sendPaymentInfo(Facility facility, String plusgiro, String bankgiro) {
        String facilityPlusgiro = facility.getFacilityProperty(FacilityProperty.FacilityPropertyKey.PAYOUT_PLUSGIRO)?.value
        String facilityBankgiro = facility.getFacilityProperty(FacilityProperty.FacilityPropertyKey.PAYOUT_BANKGIRO)?.value
        boolean plusgiroChanged = plusgiro && !facilityPlusgiro.equals(plusgiro)
        boolean bankgiroChanged = bankgiro && !facilityBankgiro.equals(bankgiro)
        if (plusgiroChanged && bankgiroChanged) {
            notificationService.sendChangedPaymentInfo(facility.name, plusgiro, bankgiro)
        } else if (plusgiroChanged) {
            notificationService.sendChangedPaymentInfo(facility.name, plusgiro)
        } else if (bankgiroChanged) {
            notificationService.sendChangedPaymentInfo(facility.name, null, bankgiro)
        }
    }
}

@Validateable(nullable = true)
class MpcEditCommand {
    Long facilityId

    static constraints = {
        facilityId nullable: false
    }
}

@Validateable(nullable = true)
class AdminFacilityCommand {
    String fortnoxId
    String facilityName
}