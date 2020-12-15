package com.matchi


import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.Submission
import org.codehaus.groovy.grails.plugins.web.taglib.FormatTagLib

import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat

/**
 * @author Sergei Shushkevich
 */
class FormController extends GenericController {

    def customerService
    def messageSource
    def notificationService
    def submissionService

    public static final String SUBMISSION_SESSION_KEY = "form_submission"

    def beforeInterceptor = [action: this.&checkConstraints]

    private void redirectForms(String hash, String currentController) {
        Form formInstance = Form.findByHash(hash)

        // If membership is required, we send you to the page for showing such forms
        if (formInstance) {
            if (formInstance.paymentRequired && !formInstance.membershipRequired) {
                redirectIfNotThere(currentController, hash, "showProtectedForm")
            } else if (formInstance.membershipRequired) {
                redirectIfNotThere(currentController, hash, "showMemberForm")
            } else {
                redirectIfNotThere(currentController, hash, "show")
            }
        }
    }

    private void redirectIfNotThere(String currentController, String hash, String destination) {
        if (currentController != destination) {
            def redirectParams = [hash: hash]
            redirectParams << params
            redirect(controller: "form", action: destination, params: redirectParams)
        }
    }

    private LinkedHashMap displayForms(Form formInstance, String formType = "") {
        def fields = FormField.findAllByFormAndIsActive(formInstance, true)
        LinkedHashMap returnForm = [view: "show", model: [formInstance: formInstance, fields: fields]]
        // Only continue if admin or we have rights to fill the form
        if (!formInstance) {
            return null
        }

        if (securityService.hasFacilityAccessTo(formInstance.facility)) {
            return returnForm
        }

        if (!formInstance.checkUser(getCurrentUser())) {
            return null
        }

        if (!formInstance.isActiveNow()) {
            return [view: "inactive", model: [formInstance: formInstance]]
        }

        return returnForm
    }

    def show(String hash) {
        redirectForms(hash, "show")
        Form formInstance = Form.findByHash(hash)

        render displayForms(formInstance)

    }

    // This action requires you to be logged in. However, it does not prevent you from
    // showing ordinary forms that has now membership requirements. This way, either link
    // could be used for a form and show() and showMemberForm() will make sure you are logged in
    // if membership is required
    def showMemberForm(String hash) {
        redirectForms(hash, "showMemberForm")
        Form formInstance = Form.findByHash(hash)


        Map form = displayForms(formInstance, "showMemberForm")
        if (form) {
            render form
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def showProtectedForm(String hash) {
        redirectForms(hash, "showProtectedForm")
        Form formInstance = Form.findByHash(hash)

        Map form = displayForms(formInstance)
        if (form) {
            render form
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def submit(Long id) {
        def formInstance = Form.get(id)
        if (formInstance) {
            Submission submission = createSubmission(formInstance)
            Submission previousSubmission = submissionService.findExistingActivitySubmission(submission.customer,
                    formInstance.getActivity())

            Participant participant = Participant.findByCustomerAndActivity(submission.customer, formInstance.activity)
            boolean replacedOld = false

            // Non-admins cannot submit applications where one is already a participant
            if (!hasFacilityFullRights() && formInstance.activity && submission.customer
                    && !(securityService.hasFacilityAccess() && params.customerId)
                    && Participant.countByActivityAndCustomer(formInstance.activity, submission.customer)) {
                submission.errors.reject("form.submit.alreadySubmitted", null, null)
            }

            if (!submission.hasErrors() && submission.save()) {

                // If there was an old non-processed submission, we replace it with this new one but keep original application data
                if (previousSubmission && !previousSubmission.isProcessed()) {
                    submission.originalDate = previousSubmission.dateCreated
                    submission.status = previousSubmission.status
                    submission.save()
                    replacedOld = true
                    previousSubmission.delete()
                }

                // Following logic is only applicable for courses
                if (formInstance.getActivity() instanceof CourseActivity) {
                    // If we are creating a submission for a course were the customer is participating,
                    // but has no submission, we connect them
                    if (participant != null && participant.submission == null) {
                        participant.submission = submission
                        participant.save()

                        submission.status = Submission.Status.ACCEPTED
                        submission.save()
                    }
                }

                if (formInstance.activity && submission.customer) {
                    if (params.boolean("sendSubmissionNotification")) {
                        if (formInstance.course) {
                            notificationService.sendActivitySubmissionNotification(
                                    submission.customer, formInstance.course)
                        } else if (formInstance.event) {
                            notificationService.sendEventActivitySubmissionNotification(
                                    submission.customer, formInstance.event)
                        }
                        notificationService.sendFormSubmissionCompletedNotification(submission.customer, formInstance.activity)
                    }
                }

                if (formInstance.activity) {
                    if (replacedOld) {
                        flash.message = message(code: "form.submit.course.thankYouMessageReplaced",
                                args: [formInstance.activity.name])
                    } else {
                        flash.message = message(code: "form.submit.course.thankYouMessage",
                                args: [formInstance.activity.name])
                    }

                } else {
                    flash.message = message(code: "form.submit.thankYouMessage")
                }

                if (params.returnUrl && params.returnUrl.size() > 0) {
                    redirect url: params.returnUrl
                } else {
                    redirect(controller: "facility", action: "show", params: [name: formInstance?.facility?.shortname])
                }
            } else {
                render(view: "show", model: [formInstance: formInstance, submission: submission,
                                             fields      : FormField.findAllByFormAndIsActive(formInstance, true)])
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def submitAndSendNotification() {
        params.sendSubmissionNotification = "true"
        forward(action: "submit")
    }

    def submitAndPay(Long id) {
        def formInstance = Form.get(id)
        if (formInstance) {
            log.info("SUBMIT AND PAY  - ${formInstance.name}")
            def submission = createSubmission(formInstance)


            if(submission.customer && !(securityService.hasFacilityAccess() && params.customerId) &&
                (//if activity form and already has a submission
                (formInstance?.activity && Submission.countByFormAndCustomer(formInstance,submission.customer))
                ||//or activity form and already has a participant
                (formInstance.activity && Participant.countByActivityAndCustomer(formInstance.activity, submission.customer))
                || //or event form and already has a submission
                (formInstance?.event && Submission.countByFormAndCustomer(formInstance,submission.customer))
                )) {
                    submission.errors.reject("form.submit.alreadySubmitted", null, null)
                }

            def model = [formInstance: formInstance, submission: submission,
                         fields      : FormField.findAllByFormAndIsActive(formInstance, true)]

            if (!submission.hasErrors() && submission.validate()) {
                submission.discard()
                log.info("SUBMIT AND PAY TO SESSION - ${formInstance.name}")
                session[SUBMISSION_SESSION_KEY] = submission
                model.pay = true
            }

            render(view: "show", model: model)
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    boolean checkConstraints() {
        Form form = params.hash ? Form.findByHash(params.hash) : Form.get(params.id)

        if (!form) {
            response.sendError HttpServletResponse.SC_NOT_FOUND
            return false
        }

        if (securityService.hasFacilityAccessTo(form.facility)) {
            return true
        }

        if (!form?.submissionAllowed) {
            flash.error = message(code: "form.maxSubmissions.error", args: [(form.getActivity()?.name ?: form?.name)])

            if (params.returnUrl && params.returnUrl.size() > 0) {
                redirect url: params.returnUrl
            } else {
                redirect(controller: "userProfile", action: "home")
            }
            return false
        }

        return true
    }

    private Customer getCustomer(Form form) {
        def currentUser = getCurrentUser()
        currentUser ? Customer.findByUserAndFacility(currentUser, form.facility) : null
    }

    private Submission createSubmission(Form formInstance) {
        def submission = new Submission(form: formInstance, submissionIssuer: getCurrentUser())

        if (formInstance.getActivity() instanceof CourseActivity) {
            submission.status = Submission.Status.WAITING
        }
        FormField.findAllByFormAndIsActive(formInstance, true).each { FormField field ->
            field.typeEnum.binder.newInstance(field, submission, params, messageSource,
                    FormatTagLib.resolveLocale(null)).bind()
        }
        if (!submission.hasErrors() && submission.validate()) {
            if (securityService.hasFacilityAccess() && params.customerId) {
                submission.customer = Customer.findByIdAndFacility(
                        params.long("customerId"), formInstance.facility)
            }

            def pi = submission.values.findAll {
                it.fieldType == FormField.Type.PERSONAL_INFORMATION.name() ||
                        it.fieldType == FormField.Type.CLUB.name()
            }

            if (pi) {
                def customerProps = [guardian: [:]]

                pi.each {
                    customerProps[it.input] = it.value
                }
                if (customerProps.security_number) {
                    Facility facility = formInstance.facility
                    PersonalNumberSettings personalNumberSettings = facility.getPersonalNumberSettings()
                    customerProps.security_number.find(~/^(\d{6}|\d{8})(?:-(\d{4,5}))?$/) { match, dob, sn ->
                        def dateFormat = new SimpleDateFormat(dob.size() == 6 ?
                                personalNumberSettings.shortFormat :
                                personalNumberSettings.longFormat)
                        dateFormat.lenient = false
                        customerProps.dateOfBirth = dateFormat.parse(dob)
                        customerProps.securityNumber = sn
                        customerProps.remove("security_number")
                    }
                }
                if (customerProps.gender) {
                    customerProps.type = Customer.CustomerType.valueOf(customerProps.gender)
                    customerProps.remove("gender")
                }

                submission.values.findAll {
                    it.fieldType == FormField.Type.ADDRESS.name()
                }.each {
                    customerProps[it.input] = it.value
                }
                submission.values.findAll {
                    it.fieldType == FormField.Type.PARENT_INFORMATION.name()
                }.each {
                    customerProps.guardian[it.input] = it.value
                }

                if (submission.customer) {
                    customerService.updateCustomer(submission.customer, customerProps)
                } else {
                    submission.customer = customerService.getOrCreateCustomer(
                            customerProps, formInstance.facility, true)
                }
            }

            if (!submission.customer && submission.submissionIssuer) {
                submission.customer = customerService.getOrCreateUserCustomer(
                        submission.submissionIssuer, formInstance.facility)
            }

            if (!submission.customer) {
                String values = submission.values.collect {
                    return "${it.label}, ${it.input}: ${it.value}"
                }.join(" - ")
                log.error("Customer is null ${formInstance.name} (${formInstance.id}) - ${getCurrentUser()}. ${values}")

                submission.errors.reject("form.submit.noCustomer.error",
                        [(submission.form.course?.name ?: submission.form.name), submission.form.facility.name] as String[], null)
            } else {
                customerService.linkCustomerToUser(submission.customer)
            }

            if (submission.form.membershipRequired && !submission.customer?.hasActiveMembership()
                    && !submission.customer?.membership?.inStartingGracePeriod) {
                submission.errors.reject("form.membershipRequired.error",
                        [(submission.form.course?.name ?: submission.form.name), submission.form.facility.name] as String[], null)
            }
        }

        submission
    }
}
