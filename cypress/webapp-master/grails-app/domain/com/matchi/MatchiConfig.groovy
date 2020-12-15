package com.matchi

import org.apache.commons.lang.IllegalClassException

class MatchiConfig {

    def messageSource
    def userService

    String keyName
    MatchiConfigKey key
    String value

    User user

    Date dateCreated
    Date lastUpdated

    static transients = ['key'];

    static constraints = {
        keyName(nullable: false,  validator: { value ->
            if (!MatchiConfigKey.getByKey(value)) {
                return ['invalid.key']
            }
            return true
        })

        value(nullable: false,  validator: { value, obj ->
            Object possibleValues = MatchiConfigKey.getByKey(obj.keyName).possibleValues;
            if (possibleValues instanceof List && !possibleValues.contains(value)) {
                return "Not possible value for ¨${obj.key.title}'"
            }
            return true
        })
    }

    static mapping = {
        version false
        autoTimestamp true
        id generator: 'assigned', name: 'keyName', type: 'string'
        discriminator MatchiConfig.simpleName

    }

    static MatchiConfig findByKey(MatchiConfigKey key) {
        MatchiConfig setting
        if (key.clazz == MatchiConfig.simpleName) {
            setting = findOrCreateByKeyName(key.key)
        }
        else if (key.clazz == MatchiConfigMethodAvailability.simpleName) {
            setting = MatchiConfigMethodAvailability.findOrCreateByKeyName(key.key)
        }
        else {
            throw new IllegalClassException()
        }

        if (!setting) {
            throw new IllegalStateException("Setting might not be initialized, contact support@matchi.se.")
        }
        setting.key = key
        return setting
    }
}

enum MatchiConfigKey {
    DISABLE_DEVIATION("Disable create deviation", "Create deviations", MatchiConfigMethodAvailability, MatchiConfigMethodAvailability.STANDARD_AVAILABLE_CHOICES),
    DISABLE_SUBSCRIPTIONS("Disable create subscriptions", "Create Subscriptions", MatchiConfigMethodAvailability, MatchiConfigMethodAvailability.STANDARD_AVAILABLE_CHOICES),
    DISABLE_FACILITY_BOOKING_LIST("Disable booking list", "Booking list", MatchiConfigMethodAvailability, MatchiConfigMethodAvailability.STANDARD_AVAILABLE_CHOICES),
    DISABLE_FACILITY_STATISTICS("Disable facility statistics", "Facility Statistics", MatchiConfigMethodAvailability, MatchiConfigMethodAvailability.STANDARD_AVAILABLE_CHOICES),
    MINIMUM_APP_VERSION("Minimum mobile app version (not in use)", "Minimum app version", MatchiConfig)

    String key
    String title
    String methodName
    String clazz
    Object possibleValues

    MatchiConfigKey(String title, String methodName, Class clazz, possibleValues = null) {
        this.key = this.toString()
        this.title = title
        this.methodName = methodName
        this.clazz = clazz.simpleName
        this.possibleValues = possibleValues
    }

    static MatchiConfigKey getByKey(String key) {
        list().find { it.key == key }
    }

    static List list() {
        return [DISABLE_DEVIATION, DISABLE_SUBSCRIPTIONS, DISABLE_FACILITY_BOOKING_LIST, DISABLE_FACILITY_STATISTICS, MINIMUM_APP_VERSION]
    }
}


