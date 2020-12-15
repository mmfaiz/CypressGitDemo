import com.matchi.*
import com.matchi.admin.*
import com.matchi.facility.*
import com.matchi.facility.offers.FacilityOfferController
import com.matchi.orders.OrderPaymentController
import com.matchi.payment.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

/**
 * @author Sergei Shushkevich
 */
@TestFor(UrlMappings)
@Mock([ActivityPaymentController, AdminFacilityController, AdminFacilityCourtsController,
        AdminFacilityPropertiesController, AdminHomeController, AdminJobController, AdminOrderController,
        AdminRegionController, AdminSystemController, AdminUserController, AutoCompleteSupportController,
        CouponPaymentController, FacilityAccessCodeController, FacilityActivityController, FacilityActivityOccasionController,
        FacilityAdministrationController, FacilityBookingController, FacilityCampaignController, FacilityController,
        FacilityOfferController, FacilityCouponConditionController, FacilityCourtsController,
        FacilityCustomerArchiveController, FacilityCustomerCategoryController, FacilityCustomerController,
        FacilityCustomerFamilyController, FacilityCustomerImportController,
        FacilityCustomerInviteController, FacilityCustomerMembersController, FacilityCustomerMembersInvoiceController,
        FacilityCustomerMergeController, FacilityCustomerMessageController, FacilityCustomerUpdateRequestController,
        FacilityGroupController, FacilityInvoiceController, FacilityInvoicePaymentController,
        FacilityInvoiceRowController, FacilityMembershipTypeController, FacilityMessageController,
        FacilityPriceListController, FacilityPriceListConditionController, FacilitySeasonController,
        FacilitySeasonDeviationController, FacilityStatisticController, FacilitySubscriptionController,
        FacilitySubscriptionCopyController, FacilitySubscriptionImportController, FacilitySubscriptionInvoiceController,
        FacilitySubscriptionMessageController, HomeController, InfoController, LoginController,
        LoginSuccessController, LogoutController, MatchingController, MembershipRequestController, MessageController,
        OrderPaymentController, BookingPaymentController, ResetPasswordController,
        UpdateCustomerRequestController, UserBookingController, UserProfileController, UserRegistrationController,
        FacilitySubscriptionDeleteController, AdminFacilityContractsController, MembershipPaymentController,
        FacilityCouponPriceController, UserMessageController,
        AdminFormTemplateController, FacilityFormController, FormController,
        FacilityCouponPriceController, FacilityAccessCodeImportController, FormPaymentController,
        AdminGlobalNotificationController, FacilityCourseParticipantController])
class UrlMappingsTests {

    void testUrlMappings() {
        assertUrlMapping("/", controller: "home", action: "index")

        assertUrlMapping("/activityPayment/cancel", controller: "activityPayment", action: "cancel")
        assertUrlMapping("/activityPayment/confirm", controller: "activityPayment", action: "confirm")
        assertUrlMapping("/activityPayment/pay", controller: "activityPayment", action: "pay")
        assertUrlMapping("/activityPayment/process", controller: "activityPayment", action: "process")
        assertUrlMapping("/activityPayment/receipt", controller: "activityPayment", action: "receipt")

        assertUrlMapping("/admin/index", controller: "adminHome", action: "index")
        assertUrlMapping("/admin/switchFacility", controller: "adminHome", action: "switchFacility")

        assertUrlMapping("/admin/facility/create", controller: "adminFacility", action: "create")
        assertUrlMapping("/admin/facility/index", controller: "adminFacility", action: "index")
        assertUrlMapping("/admin/facility/save", controller: "adminFacility", action: "save")

        assertUrlMapping("/admin/facility/contracts/create", controller: "adminFacilityContracts", action: "create")
        assertUrlMapping("/admin/facility/contracts/edit", controller: "adminFacilityContracts", action: "edit")
        assertUrlMapping("/admin/facility/contracts/index", controller: "adminFacilityContracts", action: "index")

        assertUrlMapping("/admin/facility/courts/create", controller: "adminFacilityCourts", action: "create")
        assertUrlMapping("/admin/facility/courts/edit", controller: "adminFacilityCourts", action: "edit")
        assertUrlMapping("/admin/facility/courts/index", controller: "adminFacilityCourts", action: "index")

        assertUrlMapping("/admin/facility/properties/index", controller: "adminFacilityProperties", action: "index")
        assertUrlMapping("/admin/facility/properties/update", controller: "adminFacilityProperties", action: "update")

        assertUrlMapping("/admin/formTemplates/index", controller: "adminFormTemplate", action: "index")

        assertUrlMapping("/admin/notifications/index", controller: "adminGlobalNotification", action: "index")

        assertUrlMapping("/admin/job/index", controller: "adminJob", action: "index")
        assertUrlMapping("/admin/job/forceFortnoxCustomerSync", controller: "adminJob", action: "forceFortnoxCustomerSync")
        assertUrlMapping("/admin/job/runCashRegisterHistory", controller: "adminJob", action: "runCashRegisterHistory")
        assertUrlMapping("/admin/job/runRedeemSubscriptionCancellations", controller: "adminJob", action: "runRedeemSubscriptionCancellations")

        assertUrlMapping("/admin/orders/index", controller: "adminOrder", action: "index")
        assertUrlMapping("/admin/orders/paymentDetail", controller: "adminOrder", action: "paymentDetail")

        assertUrlMapping("/admin/region/create", controller: "adminRegion", action: "create")
        assertUrlMapping("/admin/region/index", controller: "adminRegion", action: "index")
        assertUrlMapping("/admin/region/save", controller: "adminRegion", action: "save")

        assertUrlMapping("/admin/system", controller: "adminSystem", action: "index")

        assertUrlMapping("/admin/users/devices", controller: "adminUser", action: "devices")
        assertUrlMapping("/admin/users/edit", controller: "adminUser", action: "edit")
        assertUrlMapping("/admin/users/index", controller: "adminUser", action: "index")

        assertUrlMapping("/autoCompleteSupport/courtHours", controller: "autoCompleteSupport", action: "courtHours")
        assertUrlMapping("/autoCompleteSupport/customerSelect2", controller: "autoCompleteSupport", action: "customerSelect2")
        assertUrlMapping("/autoCompleteSupport/customers", controller: "autoCompleteSupport", action: "customers")
        assertUrlMapping("/autoCompleteSupport/facilities", controller: "autoCompleteSupport", action: "facilities")
        assertUrlMapping("/autoCompleteSupport/familyMemberSelect", controller: "autoCompleteSupport", action: "familyMemberSelect")
        assertUrlMapping("/autoCompleteSupport/opponents", controller: "autoCompleteSupport", action: "opponents")
        assertUrlMapping("/autoCompleteSupport/userOnEmail", controller: "autoCompleteSupport", action: "userOnEmail")

        assertUrlMapping("/couponPayment/confirm", controller: "couponPayment", action: "confirm")
        assertUrlMapping("/couponPayment/pay", controller: "couponPayment", action: "pay")
        assertUrlMapping("/couponPayment/process", controller: "couponPayment", action: "process")
        assertUrlMapping("/couponPayment/receipt", controller: "couponPayment", action: "receipt")

        assertUrlMapping("/facilities/abc/12345", controller: "facility", action: "show") {
            name = "abc"
            wl = "12345"
        }
        assertUrlMapping("/facilities/index", controller: "facility", action: "index")
        assertUrlMapping("/facilities/membership/abc", controller: "membershipRequest", action: "index") { name = "abc" }
        assertUrlMapping("/facilities/membership/request", controller: "membershipRequest", action: "request")

        assertUrlMapping("/facilities/membership/payment/confirm", controller: "membershipPayment", action: "confirm")
        assertUrlMapping("/facilities/membership/payment/pay", controller: "membershipPayment", action: "pay")

        assertUrlMapping("/facility/accesscodes/index", controller: "facilityAccessCode", action: "index")
        assertUrlMapping("/facility/accesscodes/update", controller: "facilityAccessCode", action: "update")
        assertUrlMapping("/facility/accesscodes/import/import", controller: "facilityAccessCodeImport", action: "import")
        assertUrlMapping("/facility/activities/create", controller: "facilityActivity", action: "create")
        assertUrlMapping("/facility/activities/index", controller: "facilityActivity", action: "index")
        assertUrlMapping("/facility/activities/occasions/create", controller: "facilityActivityOccasion", action: "create")
        assertUrlMapping("/facility/activities/occasions/edit", controller: "facilityActivityOccasion", action: "edit")
        assertUrlMapping("/facility/administration/index", controller: "facilityAdministration", action: "index")
        assertUrlMapping("/facility/administration/settings", controller: "facilityAdministration", action: "settings")
        assertUrlMapping("/facility/booking/checkUpdate", controller: "facilityBooking", action: "checkUpdate")
        assertUrlMapping("/facility/booking/index", controller: "facilityBooking", action: "index")
        assertUrlMapping("/facility/campaign/index", controller: "facilityCampaign", action: "index")
        assertUrlMapping("/facility/coupons/conditions/form", controller: "facilityCouponCondition", action: "form")
        assertUrlMapping("/facility/coupons/conditions/list", controller: "facilityCouponCondition", action: "list")
        assertUrlMapping("/facility/coupons/edit", controller: "facilityOffer", action: "edit")
        assertUrlMapping("/facility/coupons/index", controller: "facilityOffer", action: "index")
        assertUrlMapping("/facility/coupons/prices/index/12345", controller: "facilityCouponPrice", action: "index") { id = "12345" }
        assertUrlMapping("/facility/coupons/ruleForm/12345", controller: "facilityOffer", action: "ruleForm") { id = "12345" }
        assertUrlMapping("/facility/course/participants/index", controller: "facilityCourseParticipant", action: "index")
        assertUrlMapping("/facility/courts/edit", controller: "facilityCourts", action: "edit")
        assertUrlMapping("/facility/courts/index", controller: "facilityCourts", action: "index")
        assertUrlMapping("/facility/customercategories/create", controller: "facilityCustomerCategory", action: "create")
        assertUrlMapping("/facility/customercategories/index", controller: "facilityCustomerCategory", action: "index")
        assertUrlMapping("/facility/customers/archive/archive", controller: "facilityCustomerArchive", action: "archive")
        assertUrlMapping("/facility/customers/edit", controller: "facilityCustomer", action: "edit")
        assertUrlMapping("/facility/customers/export", controller: "facilityCustomer", action: "export")
        assertUrlMapping("/facility/customers/family/add", controller: "facilityCustomerFamily", action: "add")
        assertUrlMapping("/facility/customers/family/create", controller: "facilityCustomerFamily", action: "create")
        assertUrlMapping("/facility/customers/import/import", controller: "facilityCustomerImport", action: "import")
        assertUrlMapping("/facility/customers/index", controller: "facilityCustomer", action: "index")
        assertUrlMapping("/facility/customers/invite/invite", controller: "facilityCustomerInvite", action: "invite")
        assertUrlMapping("/facility/customers/members/index", controller: "facilityCustomerMembers", action: "index")
        assertUrlMapping("/facility/customers/members/invoice/createMembershipInvoice", controller: "facilityCustomerMembersInvoice", action: "createMembershipInvoice")
        assertUrlMapping("/facility/customers/members/removeMembership", controller: "facilityCustomerMembers", action: "removeMembership")
        assertUrlMapping("/facility/customers/merge/merge", controller: "facilityCustomerMerge", action: "merge")
        assertUrlMapping("/facility/customers/message/message", controller: "facilityCustomerMessage", action: "message")
        assertUrlMapping("/facility/customers/requestUpdate/sendRequest", controller: "facilityCustomerUpdateRequest", action: "sendRequest")
        assertUrlMapping("/facility/forms/index", controller: "facilityForm", action: "index")
        assertUrlMapping("/facility/groups/create", controller: "facilityGroup", action: "create")
        assertUrlMapping("/facility/groups/index", controller: "facilityGroup", action: "index")
        assertUrlMapping("/facility/invoice/edit", controller: "facilityInvoice", action: "edit")
        assertUrlMapping("/facility/invoice/index", controller: "facilityInvoice", action: "index")
        assertUrlMapping("/facility/invoice/payments/index", controller: "facilityInvoicePayment", action: "index")
        assertUrlMapping("/facility/invoice/payments/makePayments", controller: "facilityInvoicePayment", action: "makePayments")
        assertUrlMapping("/facility/invoicerows/index", controller: "facilityInvoiceRow", action: "index")
        assertUrlMapping("/facility/invoicerows/remove", controller: "facilityInvoiceRow", action: "remove")
        assertUrlMapping("/facility/membertypes/edit", controller: "facilityMembershipType", action: "edit")
        assertUrlMapping("/facility/membertypes/index", controller: "facilityMembershipType", action: "index")
        assertUrlMapping("/facility/messages/form", controller: "facilityMessage", action: "form")
        assertUrlMapping("/facility/messages/index", controller: "facilityMessage", action: "index")
        assertUrlMapping("/facility/pricelist/conditions/form", controller: "facilityPriceListCondition", action: "form")
        assertUrlMapping("/facility/pricelist/conditions/index", controller: "facilityPriceListCondition", action: "index")
        assertUrlMapping("/facility/pricelist/create", controller: "facilityPriceList", action: "create")
        assertUrlMapping("/facility/pricelist/index", controller: "facilityPriceList", action: "index")
        assertUrlMapping("/facility/seasons/create", controller: "facilitySeason", action: "create")
        assertUrlMapping("/facility/seasons/index", controller: "facilitySeason", action: "index")
        assertUrlMapping("/facility/seasons/deviations/confirm", controller: "facilitySeasonDeviation", action: "confirm")
        assertUrlMapping("/facility/seasons/deviations/create", controller: "facilitySeasonDeviation", action: "create")
        assertUrlMapping("/facility/statistic/index", controller: "facilityStatistic", action: "index")
        assertUrlMapping("/facility/statistic/payment", controller: "facilityStatistic", action: "payment")
        assertUrlMapping("/facility/subscriptions/create", controller: "facilitySubscription", action: "create")
        assertUrlMapping("/facility/subscriptions/index", controller: "facilitySubscription", action: "index")
        assertUrlMapping("/facility/subscriptions/copy/copy", controller: "facilitySubscriptionCopy", action: "copy")
        assertUrlMapping("/facility/subscriptions/copy/index", controller: "facilitySubscriptionCopy", action: "index")
        assertUrlMapping("/facility/subscriptions/delete/delete", controller: "facilitySubscriptionDelete", action: "delete")
        assertUrlMapping("/facility/subscriptions/import/index", controller: "facilitySubscriptionImport", action: "index")
        assertUrlMapping("/facility/subscriptions/import/import", controller: "facilitySubscriptionImport", action: "import")
        assertUrlMapping("/facility/subscriptions/invoice/createSubscriptionInvoice", controller: "facilitySubscriptionInvoice", action: "createSubscriptionInvoice")
        assertUrlMapping("/facility/subscriptions/message/index", controller: "facilitySubscriptionMessage", action: "index")
        assertUrlMapping("/facility/subscriptions/message/message", controller: "facilitySubscriptionMessage", action: "message")

        assertUrlMapping("/forms/12345", controller: "form", action: "show") {
            hash = "12345"
        }
        assertUrlMapping("/forms/submit", controller: "form", action: "submit")

        assertUrlMapping("/forms/payment/confirm/12345", controller: "formPayment", action: "confirm") {
            id = "12345"
        }

        assertUrlMapping("/info/index", controller: "info", action: "index")
        assertUrlMapping("/info/login", controller: "info", action: "login")
        assertUrlMapping("/info/register", controller: "info", action: "register")
        assertUrlMapping("/invite/12345", controller: "info", action: "invite") { ticket = "12345" }

        assertUrlMapping("/login/auth", controller: "login", action: "auth")
        assertUrlMapping("/login/index", controller: "login", action: "index")

        assertUrlMapping("/login/password/change", controller: "resetPassword", action: "change")
        assertUrlMapping("/login/password/reset", controller: "resetPassword", action: "reset")

        assertUrlMapping("/loginSuccess/index", controller: "loginSuccess", action: "index")

        assertUrlMapping("/logout", controller: "logout")

        assertUrlMapping("/matching/index", controller: "matching", action: "index")

        assertUrlMapping("/message/index", controller: "message", action: "index")
        assertUrlMapping("/message/sendMessage", controller: "message", action: "sendMessage")

        assertUrlMapping("/orderPayment/confirm", controller: "orderPayment", action: "confirm")

        assertUrlMapping("/bookingPayment/confirm", controller: "bookingPayment", action: "confirm")
        assertUrlMapping("/bookingPayment/payEntryPoint", controller: "bookingPayment", action: "payEntryPoint")
        assertUrlMapping("/bookingPayment/receipt", controller: "bookingPayment", action: "receipt")

        assertUrlMapping("/profile/account", controller: "userProfile", action: "account")

        assertUrlMapping("/registration/enable", controller: "userRegistration", action: "enable")
        assertUrlMapping("/registration/save", controller: "userRegistration", action: "save")
        assertUrlMapping("/registration/user", controller: "userRegistration", action: "index")

        assertUrlMapping("/updateRequest/12345", controller: "updateCustomerRequest", action: "index") { ticket = "12345" }
        assertUrlMapping("/updateRequest/update", controller: "updateCustomerRequest", action: "update")

        assertUrlMapping("/profile/messages/index", controller: "userMessage", action: "index")

        assertForwardUrlMapping("/home/getmatchi", controller: "home", action: "getmatchi")
        assertForwardUrlMapping("/home/index", controller: "home", action: "index")
        assertForwardUrlMapping("/home/interested", controller: "home", action: "interested")

        assertUrlMapping("/booking/cancel", controller: "userBooking", action: "cancelByTicket")
        assertUrlMapping("/booking/show", controller: "userBooking", action: "showByTicket")

        assertUrlMapping("/customer/disableClubMessages", controller: "facilityCustomer", action: "disableClubMessagesByTicket")
        assertUrlMapping("/customer/show", controller: "facilityCustomer", action: "showByTicket")
    }
}