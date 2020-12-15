import com.matchi.SecurityException
import com.matchi.api.APIException

class UrlMappings {

    static mappings = {

        /***********************************************************
         * K8s HEALTH
         ***********************************************************/
        "/readiness"(controller: "health", action: "readiness")
        "/liveness"(controller: "health", action: "liveness")
        "/preStop"(controller: "health", action: "preStop")

        /***********************************************************
         * GENERAL
         ***********************************************************/

        "/"(controller: "home", view: "index")
        "/useragreement"(controller: "home", view: "useragreement")
        //"/about"(controller: "home", view:"about")
        "/about"(controller: "contentFromUrl", action: "index") {
            baseurl = "http://d4y4exzbfard3.cloudfront.net/"; path = "about"
        }
        "/faq"(controller: "home", view: "faq")
        "/levels"(controller: "home", view: "levelDescriptions")
        "/getmatchi"(controller: "home", action: "getmatchi")
        "/customers"(controller: "home", action: "customers")
        "500"(action: "index", controller: "APIErrorHandler", exception: APIException)
        "500"(view: '/error')
        "404"(view: '/APIErrorHandler/404')
        "500"(view: "/login/denied", exception: SecurityException)

        /***********************************************************
         * USER REGISTRATION AND PROFILE - Starts with /registration /profile
         ***********************************************************/

        "/registration/user/$c?"(controller: "userRegistration", action: "index")
        "/registration/enable/$ac?"(controller: "userRegistration", action: "enable")
        "/registration/$action?/$id?"(controller: "userRegistration")
        "/profile/$action?/$id?"(controller: "userProfile")
        "/profile/password/$id?"(controller: "userProfile", action: "passwordForFb")
        "/profile/messages/$action?/$id?"(controller: "userMessage")
        "/login/password/$action?/$id?"(controller: "resetPassword")
        "/invite/$ticket"(controller: "info", action: "invite")
        "/updateRequest/$ticket"(controller: "updateCustomerRequest", action: "index")
        "/updateRequest/update"(controller: "updateCustomerRequest", action: "update")
        "/changeEmail/change/"(controller: "changeEmail", action: "change")

        "/info/$action/$id?"(controller: "info")
        "/logout"(controller: "logout")

        /***********************************************************
         * ADMINISTRATION (MATCHI) - Starts with /admin
         ***********************************************************/

        "/admin/users/$action?/$id?"(controller: "adminUser")
        "/admin/matchiConfig/$action?/$id?"(controller: "adminMatchiConfig")
        "/admin/orders/$action?/$id?"(controller: "adminOrder")
        "/admin/facility/$action?/$id?"(controller: "adminFacility")
        "/admin/facility/properties/$action?/$id?"(controller: "adminFacilityProperties")
        "/admin/facility/contracts/$action?/$id?"(controller: "adminFacilityContracts")
        "/admin/facility/organizations/$action?/$id?"(controller: "adminFacilityOrganizations")
        "/admin/facility/courts/$action?/$id?"(controller: "adminFacilityCourts")
        "/admin/facility/billing/$action?/$id?"(controller: "adminFacilityBilling")
        "/admin/facility/sports/$id?"(controller: "adminFacility", action: "sports")
        "/admin/region/$action?/$id?"(controller: "adminRegion")
        "/admin/statistics/$action?/$id?"(controller: "adminStatistics", view: "index")
        "/admin/signups/$action?/$id?"(controller: "adminContactMe", view: "index")
        "/admin/system/"(controller: "adminSystem", action: "index")
        "/admin/system/mpc"(controller: "adminSystem", action: "mpc")
        "/admin/job/$action?"(controller: "adminJob")
        "/admin/mail/$action?"(controller: "adminMail")
        "/admin/encoding/$action?"(controller: "adminEncoding")
        "/admin/formTemplates/$action?/$id?"(controller: "adminFormTemplate")
        "/admin/notifications/$action?/$id?"(controller: "adminGlobalNotification")
        "/admin/frontEndMessage/$action?/$id?"(controller: "adminFrontEndMessage")
        "/admin/$action?" {
            controller = "adminHome"
            constraints {
                action(matches: /index|switchFacility|templates/)
            }
        }

        /***********************************************************
         * FACILITY ADMINISTRATION - Starts with /facility
         ***********************************************************/

        group("/facility") {
            "/administration/$action?/$id?"(controller: "facilityAdministration")
            "/groups/$action?/$id?"(controller: "facilityGroup")
            "/seasons/$action?/$id?"(controller: "facilitySeason")
            "/seasons/deviations/$action?/$id?"(controller: "facilitySeasonDeviation")
            "/subscriptions/$action?/$id?"(controller: "facilitySubscription")
            "/subscriptions/copy/$action?/$id?"(controller: "facilitySubscriptionCopy")
            "/subscriptions/delete/$action?/$id?"(controller: "facilitySubscriptionDelete")
            "/subscriptions/invoice/$action?/$id?"(controller: "facilitySubscriptionInvoice")
            "/subscriptions/import/$action?/$id?"(controller: "facilitySubscriptionImport")
            "/subscriptions/message/$action?/$id?"(controller: "facilitySubscriptionMessage")
            "/subscriptions/redeem/$action?/$id?"(controller: "facilitySlotRedeem")
            "/booking/$action?/$id?"(controller: "facilityBooking")
            "/campaign/$action?/$id?"(controller: "facilityCampaign")
            "/statistic/$action?/$id?"(controller: "facilityStatistic")
            "/courts/$action?/$id?"(controller: "facilityCourts")
            "/courtsgroups/$action?/$id?"(controller: "facilityCourtsGroups")
            "/requirements/$action?/$id?"(controller: "facilityRequirements")
            "/activities/$action?/$id?"(controller: "facilityActivity")
            "/activities/occasions/$action?/$id?"(controller: "facilityActivityOccasion")
            "/pricelist/$action?/$id?"(controller: "facilityPriceList")
            "/pricelist/flow/$action?/$id?"(controller: "facilityPriceListFlow")
            "/customercategories/$action?/$id?"(controller: "facilityCustomerCategory")
            "/pricelist/conditions/$action?/$id?"(controller: "facilityPriceListCondition")
            "/offers/$action?/$id?"(controller: "facilityOfferFlow")
            name Coupon: "/coupons/$action?/$id?"(controller: "facilityOffer") {
                type = 'Coupon'
            }
            name GiftCard: "/giftCards/$action?/$id?"(controller: "facilityOffer") {
                type = 'GiftCard'
            }
            name PromoCode: "/promoCodes/$action?/$id?"(controller: "facilityPromoCode") {
                type = 'PromoCode'
            }
            name CouponConditions: "/coupons/conditions/$action/$id?"(controller: "facilityCouponCondition") {
                type = 'Coupon'
            }
            name CouponPrices: "/coupons/prices/$action/$id?"(controller: "facilityCouponPrice") {
                type = 'Coupon'
            }
            name GiftCardConditions: "/giftCards/conditions/$action/$id?"(controller: "facilityCouponCondition") {
                type = 'GiftCard'
            }
            name GiftCardPrices: "/giftCards/prices/$action/$id?"(controller: "facilityCouponPrice") {
                type = 'GiftCard'
            }
            name PromoCodeConditions: "/promoCodes/conditions/$action/$id?"(controller: "facilityCouponCondition") {
                type = 'PromoCode'
            }

            "/customers/$action?/$id?"(controller: "facilityCustomer")
            "/customers/members/$action?/$id?"(controller: "facilityCustomerMembers")
            "/customers/members/flow/$action?/$id?"(controller: "facilityCustomerMembersFlow")
            "/customers/members/invoice/$action?/$id?"(controller: "facilityCustomerMembersInvoice")
            "/customers/family/$action?/$id?"(controller: "facilityCustomerFamily")
            "/customers/archive/$action?/$id?"(controller: "facilityCustomerArchive")
            "/customers/import/$action?/$id?"(controller: "facilityCustomerImport")
            "/customers/invite/$action?/$id?"(controller: "facilityCustomerInvite")
            "/customers/merge/$action?/$id?"(controller: "facilityCustomerMerge")
            "/customers/remove/$action?/$id?"(controller: "facilityCustomerRemove")
            "/customers/message/$action/$id?"(controller: "facilityCustomerMessage")
            "/customers/sms/$action/$id?"(controller: "facilityCustomerSMSMessage")
            "/customers/requestUpdate/$action/$id?"(controller: "facilityCustomerUpdateRequest")
            "/customers/group/$action/$id?"(controller: "facilityCustomerGroup")
            "/invoice/$action/$id?"(controller: "facilityInvoice")
            "/invoice/flow/$action/$id?"(controller: "facilityInvoiceFlow")
            "/invoice/payments/$action/$id?"(controller: "facilityInvoicePayment")
            "/invoicerows/$action/$id?"(controller: "facilityInvoiceRow")
            "/invoicerows/flow/$action/$id?"(controller: "facilityInvoiceRowFlow")
            "/membertypes/$action/$id?"(controller: "facilityMembershipType")
            "/messages/$action/$id?"(controller: "facilityMessage")
            "/accesscodes/$action/$id?"(controller: "facilityAccessCode")
            "/accesscodes/import/$action?/$id?"(controller: "facilityAccessCodeImport")
            "/currentlyRunningTasks"(controller: 'scheduledTask', action: 'getCurrentlyRunningTasks')
            "/controlSystems/$action?/$id?"(controller: 'facilityControlSystems')
            "/downloadTaskResult/$id"(controller: 'scheduledTask', action: 'download')
            "/markTaskReportAsRead/$id"(controller: 'scheduledTask', action: 'markAsRead')
            "/forms/$action?/$id?"(controller: "facilityForm")
            "/trainer/$action/$id?"(controller: "trainer")
            "/courses/$action/$id?"(controller: "facilityCourse")
            "/courses/flow/$action/$id?"(controller: "facilityCourseFlow")
            "/course/participants/$action/$id?"(controller: "facilityCourseParticipant")
            "/course/participants/flow/$action/$id?"(controller: "facilityCourseParticipantFlow")
            "/course/submissions/$action/$id?"(controller: "facilityCourseSubmission")
            "/course/submissions/flow/$action/$id?"(controller: "facilityCourseSubmissionFlow")
            "/users/$action/$id?"(controller: "facilityUser")
            "/organizations/$action?/$id?"(controller: "organization")
            "/events/$action/$id?"(controller: "facilityEventActivity")
            "/notifications/$action?/$id?"(controller: "facilityNotification")
            "/restrictions/$action?/$id?"(controller: "facilityBookingRestrictions")
        }

        /***********************************************************
         * USER - Starts with /
         ***********************************************************/
        "/facilities/index?"(action: "index", controller: "facility")
        "/facilities/$name?"(action: "show", controller: "facility")
        "/facilities/$name/$wl?"(action: "show", controller: "facility")
        "/facilities/leagues/$name?"(action: "leagues", controller: "facility")
        "/facilities/leagues/$name/$wl?"(action: "leagues", controller: "facility")
        "/facilities/$action?"(controller: "facility")
        "/facilities/membership/payment/$action?/$id?"(controller: "membershipPayment")
        "/facilities/membership/request?"(action: "request", controller: "membershipRequest")
        "/facilities/membership/$name?"(action: "index", controller: "membershipRequest")
        "/boxnet/$id?"(action: "boxnet", controller: "bookingPayment")
        "/book/$action?/$id?"(controller: "book")
        "/activities/$action?/$id?"(controller: "activity")
        "/user/booking/$action?/$id?"(controller: "userBooking")
        "/user/favorites/$action?/$id?"(controller: "userFavorites")

        "/adyen/$action?"(controller: "adyenPayment")

        "/forms/submit"(controller: "form", action: "submit")
        "/forms/submitAndPay"(controller: "form", action: "submitAndPay")
        "/forms/$hash"(controller: "form", action: "show")
        "/forms/memberform/$hash"(controller: "form", action: "showMemberForm")
        "/forms/protectedform/$hash"(controller: "form", action: "showProtectedForm")
        "/forms/payment/$action?/$id?"(controller: "formPayment")

        "/payment/update/$action?/$id?"(controller: "paymentInfoUpdate")

        "/activitywatch/confirm"(controller: "classActivityWatch", action: "confirm")

        /***********************************************************
         * API - Starts with /api
         ***********************************************************/
        group "/api", {
            /***********************************************************
             * General API - Version 1
             ***********************************************************/

            // -- Health --
            "/v1/health/?"(controller: "health") {
                format = "json"
                action = [GET: "status"]
            }

            // -- Authentication session --
            "/v1/auth"(controller: "authentication") {
                action = [POST: "authSession"]
            }
            "/v1/auth/fb"(controller: "authentication") {
                action = [POST: "authFacebookSession"]
            }
            "/v1/auth/new"(controller: "userResource", action: "register")

            // -- Get orders and register payments --
            "/v1/orders"(controller: "orderResource", namespace: "v1", excludes: ['delete', 'create', 'edit', 'save']) {
                action = [GET: "index", POST: "addOrderPayment"]
                format = "json"
            }

            // -- Stale --
            "/bookings/$applicationId?"(action: "bookings", controller: "apiBookings")
            // -- end stale --
            // -- Used for Boxnet --
            "/payment/$orderId?"(action: "payment", controller: "apiPayment")
            // -- Used for MLCS --
            "/mlcs/schedule/$from/$to?"(action: "schedule", controller: "MLCS")
            // -- Used for IOSync --
            "/iosync?"(controller: "IOSync") {
                action = [POST: "update"]
            }

            /***********************************************************
             * BackhandSmash API - Version 1
             ***********************************************************/
            "/bs/v1"(controller: "backhandSmash") {
                action = [GET: "facility", DELETE: "cancelBooking"]
            }

            /***********************************************************
             * Slotwatch API - Version 1
             ***********************************************************/
            name slotWatch: "/slotwatch/v1/watch"(controller: "slotWatch", parseRequest: true) {
                action = [GET: "list", DELETE: "remove", POST: "add"]
            }

            name activityWatch: "/activitywatch/v1/watch"(controller: "classActivityWatch", parseRequest: true) {
                action = [GET: "list", DELETE: "remove", POST: "add"]
            }

            /***********************************************************
             * Mobile API - Version 1
             ***********************************************************/

            "/mobile/v1/auth"(controller: "authentication") {
                action = [POST: "auth"]
            }
            "/mobile/v1/auth/facebook"(controller: "authentication", action: "authFacebook")

            // User resource
            "/mobile/v1/auth/new"(controller: "userResource", action: "register")
            "/mobile/v1/secure/users/current"(controller: "userResource", action: "current")
            "/mobile/v1/secure/users/update"(controller: "userResource", action: "update")

            // Bookings resource
            "/mobile/v1/secure/bookings/${id}?"(controller: "bookingResource") {
                action = [GET: "list", POST: "create", DELETE: "cancel"]
            }

            "/mobile/v1/activities"(controller: "classActivitySearchResource", action: "index")

            "/mobile/v1/**"(controller: "APIErrorHandler", action: "notFound")

            "/v1/articles"(controller: "articleResource") {
                action = [GET: "listArticles", POST: "updateArticles"]
            }

            "/v1/customers/$id"(action: "show", controller: "customerResource", method: "GET")
            "/v1/invoices/$id"(action: "show", controller: "invoiceResource", method: "GET")
            "/v1/invoices"(action: "list", controller: "invoiceResource", method: "GET")
            "/v1/invoices"(action: "updateInvoices", controller: "invoiceResource", method: "POST")

            /***********************************************************
             * External API
             ***********************************************************/
            "/ext/v1/auth"(controller: "APIExtAuth", action: "auth")
            "/ext/v1/health"(controller: "APIExtHealth", action: "health")
            "/ext/v1/facilities"(controller: "APIExtFacility", action: "facilities")
            "/ext/v1/facility/$facilityId?"(controller: "APIExtFacility", action: "facility")
            "/ext/v1/facility/$facilityId?/$action?/$date?"(controller: "APIExtFacility")
            "/ext/v1/facility/$facilityId?/lights/$from?/$to?"(controller: "APIExtFacility", action: "lights")

            "/ext/v1/facility/$facilityId?/offers"(controller: "APIExtFacilityOffer", action: "offers")

            "/ext/v1/facility/$facilityId?/customers"(controller: "APIExtCustomer", action: "customers")
            "/ext/v1/facility/$facilityId?/customer/$customerId?"(controller: "APIExtCustomer", action: "customer")

            "/ext/v1/facility/$facilityId?/invoices"(controller: "APIExtAccounting", action: "invoices")
            "/ext/v1/facility/$facilityId?/invoice/$invoiceId?"(controller: "APIExtAccounting", method: "GET", action: "invoice")

            /*
             * v2 API
             */

            // Actually v2 endpoints
            //Please update existing mobile API collection when adding new endpoint
            //https://matchi.postman.co/collections/24638-8d766050-c71d-4128-b499-984dc89244e5?version=latest&workspace=7d1b2edc-8095-4a13-b5cf-de82f6c464a9

            // Facility resource
            "/mobile/v2/facilities"(action: "list", controller: "facilityResource")
            "/mobile/v2/facilities/${id}"(action: "show", controller: "facilityResource")
            "/mobile/v2/facilities/${id}/courts"(action: "courts", controller: "facilityResource")

            // User resource
            "/mobile/v2/auth/new"(controller: "userResource", action: "register", namespace: "v2")
            "/mobile/v2/secure/users/favorites"(controller: "userResource", namespace: "v2") {
                action = [POST: "addFavorite", DELETE: "removeFavorite"]
            }

            "/mobile/v2/secure/users"(controller: "userResource", namespace: "v2") {
                action = [GET: "current", PUT: "update"]
            }

            // Slot resource
            "/mobile/v2/secure/slots/${from}/${to}?"(controller: "slotResource", action: "list", namespace: "v2")
            "/mobile/v2/secure/slots/price?"(controller: "slotResource", action: "price", namespace: "v2")
            "/mobile/v2/secure/slots/pricemodel?"(controller: "slotResource", action: "priceModel", namespace: "v2")
            "/mobile/v2/secure/promocode?"(controller: "slotResource", action: "promoCode", namespace: "v2")

            // Location resource
            "/mobile/v2/locations"(controller: "locationResource", action: "list", namespace: "v2")


            // New pointers to api v1

            "/mobile/v2/auth"(controller: "authentication") {
                action = [POST: "auth"]
            }

            "/mobile/v2/auth/token"(controller: "authentication") {
                action = [POST: "authSessionWithToken"]
            }

            "/mobile/v2/auth/facebook"(controller: "authentication", action: "authFacebook")
            "/mobile/v2/auth/appleid"(controller: "authentication", action: "authAppleID")

            // Bookings resource
            "/mobile/v2/secure/bookings/${id}?"(controller: "bookingResource", namespace: "v2") {
                action = [GET: "list", POST: "create", DELETE: "cancel"]
            }

            // Bookings resource
            "/mobile/v2/secure/reservations/$type?/$id?"(controller: "reservationResource") {
                action = [GET: "list", POST: "create", DELETE: "cancel"]
            }

            "/mobile/v2/**"(controller: "APIErrorHandler", action: "notFound")


            // ### Activities ###
            // Will search for activity occasions and then return corresponding activities, can expose those found activities
            "/mobile/v2/secure/activities"(controller: "classActivitySearchResource", action: "activities")

            "/mobile/v2/secure/activities/occasions/$id/price"(controller: "classActivitySearchResource", action: "price")
            "/mobile/v2/secure/bookings/activities"(controller: "classActivitySearchResource", action: "createActivityBooking", method: "POST")

            "/v2/articles"(action: "updateArticles", controller: "articleResource", method: "POST")
            "/v2/customers/$id"(action: "show", controller: "customerResource", method: "GET")
            "/v2/invoices/$id"(action: "show", controller: "invoiceResource", method: "GET")
            "/v2/invoices"(action: "list", controller: "invoiceResource", method: "GET")
            "/v2/invoices"(action: "updateInvoices", controller: "invoiceResource", method: "POST")
        }

        "/activityPayment/$action?/$id?"(controller: "activityPayment")

        "/recordingPayment/$action?/$id?"(controller: "recordingPayment")

        "/remotePayment/$action?/$id?"(controller: "remotePayment")

        "/autoCompleteSupport/$action?/$id?"(controller: "autoCompleteSupport")

        "/couponPayment/$action?/$id?"(controller: "couponPayment")

        "/login/$action?"(controller: "login")

        "/loginSuccess/$action?"(controller: "loginSuccess")

        "/matching/$action?/$id?"(controller: "matching")
        "/coach/$action?/$id?"(controller: "userTrainer")

        "/message/$action?/$id?"(controller: "message")

        "/orderPayment/$action?/$id?"(controller: "orderPayment")

        "/bookingPayment/$action?/$id?"(controller: "bookingPayment")

        "/"(controller: "home", action: "index")
        "/home/$action?/$id?"(controller: "home")

        "/booking/cancel"(controller: "userBooking", action: "cancelByTicket")
        "/booking/show"(controller: "userBooking", action: "showByTicket")

        "/customer/disableClubMessages"(controller: "facilityCustomer", action: "disableClubMessagesByTicket")
        "/customer/show"(controller: "facilityCustomer", action: "showByTicket")

        "/play/player/$bookingId?"(controller: "play", action: "player")
    }
}
