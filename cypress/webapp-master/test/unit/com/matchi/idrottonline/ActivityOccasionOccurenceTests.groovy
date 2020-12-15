package com.matchi.idrottonline

import com.matchi.activities.ActivityOccasion
import com.matchi.idrottonline.commands.*
import grails.test.MockUtils
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import net.sf.cglib.core.Local
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

@TestMixin(GrailsUnitTestMixin)
class ActivityOccasionOccurenceTests {

    @Before
    void setUp() { }

    @After
    void tearDown() { }

    @Test
    void testUniqueIdentifierCalculatesCorrectFormat(){
        // Arrange
        int idOfActivityOccasion = 1
        LocalDate tuesday = new LocalDate(2017, 10, 31)
        LocalDate mondayOfOccurenceWeek = new LocalDate(2017, 10, 30)
        ActivityOccasion occasion = new ActivityOccasion()
        occasion.id = idOfActivityOccasion
        ActivityOccasionOccurence activityOccasionOccurence = new ActivityOccasionOccurence(tuesday, occasion)

        // Act
        String uniqueIdentifier = activityOccasionOccurence.getUniqueIdentifier()

        // Assert
        assert uniqueIdentifier == String.format("${mondayOfOccurenceWeek.toString("YYYY-MM-dd")}-${idOfActivityOccasion}")
    }

}
