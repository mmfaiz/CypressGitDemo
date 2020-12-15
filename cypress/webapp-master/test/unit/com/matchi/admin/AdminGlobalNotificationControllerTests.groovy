package com.matchi.admin

import com.matchi.GlobalNotification
import com.matchi.i18n.Translatable
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(AdminGlobalNotificationController)
@Mock([GlobalNotification, Translatable])
class AdminGlobalNotificationControllerTests {

    void testIndex() {
        def gn = createGlobalNotification()

        def model = controller.index()

        assert 1 == model.globalNotificationInstanceList.size()
        assert gn == model.globalNotificationInstanceList[0]
        assert 1 == model.globalNotificationInstanceTotal
    }

    void testCreate() {
        def model = controller.create()
        assert model.containsKey("globalNotificationInstance")
    }

    void testSaveWithoutForm() {
        request.method = "POST"
        params.title = "title"
        params.notificationText = [translations: [en: "test"]]
        params.publishDate = "2015-02-01"
        params.endDate = "2015-02-28"
        params.isForUsers = false
        params.isForFacilityAdmins = true
        controller.save()

        assert "/admin/notifications/index" == response.redirectedUrl
        assert 1 == GlobalNotification.count()
        def gn = GlobalNotification.first()
        assert gn.title == "title"
        assert gn.notificationText.translations["en"] == "test"
        assert gn.publishDate.format("yyyy-MM-dd") == "2015-02-01"
        assert gn.endDate.format("yyyy-MM-dd") == "2015-02-28"
        assert !gn.isForUsers
        assert gn.isForFacilityAdmins
    }

    void testEdit() {
        def gn = createGlobalNotification()
        params.id = gn.id

        def model = controller.edit()

        assert gn == model.globalNotificationInstance
    }

    void testUpdate() {
        request.method = "POST"
        def gn = createGlobalNotification()
        def newText = "new ${gn.notificationText.translations['en']}"
        params.notificationText = [translations: [en: newText]]

        controller.update(gn.id, gn.version)

        assert "/admin/notifications/index" == response.redirectedUrl
        assert 1 == GlobalNotification.count()
        assert newText == GlobalNotification.first().notificationText.translations["en"]
    }

    void testDelete() {
        request.method = "POST"
        params.id = createGlobalNotification().id

        controller.delete()

        assert "/admin/notifications/index" == response.redirectedUrl
        assert !GlobalNotification.count()
    }
}
