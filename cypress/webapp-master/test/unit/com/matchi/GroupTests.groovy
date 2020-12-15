package com.matchi

import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Group)
class GroupTests {

    @Before
    public void setUp() {
        mockForConstraintsTests(Group)
    }
    void testValidGroup() {
        assert createValid(new Facility()).validate()
    }

    public static Group createValid(def facility) {
        def group = new Group()

        group.name = "Test #1"
        group.description = "Description"
        group.facility = facility

        return group;
    }
}
