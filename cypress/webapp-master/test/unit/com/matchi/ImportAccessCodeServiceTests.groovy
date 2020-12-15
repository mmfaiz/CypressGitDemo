package com.matchi

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(ImportAccessCodeService)
@Mock([Facility, Court, FacilityAccessCode, Municipality, Region, Sport])
class ImportAccessCodeServiceTests {

    void testParseAccessCodeData() {
        def data = [
                [validFrom: "2015-01-01 06:00:00", validTo: "2015-01-01 23:00:00", content: "12345"],
                [validFrom: "2015-01-02 06:00:00", validTo: "2015-01-02 23:00:00"],
                [validFrom: "2015-01-01 06:00:00", validTo: "2015/01/01", content: "12345"],
                [validFrom: "2015/01/01 06:00:00", validTo: "2015-01-01 23:00:00", content: "12345"]
        ]

        def result = service.parseAccessCodeData(data)

        assert 4 == result.size()
        assert result[0].cmd
        assert !result[0].error
        assert data[0].validFrom == result[0].cmd.validFrom
        assert data[0].validTo == result[0].cmd.validTo
        assert data[0].content == result[0].cmd.content
        assert result[1].cmd
        assert result[1].error
        assert data[1].validFrom == result[1].cmd.validFrom
        assert data[1].validTo == result[1].cmd.validTo
        assert !result[1].cmd.content
        assert result[2].error
        assert result[3].error
    }

    void testImportAccessCodes() {
        def facility = createFacility()
        def court = createCourt(facility)
        def springSecurityServiceControl = mockFor(SpringSecurityService)
        springSecurityServiceControl.demand.getCurrentUser { -> new User(facility: facility) }
        service.springSecurityService = springSecurityServiceControl.createMock()
        def data = [
                [cmd: [validFrom: "2015-01-01 06:00:00", validTo: "2015-01-01 23:00:00", content: "12345", courts: "${court.name}"]],
                [cmd: [validFrom: "2015-01-02 06:00:00", validTo: "2015-01-02 23:00:00", courts: ""]]
        ]

        def result = service.importAccessCodes(data)

        assert 1 == result.imported.size()
        assert 1 == result.failed.size()
        assert 1 == FacilityAccessCode.count()
        def fm = FacilityAccessCode.first()
        assert fm.active
        assert facility == fm.facility
        assert "12345" == fm.content
        assert FacilityAccessCode.first().courts?.size() == 1
        assert FacilityAccessCode.first().courts[0]?.id == court.id
        springSecurityServiceControl.verify()
    }
}
