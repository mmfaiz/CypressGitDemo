package com.matchi.admin

import com.matchi.MatchiConfig
import com.matchi.MatchiConfigKey

class AdminMatchiConfigController {

    def userService

    def index() {
        List<MatchiConfigKey> keys = MatchiConfigKey.list()

        Map<String, MatchiConfig> settings = [:]

        keys.each {
            settings[it.toString()] = MatchiConfig.findByKeyName(it.key)
        }

        [keys: keys, settings: settings]
    }

    def update() {
        def loggedInUser = userService.getLoggedInUser()

        MatchiConfigKey.list().each {
            MatchiConfig setting = MatchiConfig.findByKey(it)
            setting.value = params[it.key]
            setting.user = loggedInUser
            if (setting.validate()) {
                setting.save()
            }
            else {
                flash.error = flash.error + setting.errors.allErrors.join(", ")
            }
        }
        redirect action: "index"
    }
}
