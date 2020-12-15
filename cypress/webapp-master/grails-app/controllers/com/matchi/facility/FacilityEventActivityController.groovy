package com.matchi.facility

import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.Group
import com.matchi.activities.EventActivity
import com.matchi.async.ScheduledTask
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.FormFieldValue
import com.matchi.dynamicforms.FormTemplate
import com.matchi.dynamicforms.Submission
import com.matchi.requirements.RequirementProfile
import grails.validation.Validateable
import org.springframework.dao.DataIntegrityViolationException

/**
 * @author Sergei Shushkevich
 */
class FacilityEventActivityController extends GenericController {

    def messageSource
    def excelExportManager
    def fileArchiveService
    def scheduledTaskService

    final static int RESULTS_PER_PAGE = 50

    def index() {
        def facility = getUserFacility()

        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        if (!params.sort) {
            params.sort = "startDate"
            params.order = "desc"
        }

        [
            eventActivityInstanceList: EventActivity.facilityActiveEvents(facility).list(params),
            eventActivityInstanceTotal: EventActivity.facilityActiveEvents(facility).count(),
            facility: facility
        ]
    }

    def create() {
        buildModel()
    }

    def save() {
        def eventActivityInstance = new EventActivity()
        def facility = getUserFacility()

        bindData(eventActivityInstance, params)
        if (params.'form.relatedFormTemplate.id') {
            initAndSaveActivityForm(eventActivityInstance.form,
                    eventActivityInstance, facility)
        } else {
            eventActivityInstance.form = null
        }
        eventActivityInstance.facility = facility

        if (eventActivityInstance.save(flush: true)) {
            flash.message = message(code: 'default.created.message',
                    args: [message(code: 'eventActivity.label'), eventActivityInstance.name])
            redirect(action: "index")
        } else {
            render(view: "create", model: buildModel(eventActivityInstance, facility))
        }
    }

    def edit(Long id) {
        def eventActivityInstance = getEventActivity(id)
        if (eventActivityInstance) {
            buildModel(eventActivityInstance)
        }
    }

    def update(Long id, Long version) {
        def eventActivityInstance = getEventActivity(id)
        if (eventActivityInstance) {
            def facility = getUserFacility()

            if (version != null) {
                if (eventActivityInstance.version > version) {
                    eventActivityInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                            [message(code: 'eventActivity.label')] as Object[],
                            "Another user has updated this EventActivity while you were editing")
                    render(view: "edit", model: buildModel(eventActivityInstance, facility))
                    return
                }
            }

            bindData(eventActivityInstance, params, [exclude: ['form*']])

            Form form = eventActivityInstance?.form
            bindData(form, params.form)

            if (params.noLimit) {
                form.maxSubmissions = null
            }
            def newFields = parseFormFields(form)
            def fieldsToDelete = form.fields.findAll { f ->
                !newFields.find { it.id == f.id }
            }
            form.fields?.clear()
            newFields?.each {
                form.addToFields(it)
            }
            eventActivityInstance.form = form

            if (!eventActivityInstance.save(flush: true)) {
                render(view: "edit", model: buildModel(eventActivityInstance, facility))
                return
            }

            fieldsToDelete.each {
                it.delete()
            }

            flash.message = message(code: 'default.updated.message',
                    args: [message(code: 'eventActivity.label'), eventActivityInstance.name])

            redirect(action: "index")
        }
    }

    def delete(Long id) {
        def eventActivityInstance = getEventActivity(id)
        if (eventActivityInstance) {
            try {
                eventActivityInstance.delete(flush: true)
                flash.message = message(code: 'default.deleted.message',
                        args: [message(code: 'eventActivity.label'), id])
            } catch (DataIntegrityViolationException e) {
                flash.message = message(code: 'default.not.deleted.message',
                        args: [message(code: 'eventActivity.label'), id])
            }

            redirect(action: "index")
        }
    }

    def submissions(EventSubmissionCommand cmd) {
        def eventActivityInstance = getEventActivity(cmd.id)
        Locale locale = new Locale(getCurrentUser().language)
        String notSpecified = messageSource.getMessage("facilityEventParticipant.notSpecified", null, locale)

        if (eventActivityInstance) {
            params.max = Math.min(params.int("max", RESULTS_PER_PAGE), 100)
            if (!params.sort) {
                params.sort = "c.firstname,c.lastname"
            }

            def allSubmissions = Submission.createCriteria().list() {
                createAlias("customer", "c")
                createAlias("form", "f")
                eq("f.id", eventActivityInstance.form.id)
                params.sort.tokenize(",").each {
                    order(it, params.order ?: "asc")
                }
            }

            def sqlBirthYears = cmd.birthYears?.findAll { return !it.equals(notSpecified) }
                    .collect{ Integer.parseInt(it) }.unique()
            def includeNonSpecifiedBirthYear = cmd.birthYears?.any { return it.equals(notSpecified) }

            def sqlClubs = cmd.clubs?.findAll { return !it.equals(notSpecified) }.unique()
            def includeNonSpecifiedClub = cmd.clubs?.any { return it.equals(notSpecified) }

            def submissions = Submission.createCriteria().list(
                    max: params.max, offset: params.int("offset", 0)) {
                createAlias("customer", "c")
                createAlias("form", "f")

                eq("f.id", eventActivityInstance.form.id)

                or {
                    if(!sqlBirthYears?.isEmpty()) {
                        'in'("c.birthyear", sqlBirthYears)
                    }

                    if(includeNonSpecifiedBirthYear) {
                        isNull("c.birthyear")
                    }
                }

                or {
                    if(!sqlClubs?.isEmpty()) {
                        'in'("c.club", sqlClubs)
                    }

                    if(includeNonSpecifiedClub) {
                        isNull("c.club")
                    }
                }

                params.sort.tokenize(",").each {
                    order(it, params.order ?: "asc")
                }
            }.findAll { Submission s ->
                if(cmd.memberStatuses?.isEmpty()) return true

                if(cmd.memberStatuses.contains(EventSubmissionCommand.MemberStatus.MEMBER)
                        && (s.customer?.hasActiveMembership() || s.customer?.membership?.inStartingGracePeriod)) {
                    return true
                }

                if(cmd.memberStatuses.contains(EventSubmissionCommand.MemberStatus.NO_MEMBER)
                        && !s.customer?.hasActiveMembership() && !s.customer?.membership?.inStartingGracePeriod) {
                    return true
                }

                return false
            }.findAll { Submission s ->
                if(cmd.groups?.isEmpty()) return true

                return cmd.groups.any { Long groupId ->
                    if(groupId == 0) return s.customer.groups.isEmpty()
                    return s.customer.belongsTo(Group.findById(groupId))
                }
            }

            def groups = allSubmissions.collect { Submission s ->
                return s.customer.groups ?: [id: 0L, name: notSpecified]
            }.flatten().unique().sort().findAll { it != null }

            def clubs = allSubmissions.collect { Submission s ->
                return s.customer.club ?: notSpecified
            }.unique().sort().findAll { it != null }

            def birthYears = allSubmissions.collect { Submission s ->
                return s.customer.birthyear?.toString() ?: notSpecified
            }.findAll { it != null }.unique().sort()

            def model = [submissions: submissions, totalCount: allSubmissions.size(),
                         eventActivityInstance: eventActivityInstance,
                         facility: eventActivityInstance.facility, filter: cmd, clubs: clubs, birthYears: birthYears,
                         groups: groups, resultsPerPage: RESULTS_PER_PAGE]

            return model
        }
    }

    def deleteSubmission(Long id) {
        def eventActivity = getEventActivity(id)
        if (eventActivity) {
            def submissions = getSelectedSubmissions(eventActivity)
            if (submissions) {
                submissions.each {
                    it.delete()
                }

                if(params.returnUrl) {
                    redirect(url: params.returnUrl)
                } else {
                    flash.info = message(code: 'eventActivity.delete.success')
                    redirect(action: "submissions", params: params)
                }
            }
        }
    }

    def preview() {
        def eventActivity = new EventActivity()

        bindData(eventActivity, params)

        eventActivity.form.facility = getUserFacility()
        eventActivity.form.event = eventActivity
        eventActivity.form.fields = parseFormFields(eventActivity.form)
        eventActivity.form.fields.eachWithIndex { field, idx ->
            field.id = idx
        }
        eventActivity.discard()

        render view: "/form/show", model: [formInstance: eventActivity.form,
                fields: eventActivity.form.fields.findAll { it.isActive }, preview: true]
    }

    def customerAction(Long id) {
        def eventActivity = getEventActivity(id)
        if (eventActivity) {
            def customerIds = getSelectedSubmissions(eventActivity).collect {it.customer.id}.unique()
            if (customerIds) {
                session[CUSTOMER_IDS_KEY] = customerIds

                redirect(controller: params.targetController, action: params.targetAction,
                        params: [exportType: params.exportType, returnUrl: params.returnUrl])
            }
        }
    }

    def exportSubmissions(Long id) {
        def eventActivity = getEventActivity(id)
        if (eventActivity) {
            Facility facility = eventActivity.facility
            def submissionIds = getSelectedSubmissions(eventActivity)*.id
            if (submissionIds) {

                scheduledTaskService.scheduleTask(message(code: 'scheduledTask.exportSubmissions.taskName'),
                        facility.id, facility) { taskId ->
                    Submission.withNewSession {
                        def submissions = Submission.createCriteria().listDistinct {
                            inList("id", submissionIds)
                        }

                        def exportFile = File.createTempFile("submission_export-", ".xls")

                        exportFile.withOutputStream { out ->
                            excelExportManager.exportSubmissions(submissions, out)
                        }

                        def task = ScheduledTask.get(taskId)
                        task.resultFileName = "submission_export-${new Date().format('yyyy-MM-dd')}.xls"
                        task.resultFilePath = fileArchiveService.storeExportedFile(exportFile)
                        task.save(flush: true)

                        exportFile.delete()
                    }
                }
            }
        }

        if(params.returnUrl) {
            redirect(url: params.returnUrl)
        } else {
            redirect(action: "index", params: params)
        }
    }

    private EventActivity getEventActivity(id) {
        def eventActivity = EventActivity.findByIdAndFacility(id, getUserFacility())

        if (!eventActivity) {
            flash.message = message(code: 'default.not.found.message',
                    args: [message(code: 'eventActivity.label')])
            redirect(action: "index")
        }

        eventActivity
    }

    private List getSelectedSubmissions(EventActivity eventActivity) {
        def submissionIds
        if (params.boolean("allselected")) {
            return Submission.findAllByForm(eventActivity.form)
        } else if(params.submissionId) {
            submissionIds = params.list("submissionId").collect { Long.parseLong(it) }
        } else {
            flash.error = message(code: 'facilityCourseSubmission.index.noneSelected')
            redirect(action: "submissions", params: params)
        }

        submissionIds ? Submission.findAllByIdInList(submissionIds) : null
    }

    private void initAndSaveActivityForm(form, eventActivity, facility) {
        form.fields = []
        form.event = eventActivity
        form.name = form?.relatedFormTemplate?.name
        ArrayList newFormFields = parseFormFields(form)

        newFormFields.each {
            form.addToFields(it)
        }
        form.facility = facility
        form.save(flush: true, failOnError: true)
    }

    private ArrayList<FormField> parseFormFields(form) {
        def newFormFields = []
        def formFieldIds = params.list("formFieldId")

        if (formFieldIds.size() > 1) {
            formFieldIds.sort {
                params.int("fieldPosition$it", -1)
            }
        }

        formFieldIds.collect{ it as Long }.eachWithIndex { id, index ->
            FormField formField = form.fields.find { it.id == id }
            if (!formField) {
                FormField templateField = FormField.get(id)
                formField = new FormField(templateField.properties)
                formField.predefinedValues?.clear()
                templateField.predefinedValues.eachWithIndex { pv, pvIdx ->
                    formField.addToPredefinedValues(new FormFieldValue(pv.properties))
                }
            }

            if (formField.isEditable) {
                formField.isActive = Boolean.valueOf(params."field.isActive$id")
                formField.isRequired = params."field.isRequired$id" == 'on'

                if (formField.isActive && formField.typeEnum.customizable) {
                    formField.predefinedValues.collect().each {
                        formField.removeFromPredefinedValues(it)
                        it.delete()
                    }
                    bindData(formField, params.field."$id")
                }
            }

            formField.form = form
            newFormFields << formField
        }

        newFormFields
    }

    private Map buildModel(eventActivityInstance = new EventActivity(params),
                           Facility facility = getUserFacility()) {
        [
            eventActivityInstance: eventActivityInstance,
            formTemplates: FormTemplate.shared(facility).listDistinct(),
            facilityHasRequirementProfiles: facility.hasRequirementProfiles(),
            requirementProfiles: RequirementProfile.findAllByFacility(facility),
            facility: facility
        ]
    }

    def archive() {
        def facility = getUserFacility()

        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        if (!params.sort) {
            params.sort = "startDate"
            params.order = "desc"
        }

        render(view: "index", model: [
            eventActivityInstanceList: EventActivity.archivedEvents(facility).list(params),
            eventActivityInstanceTotal: EventActivity.archivedEvents(facility).count(),
            facility: facility
        ])
    }

}

@Validateable(nullable = true)
class EventSubmissionCommand {

    Long id
    List<String> birthYears = []
    List<MemberStatus> memberStatuses = []
    List<Long> groups = []
    List<String> clubs = []

    String toString() {
        return "[id: ${id}, birthYears: ${birthYears}, memberStatuses: ${memberStatuses.dump()}, groups: ${groups.dump()}, clubs: ${clubs.dump()}]"
    }

    static enum MemberStatus {
        MEMBER, NO_MEMBER

        static list() {
            return [MEMBER, NO_MEMBER]
        }
    }
}
