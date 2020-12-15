package com.matchi.adyen

import com.matchi.adyen.authorization.AdyenAuthorizationBuilderFactory
import com.matchi.adyen.authorization.AdyenNewCardStoreDetailsAuthorizationBuilder
import com.matchi.adyen.authorization.AdyenCardOnFileAuthorizationBuilder
import com.matchi.adyen.authorization.AdyenNewCardOneTimeAuthorizationBuilder
import com.matchi.adyen.authorization.AdyenRecurringProcessingModel
import com.matchi.adyen.authorization.AdyenSubscriptionAuthorizationBuilder
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Test

@TestMixin(GrailsUnitTestMixin)
class AdyenAuthorizationBuilderFactoryTests {

    @Test
    void testCreateStoredDetailsAuthorization() {
        assert AdyenAuthorizationBuilderFactory.createStoredDetailsAuthorization() instanceof AdyenCardOnFileAuthorizationBuilder
        assert AdyenAuthorizationBuilderFactory.createStoredDetailsAuthorization(AdyenRecurringProcessingModel.SUBSCRIPTION) instanceof AdyenSubscriptionAuthorizationBuilder
    }

    @Test
    void testCreateNewDetailsAuthorization() {
        assert AdyenAuthorizationBuilderFactory.createNewDetailsAuthorization() instanceof AdyenNewCardOneTimeAuthorizationBuilder
        assert AdyenAuthorizationBuilderFactory.createNewDetailsAuthorization(true) instanceof AdyenNewCardStoreDetailsAuthorizationBuilder
    }

}
