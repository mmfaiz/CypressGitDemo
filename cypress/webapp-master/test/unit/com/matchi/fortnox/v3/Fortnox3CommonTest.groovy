package com.matchi.fortnox.v3

import com.matchi.Facility
import grails.util.Holders
import com.matchi.FacilityProperty

/**
 * @author Michael Astreiko
 */
abstract class Fortnox3CommonTest {
    Facility facility

    void setUp() {
        //AppConstants mocking
        def configObject = new ConfigObject()
        configObject.put('matchi', ['fortnox': ['api': ['v3':[
                'clientSecret': 'l18YDQl9i8',
                'clientId': 'Jmm1RJvCQJZN'
        ]]]])
        Holders.setConfig(configObject)
        facility = new Facility(name: "test").save(validate: false, failOnError: true)
        new FacilityProperty(facility: facility, key: FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN.name(),
                value: '91b9f399-27a1-4f7a-91be-c31556a2a2b7').save(failOnError: true)
    }
}
