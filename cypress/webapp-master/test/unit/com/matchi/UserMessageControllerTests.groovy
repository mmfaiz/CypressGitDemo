package com.matchi

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.grails.plugins.sanitizer.MarkupSanitizerService
import org.springframework.core.io.ClassPathResource

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(UserMessageController)
@Mock([User, UserMessage])
class UserMessageControllerTests {

    void testIndex() {
        def currentUser = createUser()
        def targetUser = createUser("janedoe@local.net")
        createUserMessage(currentUser, targetUser)
        def springSecurityServiceControl = mockSpringSecurityService(currentUser)

        controller.index()

        assert "/profile/messages/conversation/$targetUser.id" == response.redirectedUrl
        springSecurityServiceControl.verify()
    }

    void testConversation() {
        def currentUser = createUser()
        def targetUser = createUser("janedoe@local.net")
        def userServiceControl = mockUserService(targetUser)
        def springSecurityServiceControl = mockSpringSecurityService(currentUser)
        def userMessageServiceControl = mockFor(UserMessageService)
        userMessageServiceControl.demand.listConversationMessages { u1, u2 -> [] }
        userMessageServiceControl.demand.listConversations { u -> [] }
        controller.userMessageService = userMessageServiceControl.createMock()

        def model = controller.conversation(targetUser.id)

        assert model
        assert currentUser == model.currentUser
        assert targetUser == model.targetUser
        assert model.containsKey("messages")
        assert model.containsKey("conversations")
        userServiceControl.verify()
        springSecurityServiceControl.verify()
        userMessageServiceControl.verify()
    }

    void testSendMessage() {
        messageSource.addMessage("date.format.dateOnly", new Locale("en"), "yyyy-MM-dd")
        def currentUser = createUser()
        def targetUser = createUser("janedoe@local.net")
        def msg = createUserMessage(currentUser, targetUser)
        msg.dateCreated = new Date()
        msg.save(failOnError: true)
        def userServiceControl = mockFor(UserService)
        userServiceControl.demand.getUser { id -> targetUser }
        userServiceControl.demand.canSendDirectMessage { from, to -> true }
        controller.userService = userServiceControl.createMock()
        def springSecurityServiceControl = mockSpringSecurityService(currentUser)
        def userMessageServiceControl = mockFor(UserMessageService)
        userMessageServiceControl.demand.send { u1, u2, m -> msg }
        controller.userMessageService = userMessageServiceControl.createMock()
        controller.markupSanitizerService = new MarkupSanitizerService(new ClassPathResource("antisamyconfigs/antisamy-slashdot-1.4.4.xml"))

        controller.sendMessage(targetUser.id, msg.message)

        assert msg.message == response.json.message
        assert msg.dateCreated.format("yyyy-MM-dd") == response.json.date
        userServiceControl.verify()
        springSecurityServiceControl.verify()
        userMessageServiceControl.verify()
    }

    void testSendMessageError() {
        messageSource.addMessage("date.format.dateOnly", new Locale("en"), "yyyy-MM-dd")
        def currentUser = createUser()
        def targetUser = createUser("janedoe@local.net")
        def msg = createUserMessage(currentUser, targetUser)
        msg.dateCreated = new Date()
        msg.save(failOnError: true)
        def userServiceControl = mockFor(UserService)
        userServiceControl.demand.getUser { id -> targetUser }
        userServiceControl.demand.canSendDirectMessage { from, to -> false }
        controller.userService = userServiceControl.createMock()
        def springSecurityServiceControl = mockSpringSecurityService(currentUser)

        controller.sendMessage(targetUser.id, msg.message)

        assert response.status == 400
        userServiceControl.verify()
        springSecurityServiceControl.verify()
    }

    void testListUnreadMessages() {
        messageSource.addMessage("date.format.dateOnly", new Locale("en"), "yyyy-MM-dd")
        def currentUser = createUser()
        def targetUser = createUser("janedoe@local.net")
        def msg = createUserMessage(targetUser, currentUser)
        msg.dateCreated = new Date()
        msg.save(failOnError: true)
        def userServiceControl = mockUserService(targetUser)
        def springSecurityServiceControl = mockSpringSecurityService(currentUser)
        def userMessageServiceControl = mockFor(UserMessageService)
        userMessageServiceControl.demand.listUnreadIncomingMessages { u1, u2 -> [msg] }
        controller.userMessageService = userMessageServiceControl.createMock()

        controller.listUnreadMessages(targetUser.id)

        def responseMsg = response.json[0]
        assert targetUser.id == responseMsg.from
        assert currentUser.id == responseMsg.to
        assert msg.message == responseMsg.message
        assert msg.dateCreated.format("yyyy-MM-dd") == responseMsg.date
        userServiceControl.verify()
        springSecurityServiceControl.verify()
        userMessageServiceControl.verify()
    }

    void testCountUnreadMessages() {
        def currentUser = createUser()
        def targetUser = createUser("janedoe@local.net")
        createUserMessage(targetUser, currentUser)
        def springSecurityServiceControl = mockSpringSecurityService(currentUser)

        controller.countUnreadMessages()

        assert 1 == response.json.count
        springSecurityServiceControl.verify()
    }

    private mockSpringSecurityService(user) {
        def springSecurityServiceControl = mockFor(SpringSecurityService)
        springSecurityServiceControl.demand.getCurrentUser { -> user }
        controller.springSecurityService = springSecurityServiceControl.createMock()
        springSecurityServiceControl
    }

    private mockUserService(user) {
        def userServiceControl = mockFor(UserService)
        userServiceControl.demand.getUser { id -> user }
        controller.userService = userServiceControl.createMock()
        userServiceControl
    }
}
