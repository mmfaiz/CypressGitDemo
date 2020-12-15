package com.matchi

import com.matchi.api.Code
import com.matchi.api.DeviceBlockedException
import com.matchi.devices.Device
import com.matchi.devices.Token

import javax.servlet.http.HttpServletRequest

class AuthenticationService {
    static transactional = false

    def userService
    def grailsApplication

    final static String APP_PLATFORM_KEY = "app-platform"
    final static String APP_VERSION_KEY = "app-version"

    /**
     * Checks email and password against users and returns the authentication token if the
     * credentials is correct.
     *
     * @param email
     * @param password
     * @return Authentication token for user if valid, otherwise null
     */
    def authenticateCredentials(String email, String password) {
        if (!email || !password) {
            return null
        }

        def user = User.findByEmail(email)

        if (user && userService.checkPassword(user, password)) {
            return user
        } else {
            return null
        }
    }

    /**
     *
     * @param user
     * @param deviceId
     * @param deviceModel
     * @param deviceDescription
     * @return
     */
    def provision(def user, def deviceId, def deviceModel, def deviceDescription) {

        // register device
        def device = getOrCreateDeviceForUser(user, deviceId, deviceModel, deviceDescription)

        if (device.blocked) {
            throw new DeviceBlockedException(402, Code.DEVICE_BLOCKED, "Device is blocked")
        }

        // get or create token if allowed
        def token = device.getValidToken()

        // return token
        return token
    }

    def getOrCreateDeviceForUser(User user, String deviceId, def deviceModel, def deviceDescription) {
        def device = Device.findByUserAndDeviceId(user, deviceId)

        if (!device) {
            device = new Device(user: user, deviceId: deviceId)
        }

        device.deviceModel = deviceModel
        device.deviceDescription = deviceDescription
        device.save()

        device
    }

    /**
     * Authenticates a token identifier and returns a <code>Token</code> if
     * authentication is successful, otherwise null.
     * @param identifier
     * @return A token if authentication is successful
     */
    def authenticate(def identifier) {
        def token = getToken(identifier)

        if (token && token.isValid()) {
            def user = token.device.user

            if (user.accountLocked || user.accountExpired || user.dateBlocked) {
                log.info("User account \"${user.email}\" is locked or expired")
                return null
            }

            Device.executeUpdate("update Device set lastUsed = :date where id = :id",
                    [date: new Date(), id: token.device.id])

            return token

        } else {
            return null
        }

    }

    def getToken(String identifier) {
        return Token.findByIdentifier(identifier, [lock: true])
    }

    String extractBasicAuthUsername(HttpServletRequest request) {
        String authString = request.getHeader('Authorization')
        String username = null

        if (authString) {
            String encodedPair = authString - 'Basic '
            String decodedPair = new String(encodedPair.decodeBase64());
            String[] credentials = decodedPair.split(':')

            username = (credentials.size() > 0 ? credentials[0] : null)
        } else {
            log.info("No HTTP Basic header found")
        }

        return username
    }

    boolean checkAppVersion(HttpServletRequest request) {
        if (!request) {
            return false
        }

        Map minimumVersionConfig = grailsApplication.config.minimumAppVersion
        if (minimumVersionConfig && minimumVersionConfig.entrySet().every { e -> e.value }) {
            String os = request.getHeader(APP_PLATFORM_KEY)
            String version = request.getHeader(APP_VERSION_KEY)

            if (os && version) {
                String minimumVersion = minimumVersionConfig[os]
                if (minimumVersion) {
                    return isRequestVersionEqualToOrGreaterThan(version, minimumVersion)
                }
            }

            return false
        }

        return true
    }

    boolean isRequestVersionEqualToOrGreaterThan(String requestVersion, String configVersion) {
        String[] requestVersionPartials = requestVersion.split(/\./)
        String[] configVersionPartials = configVersion.split(/\./)

        int i = 0;

        for (; i < requestVersionPartials.length; i++) {

            // If configVersion had no specified version, request wins
            if (i >= configVersionPartials.length) {
                return true
            }

            BigInteger requestPartial = requestVersionPartials[i].toBigInteger()
            BigInteger configPartial = configVersionPartials[i].toBigInteger()

            if (requestPartial != configPartial) {
                return requestPartial > configPartial
            }
        }

        for (; i < configVersionPartials.length; i++) {
            if (configVersionPartials[i].toBigInteger() > 0) {
                return false
            }
        }

        return true
    }
}
