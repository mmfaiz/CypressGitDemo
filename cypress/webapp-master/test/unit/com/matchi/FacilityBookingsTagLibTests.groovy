package com.matchi

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(FacilityBookingsTagLib)
@Mock(FacilityBookingsTagLib)
class FacilityBookingsTagLibTests extends Specification {

    void testBookingPlayers() {
        messageSource.addMessage("player.unknown.amount", new Locale("en"), "{0} guest")

        expect:
        applyTemplate('<g:bookingPlayers/>') == ''
        applyTemplate('<g:bookingPlayers players="${players}"/>',
                [players: [new Player()]]) == '1 guest'
        applyTemplate('<g:bookingPlayers players="${players}"/>',
                [players: [new Player(customer: new Customer(firstname: "John", lastname: "Doe"))]]) == 'John Doe'
        applyTemplate('<g:bookingPlayers players="${players}"/>',
                [players: [new Player(email: "someone@matchi.se")]]) == 'someone@matchi.se'
        applyTemplate('<g:bookingPlayers players="${players}"/>',
                [players: [new Player(), new Player(email: "someone@matchi.se"),
                        new Player(customer: new Customer(firstname: "John", lastname: "Doe")), ]]) == 'someone@matchi.se, John Doe, 1 guest'
        applyTemplate('<g:bookingPlayers players="${players}" var="p"><div>${p}</div></g:bookingPlayers>',
                [players: [new Player()]]) == '<div>1 guest</div>'
    }
}
