package com.matchi

import org.joda.time.LocalTime

class MatchiConfigMethodAvailability extends MatchiConfig {
    static LocalTime OFFICE_HOURS_START = new LocalTime("8:00:00")
    static LocalTime OFFICE_HOURS_END = new LocalTime("18:00:00")

    static NO = "no"
    static ALWAYS = "always"
    static OFFICE_HOURS = "office_hours"

    static List STANDARD_AVAILABLE_CHOICES = ['no', 'always', 'office_hours']

    static mapping = {
        discriminator MatchiConfigMethodAvailability.simpleName
    }

    Boolean isBlocked() {
        LocalTime now = new LocalTime()
        if (this.value == NO) {
            return false
        }
        if (this.value == ALWAYS) {
            return true
        }
        if (this.value == OFFICE_HOURS) {
            if (now.isAfter(OFFICE_HOURS_START) && now.isBefore(OFFICE_HOURS_END)) {
                return true
            }
        }
        return false
    }

    String isBlockedMessage() {
        Locale locale = new Locale(userService.getLoggedInUser().language)
        if (isBlocked()) {
            if (this.value == ALWAYS) {
                return messageSource.getMessage("admin.matchiConfig.disabled", [this.key.methodName] as String[], locale)
            }
            if (this.value == OFFICE_HOURS) {
                return messageSource.getMessage("admin.matchiConfig.disabledAtOfficeHours", [this.key.methodName, OFFICE_HOURS_END.toString("HH:mm")] as String[], locale)
            }
        }
        return ""
    }
}