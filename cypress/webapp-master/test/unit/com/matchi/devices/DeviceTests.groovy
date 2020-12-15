package com.matchi.devices



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Device)
@Mock([Device, Token])
class DeviceTests {

    void testCreateNewToken() {
       Device device = new Device(deviceId: "1", deviceModel: "iOS 7", deviceDescription: "iPhone")
       assert device.getValidToken() != null
       assert device.getTokens().size() == 1
    }

    void testGetTokenUsesOldToken() {
        Device device = new Device(deviceId: "1", deviceModel: "iOS 7", deviceDescription: "iPhone")
        def firstToken = device.getValidToken()
        assert device.getTokens().size() == 1

        // assert that old token was used
        device.getValidToken().identifier == firstToken.identifier
    }

    void testGetTokenCreatesNewTokenIfOldTokenIsBlocked() {
        Device device = new Device(deviceId: "1", deviceModel: "iOS 7", deviceDescription: "iPhone")
        def firstToken = device.getValidToken()
        assert device.getTokens().size() == 1

        // invalidate first token
        firstToken.blocked = new Date()

        // assert that old token was used
        device.getValidToken().identifier != firstToken.identifier
    }

}
