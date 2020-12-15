package com.matchi.integration.events

import com.google.common.collect.Lists
import com.matchi.User
import spock.lang.Specification

class EventInitiatorSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "user is provided returns userid"() {
        given:
        def user = new User()
        user.id = 1L
        def from = InitiatorProvider.from(user)

        expect:
        from.id == "1"
        from.type == "user"
    }

    void "user is null and system service initiated returns system name"() {
        given:
        def elements = Lists.newArrayList(Thread.currentThread().getStackTrace())
        elements.add(
                new StackTraceElement('com.matchi.api.NotMapped', "method", "NotMapped.groovy", 59)
        )
        def from = InitiatorProvider.fromStack(elements)

        expect:
        from.id == "unknown"
        from.type == "user"
    }
    void "user is null and no system service initiated returns unknown"() {
        given:
        def elements = Lists.newArrayList(Thread.currentThread().getStackTrace())
        elements.add(
                new StackTraceElement('com.matchi.api.BackhandSmashController', "cancelBooking", "BackhandSmashController.groovy", 59)
        )
        def from = InitiatorProvider.fromStack(elements)

        expect:
        from.id == "BackhandSmashController"
        from.type == "system"
    }

    void "user is null and no job or system service initiated returns unknown"() {
        given:
        def from = InitiatorProvider.from((User) null)

        expect:
        from.id == "unknown"
        from.type == "user"
    }

    void "user is not provided and job initiated returns job name"() {
        def elements = Lists.newArrayList(Thread.currentThread().getStackTrace())
        elements.add(
                new StackTraceElement('com.matchi.jobs.ActivityCancellationByTooFewParticipantsJob$_execute_closure1', "", "ActivityCancellationByTooFewParticipantsJob.groovy", 1)
        )
        given:
        def from = InitiatorProvider.fromStack(elements)

        expect:
        from.id == "ActivityCancellationByTooFewParticipantsJob"
        from.type == "system"
    }

    void "stacktrace filtering"() {
        def elements = Lists.newArrayList(Thread.currentThread().getStackTrace())
        elements.add(
                new StackTraceElement('com.matchi.jobs.ActivityCancellationByTooFewParticipantsJob$_execute_closure1', "", "ActivityCancellationByTooFewParticipantsJob.groovy", 1)
        )
        given:
        def filtered = InitiatorProvider.filteredReversedStacktrace(elements)

        expect:
        filtered.size() == 2
        filtered[0].getClassName() == 'com.matchi.jobs.ActivityCancellationByTooFewParticipantsJob$_execute_closure1'
        filtered[1].getClassName() == EventInitiatorSpec.class.getName()
    }

}

