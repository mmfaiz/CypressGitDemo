package com.matchi


class ContactMe {

    String name
    String email
    String type
    String facility
    String phone
    boolean contacted

    Date dateCreated
    Date lastUpdated

    static constraints = {
        name(nullable: true)
        email(nullable: true, email: true)
        type(nullable: true)
        facility(nullable: false)
        phone(nullable: true)
    }

    static mapping = {
        autoTimestamp true
    }

}
