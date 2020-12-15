package com.matchi.integration.events

class Customer {
    final String id
    final Long number
    final Facility facility
    final String email
    final String firstname
    final String lastname
    final String companyname
    final String city
    final String country
    final String nationality
    final String telephone
    final String cellphone
    final String type

    Customer(com.matchi.Customer customer) {
        this.id = customer.id
        this.number = customer.number
        this.facility = new Facility(customer.facility)
        this.email = customer.email
        this.firstname = customer.firstname
        this.lastname = customer.lastname
        this.companyname = customer.companyname
        this.city = customer.city
        this.country = customer.country
        this.nationality = customer.nationality
        this.telephone = customer.telephone
        this.cellphone = customer.cellphone
        this.type = customer.type.toString()
    }
}
