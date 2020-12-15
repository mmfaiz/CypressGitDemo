package com.matchi

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(UserProfileController)
class UpdateUserProfileCommandTest extends Specification  {

    @Unroll
    def "field `#field` with value #data should be valid=#valid" () {
        given:
        UpdateUserProfileCommand cmd = new UpdateUserProfileCommand()
        cmd.setProperty(field, data)

        expect:
        assert valid == cmd.validate([field])

        where:
        data              | field       || valid
        2030              | "birthYear" || false
        2020              | "birthYear" || true
        1930              | "birthYear" || true
        1929              | "birthYear" || false
        null              | "email"     || false
        ""                | "email"     || false
        "user@email.com"  | "email"     || true
        "äny@email.com"   | "email"     || false
    }
}
