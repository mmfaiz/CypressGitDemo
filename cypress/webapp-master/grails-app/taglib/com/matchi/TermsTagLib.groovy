package com.matchi

class TermsTagLib {

    def springSecurityService

    def termsModal = { attrs, body ->
        if(!show())
            return

        if(!attrs.skipCheck) {
            User user = springSecurityService.getCurrentUser() as User
            attrs << [showTerms: user && !user.dateAgreedToTerms]
        }

        if(attrs.existingUserMode) {
            attrs.existingUserMode = attrs.existingUserMode as Boolean
        }

        out << render(template: "/templates/user/termsModal", model: attrs)
    }

    // Do not show terms modal in user agreement or integrity policy pages
    // because we are linking to them from modal to avoid modal popping there also.
    private boolean show() {
        String requestUrl = request.requestURL.toString()
        return !(requestUrl.endsWith("privacypolicy") || requestUrl.endsWith("useragreement"))
    }
}
