package com.matchi

import com.matchi.coupon.Coupon

class GenericController {

    public static final String CUSTOMER_IDS_KEY = "customerIds"

    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService
    def securityService
    def pdfRenderingService

    protected User getCurrentUser() {
        return springSecurityService.getCurrentUser()
    }

    protected Facility getUserFacility() {
        securityService.getUserFacility()
    }

    protected def getRequiredUserFacility() {
        def facility = getUserFacility()

        if (!facility) {
            raiseFacilityAccessException(null)
        }
        return facility
    }

    protected def getUserBookings() {
        def user = getCurrentUser()
        return (user != null && user.bookings != null) ? user.bookings : null
    }

    protected void assertFacilityAccess(Facility objectFacility) {
        def facility = getUserFacility()

        if (!facility || !objectFacility) {
            raiseFacilityAccessException(objectFacility)
        }

        if (facility.id != objectFacility.id && !objectFacility.memberFacilities.contains(facility)) {
            raiseFacilityAccessException(objectFacility)
        }
    }

    protected void assertFacilityAccessTo(def resource, Facility facility = null) {

        if (resource == null) {
            return
        }

        if (resource.metaClass.hasProperty(resource, "facility")) {
            assertFacilityAccess(resource.facility)
        } else if (facility != null) {
            assertFacilityAccess(facility)
        } else {
            throw new IllegalArgumentException("No facility in resource")
        }

    }

    protected void raiseFacilityAccessException(Facility facility) {
        throw new SecurityException("User (${getCurrentUser()?.email}) is not allowed access to ${facility?.name ?: "(no facility)"} ")
    }

    protected String addParam(url, key, value) {
        (url.contains("?") ? "&" : "?") + key + "=" + URLEncoder.encode(value, "UTF-8")
    }

    protected Class specifyOfferClass(String type) {
        if (!type) {
            return Coupon.class
        }
        grailsApplication.getDomainClass("com.matchi.coupon.${type}").clazz
    }

    protected def getPdfFile(def template, def model) {
        def pdf = new ByteArrayOutputStream().withStream { outputStream ->
            pdfRenderingService.render(template: template, model: model, outputStream).toByteArray()
        }
        pdf
    }

    protected void assertHierarchicalFacilityAccessTo(Customer customer) {
        def facility = getUserFacility()
        if (facility.id == customer.facility.id) return
        if (facility.isMasterFacility()) {
            if (!facility.getMemberFacilities().contains(customer.facility)) {
                raiseFacilityAccessException(customer.facility)
            }
        } else {
            if (!facility.getMasterFacilities().contains(customer.facility)) {
                raiseFacilityAccessException(customer.facility)
            }
        }
    }

    protected def getCountryVats(Facility facility) {
        def noVat = [0: message(code: "default.vat.none")]
        def percentSign = "%"
        def countryVats = [:]
        def settingsVAT = grailsApplication.config.matchi.settings.countryVAT[facility.country]
        if (settingsVAT) {
            settingsVAT.each {
                countryVats.put(it, it + percentSign)
            }
        } else {
            if (facility.vat != 0) {
                countryVats.put(facility.vat, facility.vat + percentSign)
            }
        }
        noVat + countryVats
    }
}
