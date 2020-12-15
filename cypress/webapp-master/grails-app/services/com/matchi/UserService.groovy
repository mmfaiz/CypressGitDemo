package com.matchi

import com.matchi.admin.FilterUserCommand
import com.matchi.api.AppleIDAuthenticationCommand
import com.matchi.async.ScheduledTask
import com.matchi.sportprofile.SportAttribute
import com.matchi.sportprofile.SportProfile
import com.matchi.sportprofile.SportProfileAttribute
import com.matchi.sportprofile.SportProfileMindset
import grails.transaction.Transactional
import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang3.StringUtils
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.joda.time.LocalTime
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.social.facebook.api.User as FacebookProfile
import org.springframework.validation.Errors
import org.springframework.web.multipart.MultipartFile

import java.security.MessageDigest
import java.text.SimpleDateFormat

class UserService {

    static transactional = false
    def springSecurityService
    def notificationService
    def fileArchiveService
    def ticketService
    def userDetailsService
    def grailsApplication
    def scheduledTaskService
    def messageSource

    User getCurrentUser() { (User)springSecurityService.currentUser }

    def isLoggedIn() {
        return springSecurityService.loggedIn
    }

    @Transactional
    User getLoggedInUser() {
        if (isLoggedIn()) {
            def user = User.get(springSecurityService.currentUser.id)
            user.facility
            user.profileImage
            return user
        }
        return null
    }

    def getUserFacility() {
        def user = getLoggedInUser()
        return (user != null && user.facility != null) ? user.facility : null
    }

    def getUser(def id) {
        return User.get(id)
    }

    def getLoggedInOrSystemUser() {
        return getLoggedInUser() ?: User.findByEmail(grailsApplication.config.matchi.system.user.email)
    }

    User registerUser(def user, def params) {
        return registerUser(user, params, true)
    }

    @Transactional
    User registerUser(User user, def params, def needActivation) {
        log.info("Registering user ${user.email}")
        user.enabled = false
        user.dateCreated = new Date()

        user.validate()
        if (user.hasErrors()) {
            String errorList = ""
            user.errors.allErrors.each { err ->
                errorList += "[$err.field]: $err"
            }
            log.debug("Could not register user, error in field(s): ${errorList}")
            return user
        }
        if (!user.save(failOnError: true, flush: true)) {
            log.error("Can't save user during register operation")
            return user
        }

        addUserToRole(user, 'ROLE_USER')

        if (user.password) {
            user.password = getEncodedPassword(user.password)
        }

        if (needActivation) {
            user.activationcode = generateActivationCode(user.email)
            user.save(failOnError: true)

            def showTerms = !getUserFacility()

            notificationService.sendActivationMail(user, showTerms, params)
        } else {
            enableUser(user)
        }
        return user
    }

    def registerUserWithFacebook(FacebookProfile profile) {
        return registerUserWithFacebook(profile, null)
    }

    @Transactional
    def registerUserWithFacebook(FacebookProfile profile, MultipartFile profileImage) {
        User user = new User()

        log.info "Register user from facebook (${profile.email})"

        user.enabled = true
        user.accountExpired = false
        user.accountLocked = false
        user.passwordExpired = false
        user.activationcode = generateActivationCode(profile.email)
        user.email = profile.email
        user.firstname = profile.firstName
        user.lastname = profile.lastName
        user.facebookUID = profile.id
        user.receiveBookingNotifications = true
        user.city = profile.location?.name ?: null
        user.password = generatePassword() // user must have password (spring security)

        if (profile.gender) {
            user.gender = User.Gender.valueOf(profile.gender)
        }

        if (profile.birthday) {
            user.birthday = new SimpleDateFormat("MM/dd/yyyy").parse(profile.birthday)
        }

        if (profileImage) {
            processProfileImage(profileImage, user)
        }

        def locale = GrailsWebRequest.lookup()?.getLocale()
        if (locale) {
            user.language = StringUtils.substring(locale.language, 0, 2)
        }

        user = registerUser(user, null, false) // no activation from facebook

        return user
    }

    @Transactional
    def registerUserWithAppleID(AppleIDToken appleID, AppleIDAuthenticationCommand cmd) {
        User user = new User()

        log.info "Register user from apple (${appleID.email})"

        user.enabled = true
        user.accountExpired = false
        user.accountLocked = false
        user.passwordExpired = false
        user.activationcode = generateActivationCode(appleID.email)
        user.receiveBookingNotifications = true
        user.city = null
        user.password = generatePassword() // user must have password (spring security)

        user = updateUserWithApple(user, appleID, cmd)

        def locale = GrailsWebRequest.lookup()?.getLocale()
        if (locale) {
            user.language = StringUtils.substring(locale.language, 0, 2)
        }

        user = registerUser(user, null, false) // no activation from Apple

        return user
    }

    // Only used for connecting existing user account to Facebook
    @Transactional
    def addFacebookToUser(User user, FacebookProfile profile, MultipartFile profileImage) {
        user = updateUserWithFacebook(user, profile, profileImage)
        user.lastUpdated = new Date()
        user.save(failOnError: true)

        return user
    }

    // Only used for connecting existing user account to Facebook
    @Transactional
    def addAppleIDToUser(User user, AppleIDToken appleID, AppleIDAuthenticationCommand cmd) {
        user = updateUserWithApple(user, appleID, cmd)
        user.lastUpdated = new Date()
        user.save(failOnError: true)

        return user
    }

    @Transactional
    def updateUserWithFacebook(User user, FacebookProfile profile, MultipartFile profileImage) {
        user.email = user.email != null ? user.email : profile.email
        user.firstname = user.firstname ?: profile.firstName
        user.lastname = user.lastname ?: profile.lastName
        user.facebookUID = user.facebookUID != null ? user.facebookUID : profile.id
        user.city = user.city ?: (profile.location?.name ?: null)

        if (user.gender == null && profile.gender) {
            user.gender = User.Gender.valueOf(profile.gender)
        }
        if (user.birthday == null && profile.birthday) {
            user.birthday = new SimpleDateFormat("MM/dd/yyyy").parse(profile.birthday)
        }
        if (user.profileImage == null && profileImage) {
            processProfileImage(profileImage, user)
        }

        log.info("Added FacebookUID to: " + user.fullName())

        return user

    }

    @Transactional
    def updateUserWithApple(User user, AppleIDToken appleID, AppleIDAuthenticationCommand cmd) {
        user.email = user.email != null ? user.email : appleID.email
        user.appleUID = user.appleUID != null ? user.appleUID : appleID.sub
        user.firstname = user.firstname ?: cmd.firstName
        user.lastname = user.lastname ?: cmd.lastName

        log.info("Added AppleUID to: " + user.fullName())

        return user

    }



    @Transactional
    def forgetFacebookConnect(User user) {
        user.facebookUID = null
        user.save(failOnError: true)
    }

    def enableUser(User user, def params, String passwordInPlain) {
        changePassword(passwordInPlain, user)
        enableUser(user, params)
    }

    def enableUser(User user, def params = null) {
        if (user) {
            log.info("Enabling user ${user.email} with password ${user.password}")
            user.enabled = true
            user.activationcode = null
            user.dateActivated = new Date()

            if (user.save(failOnError: true, flush: true)) {
                Facility facility = params?.f ? Facility.findById(Long.parseLong(params.f)) : null

                if (facility) {
                    notificationService.sendRegistrationConfirmationMail(user, facility)
                } else {
                    notificationService.sendRegistrationConfirmationMail(user)
                }

                scheduledTaskService.scheduleTask(messageSource.getMessage('scheduledTask.matchUserToCustomer.taskName', null, new Locale(user.language)), user.id, null) { taskId ->
                    user.addToMatchingCustomers()
                }

            } else {
                log.error("Unable to enable user: " + user.errors)
                throw new IllegalArgumentException("Unable to enable user: " + user.errors)
            }
        } else {
            log.error("Unable to enable null user")
            throw new IllegalArgumentException("Unable to enable null user")
        }
    }

    def addUserToRole(def user, def newRole) {
        def role = Role.findByAuthority(newRole)

        UserRole.create user, role
        log.info("Added " + user.firstname + " to role " + role)
    }

    def updateUserRoles(User user, def roles) {

        UserRole.removeAll(user)

        roles.each { role ->
            UserRole.create(user, role)
        }
    }

    def logIn(user) {
        log.info("Logging in user: " + user.email)

        UserDetails details = userDetailsService.loadUserByUsername(user.email)

        if (details.isAccountNonExpired() && details.isAccountNonLocked() && details.isEnabled()) {
            springSecurityService.reauthenticate(user.email)
            return true
        } else {
            return false
        }
    }

    def parseName(def name) {
        def names

        if (name) {
            if (name.contains(' ')) {
                names = name.split(' ')
            } else {
                return [firstName: name, lastName: '']
            }
        } else {
            return [firstName: null, lastName: null]
        }

        return [firstName: names[0], lastName: names[1]]
    }

    def findUser(def query) {
        return findUser(query, null)
    }

    def findUser(def query, def facility) {

        if (!facility) {
            return User.executeQuery("""
                SELECT distinct u from User as u
                WHERE
                (email like :query)
                or (firstname like :query)
                or (lastname like :query)
                or (telephone like :query)
                or (concat(u.firstname,' ',u.lastname) like :query)""",
                    [query: "%${query}%"], [fetchSize: 20]);
        } else {
            return User.executeQuery("""
                SELECT distinct u from User as u
                    LEFT JOIN Customer as c
                WHERE
                (email like :query)
                or (firstname like :query)
                or (lastname like :query)
                or (telephone like :query)
                or (concat(u.firstname,' ',u.lastname) like :query)
                or (c in
                    (select distinct fc from Customer fc where facility = :facility and fc.user = u and number like :query)
                )""", [query: "%${query}%", facility: facility], [fetchSize: 20]);
        }
    }

    def findUsersNotMembers(def facility, def query) {
        def notMembers = []

        def users = User.withCriteria {
            if (query) {
                or {
                    //like("membernumber", "%${filter}%")
                    like("email", "%${query}%")
                    like("firstname", "%${query}%")
                    like("lastname", "%${query}%")
                    like("telephone", "%${query}%")
                    sqlRestriction("concat(firstname,' ',lastname) like ?", ["%${query}%" as String])
                }
            }
        }

        users.each {
            if (!it.hasMembershipIn(facility)) {
                notMembers << it
            }
        }

        return notMembers
    }

    def findUserNotCustomerByEmail(def query, def facility) {
        def customers = Customer.createCriteria().listDistinct {
            eq("facility", facility)
            like("email", "%${query}%")
        }

        def customerIds = customers.collect { Customer c -> c.userId }

        def users = User.createCriteria().listDistinct {

            if (customerIds.size() > 0) {
                not {
                    inList("id", customerIds)
                }
            }
            like("email", "%${query}%")
        }

        return users
    }

    def searchUserByNames(def query) {

        def users = User.withCriteria {
            if (query) {
                eq("searchable", true)
                and {
                    or {
                        like("firstname", "%${query}%")
                        like("lastname", "%${query}%")
                    }
                }
            }
            maxResults 20
        }

        return users
    }


    @Transactional
    def updateAvailability(User user, def params) {
        Availability availability = new Availability()
        def addToUser = false

        (1..7).each { int day ->
            availability = user.availabilities.find { it.weekday == day }

            if (!availability) {
                addToUser = true
                availability = new Availability()
                availability.weekday = day
            }

            availability.begin = new LocalTime(params["fromHour_${day}"])
            availability.end = new LocalTime(params["toHour_${day}"])
            availability.active = params["active_${day}"]

            if (addToUser) {
                user.addToAvailabilities(availability)
            }

            availability.save()
        }
    }

    def generateActivationCode(String email) {

        String encodeMe = UUID.randomUUID().toString() + email

        MessageDigest digest = MessageDigest.getInstance("MD5")
        digest.update(encodeMe.bytes)

        BigInteger big = new BigInteger(1, digest.digest())
        String md5 = big.toString(16).padLeft(32, "0")
        return md5
    }

    @Transactional
    def addSportProfile(User user, Sport sport, def params) {
        def level = Integer.parseInt(params.level)

        SportProfile sp = SportProfile.link(user, sport, level, params.list("mindset"))

        sport.sportAttributes.each { SportAttribute sa ->
            SportProfileAttribute spa = new SportProfileAttribute()
            spa.sportAttribute = sa
            spa.skillLevel = Integer.parseInt(params["level_${sa.id}"])

            sp.addToSportProfileAttributes(spa)
        }
        sp.frequency = params.frequency

        sp.save(failOnError: true)
        return sp
    }

    @Transactional
    def updateSportProfile(SportProfile sp, def params) {
        sp.mindSets.clear()

        params.list("mindset").each {
            sp.addToMindSets(SportProfileMindset.findByName(it))
        }
        sp.skillLevel = Integer.parseInt(params.level)

        sp.sportProfileAttributes.each {
            it.skillLevel = Integer.parseInt(params["level_${it.sportAttribute.id}"])
        }
        sp.frequency = params.frequency

        sp.save(failOnError: true)
        return sp
    }

    def getUserNonActiveSportProfilesSports(User user) {
        def nonActiveSportIds = (Sport.list().collect { it.id } - user.sportProfiles.collect { it.sport.id })
        if (nonActiveSportIds.size() < 1) {
            return []
        }

        def sports = Sport.withCriteria {
            inList("id", nonActiveSportIds)
            order("id")
        }

        return sports
    }

    void changePasswordWithTicket(String newPassword, String ticketKey) {
        ResetPasswordTicket ticket = ResetPasswordTicket.findByKey(ticketKey)
        if (!ticket) {
            throw new IllegalArgumentException("Unable to find ticket with key ${ticketKey}")
        }

        changePassword(newPassword, ticket.user)

        if (!ticketService.useResetPasswordTicket(ticket.key)) {
            throw new IllegalStateException("Ticket has expired")
        }
    }

    void changeEmailWithTicket(def email, def ticketKey) {
        ChangeEmailTicket ticket = ChangeEmailTicket.findByKey(ticketKey)
        if (!ticket) {
            throw new IllegalArgumentException("Unable to find ticket with key ${ticketKey}")
        }
        changeEmail(email, ticket.user)

        if (!ticketService.useChangeEmailTicket(ticket.key)) {
            throw new IllegalStateException("Ticket has expired")
        }
    }

    @Transactional
    void changePassword(String passwordInPlain, User user) {
        user.password = getEncodedPassword(passwordInPlain)
        user.save(flush: true)
        log.info("New password set on user ${user.email}")
    }

    @Transactional
    void changeEmail(def email, def user) {
        user.email = email
        user.save()
        log.info("New email set on user ${user.id}")
    }

    boolean checkPassword(User user, String password) {
        return user.password.equals(getEncodedPassword(password))
    }

    boolean checkPassword(String email, String password) {
        User user = User.findByEmail(email)
        if (user) {
            return checkPassword(user, password)
        } else {
            return false
        }
    }

    boolean isUserExist(String userEmail) {
        User.findByEmail(userEmail) != null
    }

    String getEncodedPassword(String password) {
        return springSecurityService.encodePassword(password)
    }

    /**
     * Random 8 chars string where letters are enabled while numbers are not.
     * @return
     */
    protected def generatePassword = {
        return RandomStringUtils.random(8, true, false)
    }

    def processWelcomeImage(MultipartFile userWelcomeImageFile, User user) {
        if (!userWelcomeImageFile.isEmpty()) {
            log.info("Saving users welcome image to file archive")
            def welcomeImage = fileArchiveService.storeFile(userWelcomeImageFile)
            user.welcomeImage = welcomeImage
        } else {
            user.welcomeImage = null
        }
    }

    def processProfileImage(MultipartFile userImageFile, User user) {
        if (!userImageFile.isEmpty()) {
            log.info("Saving users facebook image to file archive")
            def profileImage = fileArchiveService.storeFile(userImageFile)
            user.profileImage = profileImage
        } else {
            user.profileImage = null
        }
    }

    @Transactional
    void updateLastLoggedInDate(User user = null) {
        def userId = user?.id ?: springSecurityService.currentUserId
        if (userId) {
            User.executeUpdate("update User set lastLoggedIn = :date where id = :id",
                    [date: new Date(), id: userId])
        }
    }

    @Transactional
    def getUserFacilities() {
        def user = getLoggedInUser()
        user ? user.facilityUsers*.facility.sort { it.name } : null
    }

    List<Facility> getUserFacilities(User user) {
        user ? user.facilityUsers*.facility.sort { it.name } : new ArrayList<>()
    }

    List findUsers(FilterUserCommand filter) {
        if (!(filter.roles || filter.q)) {
            User.list(max: filter.max, offset: filter.offset)
        }
        else {
            def ids = User.withCriteria {
                projections {
                    distinct("id")
                }
                if (filter.q) {
                    or {
                        def q = StringUtils.replace(filter.q, "_", "\\_")

                        like("email", "%${q}%")
                        like("firstname", "%${q}%")
                        like("lastname", "%${q}%")
                        like("telephone", "%${q}%")
                        sqlRestriction("concat(firstname,' ',lastname) like ?", ["%${q}%" as String])
                    }
                }
                if (filter.roles) {
                    createAlias("roles", "rs")
                    inList("rs.role.id", filter.roles)
                }
            }


            User.createCriteria().list(max: filter.max, offset: filter.offset) {
                inList("id", ids ?: [-1L])
            }
        }
    }

    @Transactional
    void removeFacilityUser(Long userId) {
        def facility = getUserFacility()
        def user = User.get(userId)

        if (!user.facility || user.facility.id == facility.id) {
            user.facility = FacilityUser.findByUserAndFacilityNotEqual(user, facility)?.facility
        }

        def facilityUser = user.facilityUsers.find { it.facility.id == facility.id }
        if (facilityUser) {
            user.removeFromFacilityUsers(facilityUser)
            facilityUser.delete()
        }

        user.save(flush: true)
    }

    void deleteUser(User user, boolean flush = false) {
        User.withTransaction {
            log.info "Deleting user ${user.id} with flush = ${flush}"
            if (user.isHardDeletable()) {
                log.info "Hard delete of user ${user.id}"

                user.deleteOrders()
                user.removeFromOrders()
                user.disconnectFromFacilities()
                user.deleteRelations(true)
                user.delete(flush: flush)
            } else {
                log.info "Soft delete of user ${user.id}"

                user.disconnectFromFacilities()
                user.deleteRelations()
                user.clearProperties()
                user.scramble()
                user.save(flush: flush)
            }
        }
    }

    boolean canSendDirectMessage(User currentUser, User toUser) {
        if (!currentUser || !toUser) {
            return false
        }

        // true if superadmin or has connected customer or already has a conversation
        currentUser.isInRole("ROLE_ADMIN") ||
                Customer.countByUser(currentUser) ||
                UserMessage.countByFromAndTo(toUser, currentUser)
    }

    @Transactional
    UserFavorite addFavorite(User user, Facility facility) {
        log.info("Add favorite facility $facility.id to user $user.id")
        UserFavorite.findByUserAndFacility(user, facility) ?:
                new UserFavorite(user: user, facility: facility).save()
    }

    @Transactional
    boolean removeFavorite(User user, Facility facility) {
        def favorite = UserFavorite.findByUserAndFacility(user, facility)
        if (!favorite) {
            return false
        }
        log.info("Remove favorite facility $facility.id from user $user.id")
        favorite.delete()
        return true
    }

    String userErrorsToString(Errors errors) {
        StringBuffer buffer = new StringBuffer()
        String errCode = "unspecified"
        errors?.fieldErrors.each { err ->
            if (err.code) {
                errCode = err.code
            } else if (err.codes?.length > 0) {
                errCode = err.codes[err.codes.length - 1]
            }
            buffer.append "[$err.field] $errCode"
        }
        return buffer.toString()
    }
}