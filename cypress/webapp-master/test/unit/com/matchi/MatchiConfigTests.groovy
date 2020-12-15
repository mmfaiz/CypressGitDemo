package com.matchi

import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(MatchiConfig)
class MatchiConfigTests {
    void testMatchiConfig() {
        MatchiConfig matchiConfig = MatchiConfig.findByKey(MatchiConfigKey.MINIMUM_APP_VERSION)

        assert matchiConfig.keyName == MatchiConfigKey.MINIMUM_APP_VERSION.key
        assert MatchiConfig.simpleName == MatchiConfigKey.MINIMUM_APP_VERSION.clazz

    }

}
