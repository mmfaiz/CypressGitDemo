package com.matchi

import com.matchi.subscriptionredeem.SlotRedeem
import org.joda.time.DateMidnight

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class RedeemServiceIntegrationTests extends GroovyTestCase {

    def redeemService
    def springSecurityService

    void testGetSlotRedeems() {
        def facility1 = createFacility()
        def customer11 = createCustomer(facility1, "cust11@local.net")
        def slot11 = createSlot(createCourt(facility1))
        def subscription11 = createSubscription(customer11)
        slot11.subscription = subscription11
        slot11.save(failOnError: true, flush: true)
        def redeem11 = new SlotRedeem(slot: slot11).save(failOnError: true, flush: true)
        def customer12 = createCustomer(facility1, "cust12@local.net")
        def slot12 = createSlot(createCourt(facility1))
        def subscription12 = createSubscription(customer12)
        slot12.subscription = subscription12
        slot12.save(failOnError: true, flush: true)
        def redeem12 = new SlotRedeem(slot: slot12).save(failOnError: true, flush: true)

        def facility2 = createFacility()
        def customer21 = createCustomer(facility2, "cust21@local.net")
        def slot21 = createSlot(createCourt(facility2))
        def subscription21 = createSubscription(customer21)
        slot21.subscription = subscription21
        slot21.save(failOnError: true, flush: true)
        def redeem21 = new SlotRedeem(slot: slot21).save(failOnError: true, flush: true)

        reauthenticate("u1@local.net", facility1)

        def result = redeemService.getSlotRedeems(getFilter())
        assert 2 == result.size()
        assert result.find {it.id == redeem11.id}
        assert result.find {it.id == redeem12.id}

        result = redeemService.getSlotRedeems(getFilter(customer11.email))
        assert 1 == result.size()
        assert result[0].id == redeem11.id

        reauthenticate("u2@local.net", facility2)

        result = redeemService.getSlotRedeems(getFilter())
        assert 1 == result.size()
        assert result[0].id == redeem21.id
    }

    private void reauthenticate(email, facility) {
        def user = createUser(email)
        user.facility = facility
        user.save()
        springSecurityService.reauthenticate user.email
    }

    private getFilter(q = null) {
        [start: new DateMidnight().minusMonths(1).withDayOfMonth(1).toLocalDate(),
                end: new DateMidnight().plusMonths(1).withDayOfMonth(1).minusDays(1).toLocalDate(),
                sort: "dateCreated", order: "desc", q: q]
    }
}