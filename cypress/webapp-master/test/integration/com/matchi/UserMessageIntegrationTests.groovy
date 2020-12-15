package com.matchi

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class UserMessageIntegrationTests extends GroovyTestCase {

    void testConversationMessagesQuery() {
        def user1 = createUser()
        def user2 = createUser("janedoe@local.net")
        def user3 = createUser("chrisdoe@local.net")
        createUserMessage(user1, user2)
        createUserMessage(user2, user1)
        createUserMessage(user3, user1)
        createUserMessage(user3, user2)

        assert 3 == UserMessage.conversationMessages(user1).count()
        assert 2 == UserMessage.conversationMessages(user1, user2).count()
        assert 1 == UserMessage.conversationMessages(user1, user3).count()
        assert 3 == UserMessage.conversationMessages(user2).count()
        assert 2 == UserMessage.conversationMessages(user2, user1).count()
        assert 1 == UserMessage.conversationMessages(user2, user3).count()
        assert 2 == UserMessage.conversationMessages(user3).count()
        assert 1 == UserMessage.conversationMessages(user3, user1).count()
        assert 1 == UserMessage.conversationMessages(user3, user2).count()
    }

    void testUnreadIncomingMessagesQuery() {
        def user1 = createUser()
        def user2 = createUser("janedoe@local.net")
        def user3 = createUser("chrisdoe@local.net")
        createUserMessage(user1, user2)
        createUserMessage(user2, user1, true)
        createUserMessage(user3, user2)
        createUserMessage(user3, user2, true)

        assert 2 == UserMessage.unreadIncomingMessages(user2).count()
        assert 1 == UserMessage.unreadIncomingMessages(user2, user1).count()
        assert 1 == UserMessage.unreadIncomingMessages(user2, user3).count()
        assert !UserMessage.unreadIncomingMessages(user1).count()
        assert !UserMessage.unreadIncomingMessages(user3).count()
    }
}
