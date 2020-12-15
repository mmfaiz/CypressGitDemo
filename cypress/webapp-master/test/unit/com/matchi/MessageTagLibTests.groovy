package com.matchi

import com.matchi.GlobalNotification
import com.matchi.i18n.Translatable
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

import static com.matchi.TestUtils.*

@TestFor(MessageTagLib)
@Mock([GlobalNotification, Translatable])
class MessageTagLibTests {

    Map render

    @Before
    void setUp() {
        tagLib.class.metaClass.render = { Map attrs ->
            render = attrs
        }
    }

    void testB3GlobalMessage() {
        def gn = createGlobalNotification()

        tagLib.b3GlobalMessage()

        assert render.template == "/templates/messages/bootstrap3/globalNotification"
        assert render.model.messages.size() == 1

        gn.isForUsers = true
        gn.save(failOnError: true)

        tagLib.b3GlobalMessage()

        assert render.model.messages.size() == 1

        gn.isForUsers = false
        gn.isForFacilityAdmins = true
        gn.save(failOnError: true)

        tagLib.b3GlobalMessage()

        assert !render.model.messages
    }

    void testFacilityAdminGlobalMessage() {
        def gn = createGlobalNotification()

        tagLib.facilityAdminGlobalMessage()

        assert render.template == "/templates/messages/globalNotification"
        assert render.model.messages.size() == 1

        gn.isForFacilityAdmins = true
        gn.save(failOnError: true)

        tagLib.facilityAdminGlobalMessage()

        assert render.model.messages.size() == 1

        gn.isForUsers = true
        gn.isForFacilityAdmins = false
        gn.save(failOnError: true)

        tagLib.facilityAdminGlobalMessage()

        assert !render.model.messages
    }
}
