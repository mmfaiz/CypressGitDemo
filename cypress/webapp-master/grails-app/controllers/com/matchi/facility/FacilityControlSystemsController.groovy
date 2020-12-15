package com.matchi.facility

import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.GenericController
import com.matchi.ValidationUtils
import grails.validation.Validateable

class FacilityControlSystemsController extends GenericController {

    static layout = 'facilityLayout'
    def mpcService

    def index() {
        Facility facility = getUserFacility()

        def settings = mpcService.getNode(facility.id)

        if(!settings) {
            [status: null]
        } else {
            [status: settings.provider.status, type: settings.provider.type, facility: facility]
        }
    }

    def updateWarnings(UpdateWarningsCommand cmd) {
        List<String> emails

        Facility facility = getUserFacility()
        String finalEmails = facility.getMpcNotificationMails().join(',') // default to this

        try {
            if(cmd.emailAddresses?.size() > 0) {
                emails = ValidationUtils.getEmailsFromString(cmd.emailAddresses, ',')

                // If the valid emails are as many as the emails entered, things are OK.
                // If not, then some of the entered addresses must be wrong
                if(emails.size() == cmd.emailAddresses.split(',').size()) {
                    finalEmails = emails.join(',')
                } else {
                    flash.error = message(code: 'facilityControlSystems.error.email')
                    redirect action: "index"
                    return
                }
            }
        } catch(Exception e) {
            log.error(e.getMessage())
            flash.error = message(code: 'facilityControlSystems.error.email')
            redirect action: "index"
            return
        }

        if(cmd.phoneNumber?.size() > 0) {
            if(cmd.phoneNumber.matches(/^07[0-9]{8}$/)) {
                facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.MPC_NOTIFY_SMS_NUMBER, cmd.phoneNumber)
            } else {
                flash.error = message(code: 'facilityControlSystems.error.phoneNumber')
                redirect action: "index"
                return
            }
        } else {
            facility.removeFacilityProperty(FacilityProperty.FacilityPropertyKey.MPC_NOTIFY_SMS_NUMBER)
        }

        if(finalEmails) {
            facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.MPC_NOTIFY_EMAIL_ADDRESSES, finalEmails)
        } else {
            facility.removeFacilityProperty(FacilityProperty.FacilityPropertyKey.MPC_NOTIFY_EMAIL_ADDRESSES)
        }

        facility.save(flush: true)

        redirect action: "index"
        return
    }

}

@Validateable(nullable = true)
class UpdateWarningsCommand {
    String emailAddresses
    String phoneNumber
}