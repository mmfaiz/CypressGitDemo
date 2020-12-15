package com.matchi

class FacilityUserRole implements Serializable {

    private static final long serialVersionUID = 12L

    AccessRight accessRight

    static belongsTo = [facilityUser: FacilityUser]

    static constraints = {
    }

    static mapping = {
        version false
        cache true
    }

    static namedQueries = {
        granted { user, rights = null ->
            createAlias("facilityUser", "fu")
            eq("fu.facility", user.facility)
            eq("fu.user", user)
            if (rights) {
                inList("accessRight", rights)
            }
            cache true
        }
    }

    public static enum AccessRight {
        CUSTOMER(["facilityCustomer", "facilityCustomerMembers", "facilityCustomerArchive",
                "facilityCustomerFamily", "facilityCustomerImport", "facilityCustomerInvite",
                "facilityCustomerMerge", "facilityCustomerMessage", "facilityCustomerSMSMessage",
                "facilityCustomerUpdateRequest", "facilityCustomerGroup", "scheduledTask",
                "facilityOffer", "facilityOfferFlow", "facilityGroup", "facilityCustomerMembersFlow"]),
        INVOICE(["facilityInvoice", "facilityInvoicePayment", "facilityInvoiceRow", "facilityInvoiceRowFlow", "facilityInvoiceFlow"]),
        SCHEDULE(["facilityBooking", "facilityActivity", "facilityActivityOccasion"]),
        TRAINING_PLANNER(["facilityCustomer", "facilityCustomerMembers", "facilityCustomerArchive",
                "facilityCustomerFamily", "facilityCustomerImport", "facilityCustomerInvite",
                "facilityCustomerMerge", "facilityCustomerMessage", "facilityCustomerSMSMessage",
                "facilityCustomerUpdateRequest", "facilityCustomerGroup", "facilityGroup", "scheduledTask",
                "facilityOffer", "facilityOfferFlow", "facilityCourse", "facilityCourseFlow", "facilityCourseParticipant", "facilityCourseParticipantFlow",
                "facilityCourseSubmission", "facilityCourseSubmissionFlow", "trainer",
                "facilityForm"]),
        FACILITY_ADMIN([]),
        INTEGRATION_BASIC([]),
        INTEGRATION_ACCOUNTING([])

        // list of accessible controllers
        // first value - default target controller (e.g. for "Min klubb" link)
        final List controllers

        AccessRight(List controllers) {
            this.controllers = controllers.asImmutable()
        }

        static List<AccessRight> byController(String controller) {
            values().findAll { it.controllers.contains(controller) }
        }

        static List list() {
            [SCHEDULE, CUSTOMER, INVOICE, TRAINING_PLANNER, FACILITY_ADMIN, INTEGRATION_BASIC, INTEGRATION_ACCOUNTING]
        }
    }
}
