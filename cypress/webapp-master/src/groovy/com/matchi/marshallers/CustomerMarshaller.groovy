package com.matchi.marshallers

import com.matchi.Customer
import com.matchi.StringHelper
import grails.converters.JSON

import javax.annotation.PostConstruct

class CustomerMarshaller {

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Customer) { Customer customer ->
            marshallCustomer(customer)
        }
    }

    def marshallCustomer(Customer customer) {
        [
                id: customer.id,
                externalId: null, // TODO: we still need to figure out whether this property is necessary or not for Visma or XLeadger
                number: customer.number,
                name: customer.fullName(),
                type: customer.isCompany() ? "COMPANY" : "PRIVATE",
                email: customer.email,
                address1: customer.invoiceAddress1 ?: customer.address1,
                address2: customer.invoiceAddress2 ?: customer.address2,
                city: customer.invoiceCity ?: customer.city,
                zipcode: customer.invoiceZipcode ?: customer.zipcode,
                countryCode: customer.country,
                comments: customer.notes,
                orginsationNumber: customer.isCompany() ? customer.orgNumber : customer.getPersonalNumber(),
                phone1: customer.invoiceTelephone ?: customer.telephone,
                phone2: customer.cellphone,
                reference: customer.invoiceContact ?: customer.contact,
                emailInvoice: customer.getCustomerInvoiceEmail(),
                deliveryName: customer.invoiceContact,
                deliveryAddress1: customer.invoiceAddress1,
                deliveryAddress2: customer.invoiceAddress2,
                deliveryZipCode: StringHelper.safeSubstring(customer.invoiceZipcode, 0, 10),
                deliveryCity: customer.invoiceCity,
                deliveryCountryCode: customer.country
        ]
    }
}

