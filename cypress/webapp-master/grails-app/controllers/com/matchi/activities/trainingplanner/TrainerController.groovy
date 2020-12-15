package com.matchi.activities.trainingplanner

import com.matchi.Availability
import com.matchi.Booking
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.User
import com.matchi.facility.Organization
import com.matchi.invoice.InvoiceRow
import com.matchi.orders.InvoiceOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.price.Price
import grails.validation.Validateable
import org.apache.commons.lang3.StringUtils
import org.apache.kafka.common.protocol.types.Field
import org.joda.time.DateMidnight
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

class TrainerController extends GenericController {

    def fileArchiveService
    TrainerService trainerService
    def dateUtil
    def excelExportManager
    def orderStatusService
    def invoiceService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        def facility = getUserFacility()
        List<Trainer> trainers = Trainer.findAllByFacility(facility, params)
        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        [trainerInstanceList: trainers, trainerInstanceTotal: trainers.size(), facility: facility]
    }

    def create() {
        [trainerInstance: new Trainer(params), facility: getUserFacility()]
    }

    def save() {
        def trainerInstance = new Trainer()
        bindData(trainerInstance, params, ['facility', 'profileImage'])
        trainerInstance.facility = getUserFacility()
        def profileImage = retrieveUploadedImage("profileImage")
        if (profileImage) {
            trainerInstance.profileImage = profileImage
        }
        if (trainerInstance.save(flush: true)) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'trainer.label', default: 'Trainer'), trainerInstance])
            redirect(action: "list")
        } else {
            render(view: "create", model: [trainerInstance: trainerInstance])
        }
    }

    def edit() {
        def trainerInstance = Trainer.get(params.id)
        if (trainerInstance) {
            trainerInstance.profileImage
            trainerInstance.sport
            [trainerInstance: trainerInstance, facility: getUserFacility()]
        } else {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'trainer.label', default: 'Trainer')])
            redirect(action: "list")
        }
    }

    def report(TrainerReportCommand cmd) {
        def facility = getUserFacility()
        def trainers = Trainer.findAllByFacilityAndIsActive(facility, true)
        def trainerInstance = (cmd.trainerId) ? Trainer.get(cmd.trainerId) : null
        def start = dateParam(params.start, new DateMidnight().withDayOfMonth(1))
        def end = dateUtil.endOfDay(dateParam(params.end, new DateMidnight().plusMonths(1).withDayOfMonth(1).minusDays(1)))
        List<Booking> bookings = trainerService.getTrainersBookings(facility, trainerInstance, start, end).findAll { (cmd.paid == null || it.isFinalPaid() == cmd.paid) }
        def total = bookings.collect { it.order.price }.sum()

        [trainers: trainers, trainerInstance: trainerInstance, cmd: cmd, facility: facility, start: start, end: end, bookings: bookings, total: total]
    }

    def export() {
        def facility = getUserFacility()

        List<Booking> bookings = Booking.findAllByIdInList(params.list('bookingIds').collect { it as Long });

        response.contentType = "application/vnd.ms-excel"
        response.setHeader("Content-disposition",
            "attachment; filename=lessons_${facility.shortname}.xls")

        excelExportManager.exportPrivateLessons(bookings, facility, response.outputStream)
    }

    def invoice() {
        def facility = getUserFacility()

        List<Booking> bookings = Booking.findAllByIdInList(params.list('bookingIds').collect { it as Long });

        [facility      : facility, rows: bookings.collect {
            [
                bookingId   : it.id,
                customerName: it.customer.fullName(),
                description : ( message(code: 'trainer.invoiceRow.description') + " " + it.trainers?.first()?.firstName +
                    ", " + it.slot?.getShortDescription() )
            ]
        }, fortnoxItems: getFortnoxItems(facility)]
    }

    def createInvoice(LessonInvoiceRowsCommand cmd) {
        def facility = getUserFacility()
        User currentUser = (User) springSecurityService.currentUser
        if (!cmd.validate())
            render(view: "invoice", model: [rows: cmd.rows, facility: facility, articleId: cmd.articleId, fortnoxItems: getFortnoxItems(facility)])
        else {
            cmd.rows.each { rowData ->
                Booking booking = Booking.get(rowData.bookingId)
                Order order = booking.order
                order.price = rowData.price
                order.vat = Price.calculateVATAmount(order.price.toLong(), new Double(facility.vat))

                InvoiceRow row = new InvoiceRow()
                row.customer = order.customer
                row.description = StringUtils.abbreviate(rowData.description, InvoiceRow.DESCRIPTION_MAX_SIZE)
                row.externalArticleId = cmd.articleId
                row.organization = Organization.get(facility.bookingInvoiceRowOrganizationId)
                row.amount = 1
                row.vat = facility.vat
                row.price = rowData.price
                row.createdBy = currentUser
                row.save(failOnError: true)

                InvoiceOrderPayment payment = new InvoiceOrderPayment()
                payment.issuer = currentUser
                payment.amount = rowData.price
                payment.vat = order.vat()
                payment.status = OrderPayment.Status.CAPTURED
                payment.invoiceRow = row

                payment.save(failOnError: true)

                order.addToPayments(payment)

                log.debug("Added payment: ${payment.id} to order: ${order.id}")

                if (order.isFinalPaid()) {
                    orderStatusService.complete(order, currentUser)
                } else {
                    orderStatusService.confirm(order, currentUser)
                }
                order.save(failOnError: true)
            }
            redirect(action: "report")
        }
    }

    private getFortnoxItems(Facility facility) {
        facility.bookingInvoiceRowOrganizationId ?
            invoiceService.getItemsForOrganization(facility.bookingInvoiceRowOrganizationId) :
            invoiceService.getItems(facility)
    }

    private DateTime dateParam(parameter, defaultValue) {
        if (parameter != null) {
            new DateMidnight(parameter).toDateTime()
        } else {
            defaultValue.toDateTime()
        }
    }

    def update(Long id, Long version) {
        def trainerInstance = Trainer.get(id)
        if (!trainerInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'trainer.label', default: 'Trainer')])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (trainerInstance.version > version) {
                trainerInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                    [message(code: 'trainer.label', default: 'Trainer')] as Object[],
                    "Another user has updated this Trainer while you were editing")
                render(view: "edit", model: [trainerInstance: trainerInstance, facility: getUserFacility()])
                return
            }

            // Validating validStart/validEnd to avoid only one of them set
            if (trainerInstance?.hasAvailability()) {
                int nrOfAvailabilities = (int) params.count { String key, value -> key.contains("weekDay_") }
                boolean validStartEndError = false
                if (nrOfAvailabilities > 0) {
                    (1..(nrOfAvailabilities + 20)).each { // Add 10 to iterations just to make sure we don't miss any
                        if (validStartEndError) return

                        if (params.get("startDate_" + it) || params.get("endDate_" + it)) {

                            String startDateString = params.get("startDate_" + it)
                            String endDateString = params.get("endDate_" + it)

                            if (!(startDateString && endDateString) && (startDateString || endDateString)) {
                                validStartEndError = true
                            }
                        }
                    }
                }

                if (validStartEndError) {
                    flash.error = message(code: 'trainer.availability.validDatesBothRequired')
                    render(view: "edit", model: [trainerInstance: trainerInstance, facility: getUserFacility()])
                    return
                }
            }
        }

        params.isActive = params.isActive ?: false
        params.isBookable = params.isBookable ?: false

        bindData(trainerInstance, params, ['facility', 'profileImage', 'fromHour_', 'toHour_', 'weekDay_'])

        if (trainerInstance?.hasAvailability()) {
            trainerInstance.availabilities.clear()

            int nrOfAvailabilities = (int) params.count { String key, value -> key.contains("weekDay_") }
            if (nrOfAvailabilities > 0) {
                (1..(nrOfAvailabilities + 20)).each { Integer index -> // Add 10 to iterations just to make sure we don't miss any
                    if (params.get("fromTime_" + index)) {
                        Availability a = Availability.buildFromParams(params, index)
                        trainerInstance.addToAvailabilities(a)
                    }
                }
            }
        }

        def profileImage = retrieveUploadedImage("profileImage")
        if (profileImage) {
            trainerInstance.profileImage = profileImage
        }
        if (!trainerInstance.save(flush: true)) {
            render(view: "edit", model: [trainerInstance: trainerInstance, facility: getUserFacility()])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'trainer.label', default: 'Trainer'), trainerInstance])
        redirect(action: "list")
    }

    def delete() {
        def trainerInstance = Trainer.get(params.id)
        if (!trainerInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'trainer.label', default: 'Trainer')])
            redirect(action: "list")
            return
        }

        try {
            trainerService.delete(trainerInstance)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'trainer.label', default: 'Trainer'), trainerInstance])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'trainer.label', default: 'Trainer'), trainerInstance])
            redirect(action: "edit", id: params.id)
        }
    }

    def deleteImage(Long id) {
        def textId = params.textId
        Trainer trainer = Trainer.get(params.id)

        log.info("Delete Trainer image (${textId}) on Trainer ${trainer.id}")
        def fileToRemove = trainer.profileImage
        trainer.profileImage = null
        fileArchiveService.removeFile(fileToRemove)
        redirect(url: params.returnUrl)
    }

    private def retrieveUploadedImage(String name) {
        MultipartHttpServletRequest mpr = (MultipartHttpServletRequest) request
        CommonsMultipartFile image = (CommonsMultipartFile) mpr.getFile(name)

        if (!image.isEmpty()) {
            return fileArchiveService.storeFile(image)
        }

        return null
    }
}

@Validateable(nullable = true)
class TrainerReportCommand {
    Long trainerId
    Boolean paid
}

@Validateable(nullable = true)
class LessonInvoiceRowsCommand implements Serializable {
    String articleId
    List<LessonInvoiceRowCommand> rows = [].withLazyDefault { new LessonInvoiceRowCommand() }

    void clearNullRows() {
        rows.removeAll { it == null }
    }

    def hasRowId(def rowId) {
        return rows.findIndexOf { it.rowId == rowId } > -1
    }

    static constraints = {
        articleId(nullable: false)
        rows(validator: { val, obj ->

            // remove null rows
            obj.clearNullRows()

            return val.every { it.validate() }
        }, minSize: 1)
    }
}

@Validateable(nullable = true)
class LessonInvoiceRowCommand implements Serializable {
    Long bookingId
    BigDecimal price
    String customerName
    String description

    static constraints = {
        bookingId(nullable: false)
        price(nullable: false, blank: false, validator: {
            return it > 0 && it <= Integer.MAX_VALUE
        })
    }
}
