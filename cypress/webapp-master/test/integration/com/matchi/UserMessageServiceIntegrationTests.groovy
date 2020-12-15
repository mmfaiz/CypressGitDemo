package com.matchi

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class UserMessageServiceIntegrationTests extends GroovyTestCase {

    def userMessageService

    void testListConversations() {
        def user1 = createUser()
        def user2 = createUser("janedoe@local.net")
        def user3 = createUser("chrisdoe@local.net")
        createUserMessage(user1, user2)
        createUserMessage(user2, user1)
        createUserMessage(user3, user1)

        assert 2 == userMessageService.listConversations(user1).size()
        assert 1 == userMessageService.listConversations(user2).size()
        assert 1 == userMessageService.listConversations(user3).size()
    }

    void testListConversationMessages() {
        def user1 = createUser()
        def user2 = createUser("janedoe@local.net")
        def msg1 = createUserMessage(user1, user2)
        def msg2 = createUserMessage(user2, user1)

        def list = userMessageService.listConversationMessages(user1, user2)
        assert 2 == list.size()
        assert list.contains(msg1)
        assert list.contains(msg2)
        assert !UserMessage.findById(msg1.id).markedAsRead
        assert UserMessage.findById(msg2.id).markedAsRead

        list = userMessageService.listConversationMessages(user2, user1)
        assert 2 == list.size()
        assert UserMessage.findById(msg1.id).markedAsRead
    }

    void testListUnreadIncomingMessages() {
        def user1 = createUser()
        def user2 = createUser("janedoe@local.net")
        def msg1 = createUserMessage(user1, user2)
        def msg2 = createUserMessage(user2, user1)

        def list = userMessageService.listUnreadIncomingMessages(user1, user2)
        assert 1 == list.size()
        assert list.contains(msg2)
        assert UserMessage.findById(msg2.id).markedAsRead
        assert !UserMessage.findById(msg1.id).markedAsRead

        assert !userMessageService.listUnreadIncomingMessages(user1, user2)

        list = userMessageService.listUnreadIncomingMessages(user2, user1)
        assert 1 == list.size()
        assert list.contains(msg1)
        assert UserMessage.findById(msg1.id).markedAsRead
    }
}
