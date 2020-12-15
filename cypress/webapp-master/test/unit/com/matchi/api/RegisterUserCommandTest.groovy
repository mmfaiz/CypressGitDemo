package com.matchi.api

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(UserResourceController)
class RegisterUserCommandTest extends Specification {

    @Unroll
    def "field `#field` with value #data should be valid=#valid"() {
        given:
        RegisterUserCommand cmd = new RegisterUserCommand()
        cmd.setProperty(field, data)

        expect:
        assert valid == cmd.validate([field])

        where:
        data             | field      || valid
        null             | "email"    || false
        ""               | "email"    || false
        "user@email.com" | "email"    || true
        "äny@email.com"  | "email"    || false
        "StrongestPwd"   | "password" || true
        "pwd"            | "password" || false
        ""               | "password" || false
        null             | "password" || false
    }
}