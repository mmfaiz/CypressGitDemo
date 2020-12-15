package com.matchi

import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(UserMessageService)
@Mock([User, UserMessage])
class UserMessageServiceTests {

    void testSend() {
        def user1 = createUser()
        def user2 = createUser("janedoe@local.net")
        def notificationServiceControl = mockFor(NotificationService)
        notificationServiceControl.demand.sendUserMessage { u1, u2, m -> }
        service.notificationService = notificationServiceControl.createMock()

        def msg = service.send(user1, user2, "message")

        assert msg
        assert user1 == msg.from
        assert user2 == msg.to
        assert "message" == msg.message
        assert !msg.markedAsRead
        assert UserMessage.count()
        notificationServiceControl.verify()
    }
}
