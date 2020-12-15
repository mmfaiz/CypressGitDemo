package com.matchi

import com.matchi.membership.Membership
import grails.transaction.NotTransactional

class CustomerMergeService {
    static transactional = true

    def groupService

    def mergeCustomers(Customer primary, List<Customer> merges) {
        merges.each { Customer toMerge ->
            toMerge.customerGroups
            merge(primary, toMerge)
        }

        return primary
    }

    def merge(Customer primary, Customer secondary) {
        _copyCustomerInfo(primary, primary, secondary)

        if (!secondary.isAttached()) {
            secondary.attach()
        }
        def groups = []
        groups.addAll secondary.customerGroups

        groups.each { CustomerGroup cg ->
            groupService.removeCustomerFromGroup(cg.group, secondary)
        }
        secondary.user = null
        secondary.archived = true
        secondary.unlinkMembershipFamily()
        Membership.unlink(secondary)

        primary.save()
        secondary.save()

        return primary
    }

    @NotTransactional
    def mergeCustomersResult(Customer primary, List<Customer> merges) {

        def result = new Customer()

        merges.each {
            _copyCustomerInfo(result, primary, it)
        }

        return result
    }

    @NotTransactional
    def _copyCustomerInfo(Customer result, Customer primary, Customer secondary) {

        result.number           = primary.number
        result.email            = primary.email ?: secondary.email
        result.type             = primary.type ?: secondary.type
        result.firstname        = primary.firstname ?: secondary.firstname
        result.lastname         = primary.lastname ?: secondary.firstname
        result.companyname      = primary.companyname ?: secondary.companyname
        result.contact          = primary.contact ?: secondary.contact
        result.address1         = primary.address1 ?: secondary.address1
        result.address2         = primary.address2 ?: secondary.address2
        result.zipcode          = primary.zipcode ?: secondary.zipcode
        result.city             = primary.city ?: secondary.city
        result.telephone        = primary.telephone ?: secondary.telephone
        result.cellphone        = primary.cellphone ?: secondary.cellphone
        result.notes            = primary.notes ?: secondary.notes
        result.dateOfBirth      = primary.dateOfBirth ?: secondary.dateOfBirth
        result.securityNumber   = primary.securityNumber ?: secondary.securityNumber
        result.orgNumber        = primary.orgNumber ?: secondary.orgNumber
        result.invoiceAddress1  = primary.invoiceAddress1 ?: secondary.invoiceAddress1
        result.invoiceAddress2  = primary.invoiceAddress2 ?: secondary.invoiceAddress2
        result.invoiceTelephone = primary.invoiceTelephone ?: secondary.invoiceTelephone
        result.invoiceEmail     = primary.invoiceEmail ?: secondary.invoiceEmail
        result.invoiceContact   = primary.invoiceContact ?: secondary.invoiceContact
        result.invoiceZipcode   = primary.invoiceZipcode ?: secondary.invoiceZipcode
        result.invoiceCity      = primary.invoiceCity ?: secondary.invoiceCity
        result.web              = primary.web ?: secondary.web

        result.user             = primary.user ?: secondary.user
    }
}
