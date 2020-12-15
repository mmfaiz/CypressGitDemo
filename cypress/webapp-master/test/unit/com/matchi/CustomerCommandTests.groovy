package com.matchi

import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Before
import org.junit.Test

/**
 *
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([Facility, Group, Customer])
class CustomerCommandTests {

    Facility facility

    @Before
    public void setUp() {
        facility = new Facility(id: 1, country: "SE").save(flush: true, validate: false)
        defineBeans {
            dateUtil(DateUtil) {
                grailsApplication = [
                        config: [
                                customer: [
                                        personalNumber: [
                                                settings: [
                                                        SE: [
                                                                securityNumberLength: 4,
                                                                orgPattern: /^(\d{6}|\d{8})(?:-(\d{4}))?$/,
                                                                orgFormat: "??XXXXXX-XXXX",
                                                                longFormat: "yyyyMMdd",
                                                                shortFormat: "yyMMdd",
                                                                readableFormat: "yymmdd"
                                                        ]
                                                ]
                                        ]
                                ]
                        ]
                ]
            }
        }
    }

    @Test
    void testCreateCustomerCommand() {
        CreateCustomerCommand validCommand = createValidCommand()
        assert validCommand.validate() == true

        CreateCustomerCommand invalidCommand1 = createValidCommand()
        invalidCommand1.securityNumber = 1816
        assert invalidCommand1.validate() == false

        CreateCustomerCommand invalidCommand2 = createValidCommand()
        invalidCommand2.personalNumber = 920231
        assert invalidCommand2.validate() == false

        CreateCustomerCommand invalidCommand3 = createValidCommand()
        invalidCommand3.personalNumber = null
        invalidCommand3.securityNumber = null
        invalidCommand3.validate()
        println(invalidCommand3.errors)
        assert invalidCommand3.validate() == true

        CreateCustomerCommand invalidCommand4 = createValidCommand()
        invalidCommand4.securityNumber = null
        assert invalidCommand4.validate() == true

        CreateCustomerCommand invalidCommand5 = createValidCommand()
        invalidCommand5.personalNumber = null
        invalidCommand5.type = Customer.CustomerType.ORGANIZATION
        invalidCommand5.companyname = "Jag gillar Båtar"
        invalidCommand5.orgNumber = "556871-6129"
        assert invalidCommand5.validate() == true
    }

    CreateCustomerCommand createValidCommand() {

        def securityServiceControl = mockFor(SecurityService)

        securityServiceControl.demand.getUserFacility(0..9) { ->
            return facility
        }

        new CreateCustomerCommand(
                number: 1,
                firstname: "John",
                lastname: "Doe",
                email: "test@matchi.se",
                personalNumber: "890627",
                securityNumber: "1815",
                facilityId: facility.id
        )

    }
}
