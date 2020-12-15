package com.matchi.activities

import org.joda.time.LocalDate

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

@TestMixin(GrailsUnitTestMixin)
class ClassActivityTests {

    @Test
    void testGetUpcomingOnlineOccasions() {
        ClassActivity activity = new ClassActivity()

        activity.occasions = [
                new ActivityOccasion(availableOnline: true, date: new LocalDate().plusDays(5)),
                new ActivityOccasion(availableOnline: true, date: new LocalDate().plusDays(5), signUpDaysUntilRestriction: 5, signUpDaysInAdvanceRestriction: 5),
                new ActivityOccasion(availableOnline: true, date: new LocalDate().plusDays(5), signUpDaysUntilRestriction: 2, signUpDaysInAdvanceRestriction: 10),
                new ActivityOccasion(availableOnline: true, date: new LocalDate().plusDays(5), signUpDaysInAdvanceRestriction: 10),
                new ActivityOccasion(availableOnline: true, date: new LocalDate().plusDays(5), signUpDaysUntilRestriction: 2),

                // The following should not be included
                new ActivityOccasion(availableOnline: true, date: new LocalDate().plusDays(5), signUpDaysInAdvanceRestriction: 4),
                new ActivityOccasion(availableOnline: true, date: new LocalDate().plusDays(5), signUpDaysUntilRestriction: 6)
        ]

        assert activity.getUpcomingOnlineOccasions().size() == 5
    }
}
