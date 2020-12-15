package com.matchi.dynamicforms

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(Submission)
class SubmissionSpec {

    @Test
    void testIsProcessed() {
        domain.status = Submission.Status.ACCEPTED
        domain.isProcessed()

        domain.status = Submission.Status.DISCARDED
        domain.isProcessed()

        domain.status = Submission.Status.WAITING
        !domain.isProcessed()

        // For example, if EventActivity
        domain.status = null
        !domain.isProcessed()
    }
}
