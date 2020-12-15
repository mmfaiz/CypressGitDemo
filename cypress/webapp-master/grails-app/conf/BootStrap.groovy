import com.matchi.*
import com.matchi.membership.MembershipType
import com.matchi.price.CustomerPriceCondition
import com.matchi.price.MemberPriceCondition
import com.matchi.price.Price
import com.matchi.price.PriceListCustomerCategory
import com.matchi.sportprofile.SportAttribute
import com.matchi.sportprofile.SportProfileMindset
import grails.converters.JSON
import grails.util.Environment
import grails.util.Holders
import org.codehaus.groovy.grails.web.converters.configuration.ChainedConverterConfiguration
import org.codehaus.groovy.grails.web.converters.configuration.ConvertersConfigurationHolder
import org.codehaus.groovy.grails.web.converters.configuration.DefaultConverterConfiguration
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalTime
import org.joda.time.Period

import java.text.SimpleDateFormat

import static grails.async.Promises.*
import grails.async.Promise

class BootStrap {

    def springSecurityService
    def facilityService
    def ticketService
    def slotService
    def priceListService
    def cashService
    def memberService
    def grailsApplication
    def kafkaConsumerService

    def init = { servletContext ->
        if (Environment.getCurrent() == Environment.TEST) {
            createTestData()
        }

        ticketService.assertConfigurationSettings()

        // work around for: https://github.com/grails-plugins/grails-rest-client-builder/issues/34
        DefaultConverterConfiguration<JSON> cfg = (DefaultConverterConfiguration<JSON>) ConvertersConfigurationHolder.getConverterConfiguration(JSON)
        ConvertersConfigurationHolder.setDefaultConfiguration(JSON.class, new ChainedConverterConfiguration<JSON>(cfg, cfg.proxyHandler))

        // just to make sure all TLS protocols are available
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2")

        JSON.createNamedConfig('apiV2') {
            com.matchi.marshallers.v2.UserMarshaller.register(it)
        }

        JSON.createNamedConfig( 'IReservation' ) {
            com.matchi.marshallers.IReservationsMarshaller.register(it)
        }

        testConfig()
        startKafkaConsumer()
    }

    def startKafkaConsumer() {
        Promise<Object> p = task {
            kafkaConsumerService.startConsumer()
        }
        p.onError { Throwable err ->
            // If Kafka consumer breaks (which should never happen)
            err.printStackTrace()
        }
    }


    def destroy = {
        }

    // TODO Oh this horror...

    def ROLES = ['ROLE_ADMIN', 'ROLE_USER']
    def SPORTS = ['Tennis', 'Badminton', 'Squash', 'Table tennis', 'Padel', 'Pickleball']
    def SPORTATTRIBUTES = ['FOREHAND', 'BACKHAND', 'SERVE', 'VOLLEY']
    def SPORTPROFILEMINDSETS = [[name: 'FOR_FUN', color: 'yellow'], [name: 'TRAINING', color: 'green'], [name: 'MATCH', color: 'blue']]
    def USERS = [
            [email: "calle@matchi.se", firstName: "Calle", lastName: "Carlsson", password: "calle", birthday: "1980-01-01", telephone: "031-031031", roles: ['ROLE_ADMIN', 'ROLE_USER'], municipality: "Göteborg", description: "You see? It's curious. Ted did figure it out - time travel. And when we get back, we gonna tell everyone."],
            [email: "mattias@matchi.se", firstName: "Mattias", lastName: "Lundström", password: "mattias", birthday: "1979-01-01", telephone: "031-031031", roles: ['ROLE_ADMIN', 'ROLE_USER'], municipality: "Göteborg", description: ""],
            [email: "daniel@matchi.se", firstName: "Daniel", lastName: "Ekman", password: "daniel", birthday: "1980-01-01", telephone: "031-031031", roles: ['ROLE_ADMIN', 'ROLE_USER'], municipality: "Göteborg", description: ""],
            [email: "fac@matchi.se", firstName: "Lasse", lastName: "Larsson", password: "fac", birthday: "1980-01-01", telephone: "031-031031", roles: ['ROLE_USER'], municipality: "Malmö", description: ""],
            [email: "fac2@matchi.se", firstName: "Lars", lastName: "Lindqvist", password: "fac", birthday: "1980-01-01", telephone: "031-031031", roles: ['ROLE_USER'], municipality: "Malmö", description: ""],
            [email: "user@matchi.se", firstName: "John", lastName: "Johnsson", password: "user", birthday: "1951-05-03", telephone: "031-031031", roles: ['ROLE_USER'], municipality: "Malmö", description: ""],
            [email: "user2@matchi.se", firstName: "Per", lastName: "Persson", password: "user", birthday: "1961-02-03", telephone: "031-031031", roles: ['ROLE_USER'], municipality: "Stockholm", description: ""],
            [email: "user3@matchi.se", firstName: "Sara", lastName: "Sarasson", password: "user", birthday: "1971-10-03", telephone: "031-031031", roles: ['ROLE_USER'], municipality: "Stockholm", description: ""],
            [email: "test@aby.se", firstName: "Henric", lastName: "Andersson", password: "password", birthday: "1979-01-01", telephone: "XXX-XXXXXX", roles: ['ROLE_USER'], municipality: "Mölndal", description: ""],
    ]
    def MEMBERTYPES = [[name: "Junior", price: 300], [name: "Senior", price: 400]]

    def testConfig() {
        try {
            Facility globalFacility = facilityService.getGlobalFacility()

            if (!globalFacility.name.toLowerCase().contains("matchi")) {
                throw new Exception("Facility doesn't seem to be a MATCHi global Facility")
            }
        }
        catch (Exception exception) {
            log.error("Global facility configuration is not set up properly")
            log.error(exception.toString())
            throw new Exception("killing app")
        }
    }

    def createTestData() {
        log.info("Creating test data:")

        createRoles()
        createRegions()
        createUsers()
        createSports()
        createSportAttributes()
        createSportProfileMindsets()
        createFacilities()

        log.info("Done creating test data")
    }

    def createRoles() {
        log.info(" * Creating User Roles")
        ROLES.each {
            Role.findByAuthority(it) ?: new Role(authority: it).save(failOnError: true)
        }
    }

    def createSports() {
        log.info(" * Creating Sports")

        SPORTS.eachWithIndex { it, i ->
            Sport.findByName(it) ?: new Sport(name: it, position: i).save(failOnError: true)
        }
    }

    def createSportAttributes() {
        log.info(" * Creating Sportattributes")

        SPORTATTRIBUTES.each { def name ->
            SPORTS.each {
                def sport = Sport.findByName(it)

                if (sport) {
                    SportAttribute.findByName(it) ?: sport.addToSportAttributes(new SportAttribute(name: name, description: "Lorem ipsum")).save(failOnError: true)
                }
            }
        }
    }

    def createSportProfileMindsets() {
        log.info(" * Creating SportProfileMindsets")

        SPORTPROFILEMINDSETS.each {
            SportProfileMindset.findByName(it.name) ?: new SportProfileMindset(name: it.name, badgeColor: it.color).save(failOnError: true)
        }
    }

    def createUsers() {
        log.info(" * Creating Users")

        USERS.each {
            def user = addAccount(it.firstName, it.lastName, it.email, it.password, it.telephone, it.birthday, it.municipality, it.description)

            it.roles.each { def authority ->
                def role = Role.findByAuthority(authority)
                if (role) {
                    addRoleToAccount(user, role)
                } else {
                    throw new IllegalStateException("Could not find role ${authority}")
                }

            }
        }
    }

    def createRegions() {
        log.info(" * Creating Regions")
        Region sthlmslan = Region.findByName("Stockholms län") ?: new Region(name: "Stockholms län", lat: 59.32893, lng: 18.06491, zoomlv: 9).save()
        Region vastgot = Region.findByName("Västra Götaland") ?: new Region(name: "Västra Götaland", lat: 58.251727183165066, lng: 13.062744140625, zoomlv: 7).save()
        Region skane = Region.findByName("Skåne") ?: new Region(name: "Skåne", lat: 55.989164255467934, lng: 13.5955810546875, zoomlv: 8).save()

        log.info(" * Creating Municipalities")
        Municipality sto = Municipality.findByName("Stockholm") ?: new Municipality(name: "Stockholm", lat: 59.32893, lng: 18.06491, zoomlv: 11, region: sthlmslan).save()
        Municipality gbg = Municipality.findByName("Göteborg") ?: new Municipality(name: "Göteborg", lat: 57.70887, lng: 11.97456, zoomlv: 11, region: vastgot).save()
        Municipality mal = Municipality.findByName("Malmö") ?: new Municipality(name: "Malmö", lat: 55.60901, lng: 13.00060, zoomlv: 11, region: skane).save()
    }

    def createFacilities() {
        log.info(" * Creating facilities")
        Municipality gbg = Municipality.findByName("Göteborg")

        def tennis = Sport.findById(1) //Tennis
        def badminton = Sport.findById(2) //Badminton

        def facility = addFacility("GLTK", "GLTK", "GLTK tennis", "Töpelsgatan", "Göteborg", gbg, "416 55", "031-773 88 60", 7, 23, "GLTK", 57.70129937574671, 12.02619530000004, true, true, true, "99073", "673111c81b655f37e443a300c7cad969")
        facility.addToSports(tennis)
        facility.addToSports(badminton)

        createCustomers(facility)

        if (facility.courts.size() == 0) {
            facility.addToCourts(new Court(name: "Bana 1", description: "Tennis bana", facility: facility,
                    indoor: true, surface: Court.Surface.HARD, sport: tennis).save(failOnError: true));
            facility.addToCourts(new Court(name: "Bana 2", description: "Tennis bana", facility: facility,
                    indoor: true, surface: Court.Surface.HARD, sport: tennis).save(failOnError: true));
            facility.addToCourts(new Court(name: "Bana 3", description: "Tennis bana", facility: facility,
                    indoor: true, surface: Court.Surface.HARD, sport: tennis, externalScheduling: true, externalId: "03").save(failOnError: true));
        }

        createFacilityPriceList(facility)

        def facility2 = addFacility("Ullevi TK", "ullevitk", "Ullevi TK tennis", "Smålandsgatan 2", "Göteborg", gbg, "411 39", "031-18 01 15", 8, 22, "Ullevi TK", 57.704868786562656, 11.980601400000069, true, true, false, "", "")
        facility2.addToSports(tennis)
        facility2.addToSports(badminton)

        facility2.addToCourts(new Court(name: "Bana 1", description: "Tennis bana", facility: facility2,
                indoor: true, surface: Court.Surface.HARD, sport: tennis).save(failOnError: true));
        facility2.addToCourts(new Court(name: "Bana 2", description: "Tennis bana", facility: facility2,
                indoor: true, surface: Court.Surface.HARD, sport: tennis).save(failOnError: true));
        facility2.addToCourts(new Court(name: "Bana 3", description: "Tennis bana", facility: facility2,
                indoor: false, surface: Court.Surface.LAWN, sport: tennis).save(failOnError: true));

        def facility3 = addFacility("Fjäderborgen", "fjaderborgen", "Badminton anläggning",
                "Södra Viktoriagatan 44", "Göteborg", gbg, "411 30", "031-16 85 10", 7, 23, "Fjäderborgen", 57.694591506082666, 11.966709100000003, true, true, false)

        facility3.addToSports(tennis)

        facility3.addToCourts(new Court(name: "Bana 1", description: "Badminton bana", facility: facility3,
                indoor: true, surface: Court.Surface.HARD, sport: badminton).save(failOnError: true));
        facility3.addToCourts(new Court(name: "Bana 2", description: "Badminton bana", facility: facility3,
                indoor: true, surface: Court.Surface.HARD, sport: badminton).save(failOnError: true));
        facility3.addToCourts(new Court(name: "Bana 3", description: "Badminton bana", facility: facility3,
                indoor: true, surface: Court.Surface.HARD, sport: badminton).save(failOnError: true));

        def facility4 = addFacility("Åby badmintonhall", "abybadminton", "", "Idrottsvägen 3", "Mölndal", gbg, "431 62", "031-86 44 10", 9, 21, "Åby badmintonhall", 57.646515000000015, 11.99944978465578, false, true, false, "", "")

        facility4.addToSports(badminton)

        facility4.addToCourts(new Court(name: "Bana 1", description: "", facility: facility4,
                indoor: true, surface: Court.Surface.HARD, sport: badminton).save(failOnError: true));
        facility4.addToCourts(new Court(name: "Bana 2", description: "", facility: facility4,
                indoor: true, surface: Court.Surface.HARD, sport: badminton).save(failOnError: true));
        facility4.addToCourts(new Court(name: "Bana 3", description: "", facility: facility4,
                indoor: true, surface: Court.Surface.HARD, sport: badminton).save(failOnError: true));

        addDefaultSlotsToFacility(facility)
        addDefaultSlotsToFacility(facility2)
        addDefaultSlotsToFacility(facility3)

        //Change below to move to other facility
        def facilityUser = User.findByEmail("fac@matchi.se")
        facilityUser.facility = facility
        facilityUser.save(failOnError: true)

        def facilityUserML = User.findByEmail("mattias@matchi.se")
        facilityUserML.facility = facility
        facilityUserML.save(failOnError: true)

        def facilityUserCC = User.findByEmail("calle@matchi.se")
        facilityUserCC.facility = facility
        facilityUserCC.save(failOnError: true)

        def facilityUser2 = User.findByEmail("fac2@matchi.se")
        facilityUser2.facility = facility2
        facilityUser2.save(failOnError: true)

        def facilityUser4 = User.findByEmail("test@aby.se")
        facilityUser4.facility = facility4
        facilityUser4.save(failOnError: true)

        def defaultFacility = addFacility("MATCHi default facility", "matchiab", "", "Nellickevägen 22", "GBG", gbg, "41681", "031", 9, 21, "MATCHi AB", 57, 11, false, true, false, "", "")
        defaultFacility.save(failOnError: true, flush: true)

        grailsApplication.config.matchi.defaultFacilityId = defaultFacility.id
    }

    private void createFacilityPriceList(Facility facility) {
        facility.sports.each { sport ->
            log.info(" * Creating ${sport.name} pricelist for facility ${facility.name}")
            def pricelist = new PriceList(startDate: new DateTime().toDate(),
                    sport: sport,
                    name: "Bootstrapped Pricelist")


            priceListService.createPriceList(facility, pricelist)

            pricelist.priceListConditionCategories.each { priceCategory ->
                pricelist.facility.priceListCustomerCategories.each { customerCategory ->


                    Long price = 505l
                    Price.create(price, priceCategory, customerCategory, null)
                }
            }
        }
    }

    private void createCustomers(Facility facility) {
        log.info(" * Creating Customers for ${facility.name}")
        USERS.each {
            def user = User.findByEmail(it.email)

            if (user) {
                def type = facility.membershipTypes.iterator().next()
                def membership = memberService.addMembership(addCustomer(user, facility),
                        type, null, null, null, null, user)
                if (membership.order.total()) {
                    cashService.createCashOrderPayment(membership.order)
                }
            }
        }
    }

    def addFacility(def name, def shortname, def description, def address, def city, Municipality municipality, def zipcode, def telephone,
                    def openingHour, def closingHour, def apiKey, def lat, def lng, def addSeason, def addAvailabilities = true, def boxnet = false, fortnoxDb = "", fortnoxToken = "", def country = "SE", def email = "bootstrap@matchi.se") {
        log.info("Adding facility ${name} with shortname ${shortname}")
        def facility = new Facility(
                name: name,
                shortname: shortname,
                description: description,
                address: address,
                city: city,
                municipality: municipality,
                zipcode: zipcode,
                telephone: telephone,
                lat: lat,
                lng: lng,
                vat: 6,
                bookable: true,
                active: true,
                boxnet: boxnet,
                bookingRuleNumDaysBookable: 30,
                apikey: facilityService.generateApiKey(apiKey),
                country: country,
                email: email)

        facility.courts = []

        facility.save(failOnError: true)

        if (addSeason) {
            Season season = new Season(name: "Vår 2012")
            season.startTime = new DateTime().toDateMidnight().toDate()
            season.endTime = new DateTime().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).plusDays(10).toDate()
            season.facility = facility
            season.save()
            facility.addToSeasons(season)
        }
        if (addAvailabilities) {
            for (int i = 1; i < 8; i++) {
                Availability av = new Availability()
                av.begin = new LocalTime().withHourOfDay(openingHour).withMinuteOfHour(0).withSecondOfMinute(0)
                av.end = new LocalTime().withHourOfDay(closingHour).withMinuteOfHour(0).withSecondOfMinute(0)
                av.active = true
                av.weekday = i
                facility.addToAvailabilities(av)
            }
        }

        MEMBERTYPES.each {
            log.info("Adding membertype ${it.name}")
            MembershipType type = new MembershipType(name: it.name, price: it.price)
            type.facility = facility
            type.save()
            facility.addToMembershipTypes(type)
        }

        PriceListCustomerCategory stdCategory = new PriceListCustomerCategory(name: "Standard", defaultCategory: true)
        CustomerPriceCondition customer = new CustomerPriceCondition(name: "Kund", description: "Desc")

        PriceListCustomerCategory membCategory = new PriceListCustomerCategory(name: "Medlem", defaultCategory: true)
        MemberPriceCondition member = new MemberPriceCondition(name: "Medlem", description: "Desc")

        membCategory.addToConditions(member)
        stdCategory.addToConditions(customer)
        facility.addToPriceListCustomerCategories(stdCategory)
        facility.addToPriceListCustomerCategories(membCategory)

        facility.save(failOnError: true)

        return facility
    }

    void addDefaultSlotsToFacility(def facility) {

        def season
        facility.seasons.each {
            season = it
        }

        CreateSeason cmd = new CreateSeason()
        cmd.name = season.name
        cmd.startTime = new DateTime(season.startTime)
        cmd.endTime = new DateTime(season.endTime)

        facility.courts.each { Court court ->
            if (!court.externalScheduling) {
                cmd.courts << createCourtCommand(court, 10, 22, 0, 60)
            }
        }

        def slots = slotService.generateSlots(cmd)

        log.info("Saving ${slots.size()} slots to ${facility.name}")

        slotService.createSlots(slots)
    }

    private CreateCourtSeason createCourtCommand(def court, def openHour, def closingHour, def timeBetween, def bookingLength) {
        CreateCourtSeason courtCmd = new CreateCourtSeason()
        courtCmd.timeBetween = new Period().plusMinutes(timeBetween)
        courtCmd.bookingLength = new Period().plusMinutes(bookingLength)

        courtCmd.court = court

        for (int i = DateTimeConstants.MONDAY; i <= DateTimeConstants.DAYS_PER_WEEK; i++) {
            OpenHours openHours = new OpenHours()
            openHours.opening = new LocalTime(openHour, 0)
            openHours.closing = new LocalTime(closingHour, 0)

            courtCmd.addOpenHours(i, openHours)
        }
        return courtCmd
    }

    def addCustomer(def user, def facility) {
        def customer = ((user && facility) ? Customer.findByUserAndFacility(user, facility) : null) ?: new Customer(
                number: facility.getNextCustomerNumber(),
                country: facility.country,
                firstname: user.firstname,
                lastname: user.lastname,
                email: user.email,
                telephone: user.telephone,
                dateOfBirth: user.birthday,
                securityNumber: "xxxx",
                municipality: user.municipality?.name,
                type: user.gender.toString().toUpperCase())

        facility.addToCustomers(customer)
        facility.save(failOnError: true)
        customer.save(failOnError: true)

        log.info("Created customer ${customer.number} on ${facility.name}")

        return customer
    }

    def addAccount(def firstname, def lastname, def email, def password, def telephone, def birthday, def municipality, def description) {
        def user = User.findByEmail(email) ?: new User(
                firstname: firstname,
                lastname: lastname,
                email: email,
                telephone: telephone,
                description: description ?: "Lorem ipsum",
                password: springSecurityService.encodePassword(password),
                birthday: new SimpleDateFormat("yyyy-MM-dd").parse(birthday),
                municipality: Municipality.findByName(municipality),
                enabled: true,
                gender: User.Gender.male).save(failOnError: true)

        return user
    }

    def addRoleToAccount(def user, def role) {
        if (!user.authorities.contains(role)) {
            UserRole.create user, role
        }
    }
}
