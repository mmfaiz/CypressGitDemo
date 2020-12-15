package com.matchi.admin

import com.matchi.Customer
import com.matchi.PaymentInfo
import com.matchi.Role
import com.matchi.User
import com.matchi.coupon.CustomerCoupon
import com.matchi.devices.Device
import com.matchi.membership.Membership
import grails.validation.Validateable

class AdminUserController {

    def customerService
    def userService
    def notificationService
    def adyenService
    def paymentService

    def index(FilterUserCommand filter) {
        def users
        int totalCount
        if (filter.submit!=null) {
            users = userService.findUsers(filter)
            totalCount = users.getTotalCount()
        }

        [users: users, numUsers: totalCount, filter: filter]
    }

    def edit() {
        User user = User.get(params.id)
        List<Customer> customers = Customer.findAllByUserAndArchived(user, false).sort { Customer c -> c.facility.name }
        List<Membership> activeMemberships = customers*.getActiveMembership().findAll { it != null }
        List<CustomerCoupon> customerCoupons = customers*.customerCoupons.collect { Set<CustomerCoupon> customerCouponSet ->
            return customerCouponSet.toList().findAll { CustomerCoupon cc ->
                return !cc.isExpired() && !cc.dateLocked && (cc.nrOfTickets > 0 || cc.coupon.unlimited)
            }
        }.flatten().findAll { it != null }

        [user: user, canBeDeleted: user?.isHardDeletable(), customers: customers, activeMemberships: activeMemberships, customerCoupons: customerCoupons ]
    }

    def devices() {
        def user = User.get(params.id)
        [user: user, devices: Device.findAllByUser(user) ]
    }

    def paymentInfo() {
        def user = User.get(params.id)
        [user: user, paymentInfos: PaymentInfo.findAllByUser(user)]
    }

    def deleteUser() {
        User user = User.get(params.id)
        String message

        if(user.isHardDeletable()) {
            message = "User ${user.id} was deleted"
        } else {
            message = "User ${user.id} was soft deleted due to existing orders and payments"
        }

        userService.deleteUser(user)
        flash.message = message

        redirect(action: 'index')
    }

    def deletePaymentInfo() {
        def paymentInfo = PaymentInfo.findById(params.paymentInfoId)
        def user        = paymentInfo?.user
        paymentInfo?.delete()

        adyenService.deletePaymentInfo(user)

        redirect(action: 'paymentInfo', params: [id: user.id])
    }

    def activateDevice() {
        def user = User.get(params.id)
        def device = Device.get(params.deviceId)

        device.blocked = null

        flash.message = message(code: "adminUser.activateDevice.success")

        redirect(action: "devices", params: [id: params.id])
    }

    def blockDevice() {
        def user = User.get(params.id)
        def device = Device.get(params.deviceId)

        device.blocked = new Date()

        flash.message = message(code: "adminUser.blockDevice.success")

        redirect(action: "devices", params: [id: params.id])
    }

    def update(UpdateUserCommand cmd) {
        User user = User.get(cmd.id)

        if (cmd.hasErrors()) {
            render(view: "edit", model: [ user: user, cmd: cmd ])
            return
        }

        def emailChanged = user.email != cmd.email
        def roles = []
        if(cmd.roles) {
            roles = Role.findAllByIdInList(cmd.roles)
        }

        user.firstname = cmd.firstname
        user.lastname = cmd.lastname
        user.email = cmd.email

        if (!user.hasErrors() && user.save()) {
            userService.updateUserRoles(user, roles)
            if (emailChanged) {
                customerService.updateCustomersEmail(user)
            }
            flash.message = message(code: "adminUser.update.success", args: [user.email])
        } else {
            render(view: "edit", model: [user: user, cmd: cmd])
            return
        }

        redirect(action: "index")
    }

    def reactivate() {
        def userIds = params.list('userId')
        def resultMessage = ""

        userIds.each { String id ->
            def user = User.get(new Long(id))

            if (user && user.activationcode) {
                notificationService.sendActivationMail(user, true, params)
                resultMessage = message(code: "adminUser.reactivate.messageSent")
            } else if (resultMessage.equals("")) {
                resultMessage = message(code: "adminUser.reactivate.messageNotSent")
            }
        }

        flash.message = resultMessage
        redirect(action:"index")
    }

    def lock() {
        def user = User.get(params.id)

        user.accountLocked = true
        user.dateBlocked = new Date()
        user.save()

        flash.message = message(code: "adminUser.lock.success", args: [user.email])
        redirect(action:"index")
    }

    def unlock() {
        def user = User.get(params.id)

        user.accountLocked = false
        user.dateBlocked = null
        user.save()

        flash.message = message(code: "adminUser.unlock.success", args: [user.email])
        redirect(action:"index")
    }
}

@Validateable(nullable = true)
class UpdateUserCommand {
    Long id
    String email
    String firstname
    String lastname
    List<Long> facilities
    List<Long> roles

    static constraints = {
        firstname(blank:false)
        lastname(blank:false)
        email(blank:false, email: true, validator: {email, obj ->
            def user = User.createCriteria().get {
                and {
                    ne("id", obj.id)
                    eq("email", email)
                }
            }

            user ? ['invalid.emailnotunique'] : true
        })
    }
}

@Validateable(nullable = true)
class FilterUserCommand {

    String submit
    String q
    List<Long> roles = []

    int max = 50
    int offset = 0
}
