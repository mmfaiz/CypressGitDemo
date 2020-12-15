package com.matchi.mpc

import com.matchi.Booking
import com.matchi.Slot
import grails.test.mixin.*
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(CodeRequest)
class CodeRequestSpec {

    void testUnverifiedCodeRequestTooFarAway() {
        Slot slot = new Slot(startTime: new DateTime(new LocalDateTime().toDate()).plusDays(3).plusHours(1).toDate())
        Booking booking = new Booking(slot: slot)
        CodeRequest cr = new CodeRequest(booking: booking)
        cr.dateCreated = new DateTime().minusMinutes(45).toDate()

        assert !cr.hasProblems()
    }

    void testUnverifiedCodeRequest() {
        Slot slot = new Slot(startTime: new DateTime(new LocalDateTime().toDate()).plusDays(2).toDate())
        Booking booking = new Booking(slot: slot)
        CodeRequest cr = new CodeRequest(booking: booking)
        cr.dateCreated = new DateTime().minusMinutes(45).toDate()

        assert cr.hasProblems()

        // Not problem if pending!
        cr.status = CodeRequest.Status.PENDING
        assert !cr.hasProblems()
    }

    void testUnverifiedCodeRequestTooFresh() {
        Slot slot = new Slot(startTime: new DateTime(new LocalDateTime().toDate()).plusDays(2).toDate())
        Booking booking = new Booking(slot: slot)
        CodeRequest cr = new CodeRequest(booking: booking)
        cr.dateCreated = new DateTime().minusMinutes(15).toDate()

        assert !cr.hasProblems()
    }

    void testVerifiedCodeRequest() {
        Slot slot = new Slot(startTime: new DateTime(new LocalDateTime().toDate()).plusDays(2).toDate())
        Booking booking = new Booking(slot: slot)
        CodeRequest cr = new CodeRequest(booking: booking, status: CodeRequest.Status.VERIFIED)
        cr.dateCreated = new DateTime().minusMinutes(45).toDate()

        assert !cr.hasProblems()
    }

    void testCreateCodeRequest() {
        CodeRequest cr = new CodeRequest()

        assert cr.mpcId == CodeRequest.DEFAULT_MPC_ID
        assert cr.status == CodeRequest.Status.UNVERIFIED
        assert cr.code == CodeRequest.DEFAULT_CODE
    }
}