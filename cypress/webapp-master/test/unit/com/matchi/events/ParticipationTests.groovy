package com.matchi.events

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.TestUtils
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import com.matchi.activities.Participation
import com.matchi.payment.ArticleType
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Participation)
@Mock([ Region, Municipality, Facility, Customer, ClassActivity, ActivityOccasion ])
class ParticipationTests {

    @Test
    void testIReservationMethods() {
        Participation participation = TestUtils.createActivityOccasionParticipation()
        assert participation.getArticleType() == ArticleType.ACTIVITY
        assert participation.getDate() == participation.occasion.getStartDateTime().toDate()

        participation.occasion.activity.delete(flush: true)
        participation.occasion.delete(flush: true)
        participation.occasion = null

        assert !participation.getDate()

        participation.customer.facility.delete(flush: true)
        participation.customer.delete(flush: true)
        participation.delete(flush: true)
    }
}
