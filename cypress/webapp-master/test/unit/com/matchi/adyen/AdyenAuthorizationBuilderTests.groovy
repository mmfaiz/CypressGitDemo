package com.matchi.adyen

import com.matchi.User
import com.matchi.adyen.authorization.AdyenAuthorizationBuilder
import com.matchi.adyen.authorization.AdyenAuthorizationBuilderFactory
import com.matchi.adyen.authorization.AdyenNewCardStoreDetailsAuthorizationBuilder
import com.matchi.adyen.authorization.AdyenCardOnFileAuthorizationBuilder
import com.matchi.adyen.authorization.AdyenNewCardOneTimeAuthorizationBuilder
import com.matchi.adyen.authorization.AdyenRecurringProcessingModel
import com.matchi.adyen.authorization.AdyenSavedCardContract
import com.matchi.adyen.authorization.AdyenShopperInteraction
import com.matchi.adyen.authorization.AdyenSubscriptionAuthorizationBuilder
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Test

@TestMixin(GrailsUnitTestMixin)
@Mock([User])
class AdyenAuthorizationBuilderTests {

    @Test
    void testCreateStoredDetailsAuthorization() {
        AdyenCardOnFileAuthorizationBuilder adyenCardOnFileAuthorizationBuilder = AdyenAuthorizationBuilderFactory.createStoredDetailsAuthorization()

        assert !adyenCardOnFileAuthorizationBuilder.storePaymentDetails()

        Map params = [:]
        params.put(AdyenAuthorizationBuilder.KEY_ADYEN_ENCRYPTED_DATA, "someEncryptedData")

        Map request = [:]
        User user = new User(
                id: 123l,
                email: 'user@matchi.se',
                firstname: 'User',
                lastname: 'Matchisson'
        ).save(flush: true, failOnError: true)

        adyenCardOnFileAuthorizationBuilder.setRequestParameters(request, params, user)

        assert request[AdyenAuthorizationBuilder.KEY_SHOPPER_INTERACTION] == AdyenShopperInteraction.CONTAUTH.value
        assert request[AdyenAuthorizationBuilder.KEY_RECURRING_OBJECT][AdyenAuthorizationBuilder.KEY_CONTRACT] == AdyenSavedCardContract.RECURRING.value
        assert request[AdyenAuthorizationBuilder.KEY_RECURRING_PROCESSING_MODEL] == AdyenRecurringProcessingModel.CARD_ON_FILE.value
        assert request[AdyenAuthorizationBuilder.KEY_SELECTED_RECURRING_DETAILS_REFERENCE] == AdyenAuthorizationBuilder.DEFAULT_DETAILS_REFERENCE
        assert !request[AdyenAuthorizationBuilder.KEY_ADDITIONAL_DATA]
        assert request[AdyenAuthorizationBuilder.KEY_SHOPPER_EMAIL] == user.email
        assert request[AdyenAuthorizationBuilder.KEY_SHOPPER_REFERENCE] == user.id.toString()

        user.delete(flush: true)
    }

    @Test
    void testCreateStoredDetailsAuthorizationRecurring() {
        AdyenSubscriptionAuthorizationBuilder adyenSubscriptionAuthorizationBuilder = AdyenAuthorizationBuilderFactory.createStoredDetailsAuthorization(AdyenRecurringProcessingModel.SUBSCRIPTION)

        assert !adyenSubscriptionAuthorizationBuilder.storePaymentDetails()

        Map params = [:]
        Map request = [:]
        User user = new User(
                id: 123l,
                email: 'user@matchi.se',
                firstname: 'User',
                lastname: 'Matchisson'
        ).save(flush: true, failOnError: true)

        adyenSubscriptionAuthorizationBuilder.setRequestParameters(request, params, user)

        assert request[AdyenAuthorizationBuilder.KEY_SHOPPER_INTERACTION] == AdyenShopperInteraction.CONTAUTH.value
        assert request[AdyenAuthorizationBuilder.KEY_RECURRING_OBJECT][AdyenAuthorizationBuilder.KEY_CONTRACT] == AdyenSavedCardContract.RECURRING.value
        assert request[AdyenAuthorizationBuilder.KEY_RECURRING_PROCESSING_MODEL] == AdyenRecurringProcessingModel.SUBSCRIPTION.value
        assert request[AdyenAuthorizationBuilder.KEY_SELECTED_RECURRING_DETAILS_REFERENCE] == AdyenAuthorizationBuilder.DEFAULT_DETAILS_REFERENCE
        assert !request[AdyenAuthorizationBuilder.KEY_ADDITIONAL_DATA]
        assert request[AdyenAuthorizationBuilder.KEY_SHOPPER_EMAIL] == user.email
        assert request[AdyenAuthorizationBuilder.KEY_SHOPPER_REFERENCE] == user.id.toString()

        user.delete(flush: true)
    }

    @Test
    void testCreateNewDetailsAuthorization() {
        AdyenNewCardOneTimeAuthorizationBuilder adyenOneTimeCardAuthorizationBuilder = AdyenAuthorizationBuilderFactory.createNewDetailsAuthorization()

        assert !adyenOneTimeCardAuthorizationBuilder.storePaymentDetails()

        Map params = [:]
        params.put(AdyenAuthorizationBuilder.KEY_ADYEN_ENCRYPTED_DATA, "someEncryptedData")

        Map request = [:]
        User user = new User(
                id: 123l,
                email: 'user@matchi.se',
                firstname: 'User',
                lastname: 'Matchisson'
        ).save(flush: true, failOnError: true)

        adyenOneTimeCardAuthorizationBuilder.setRequestParameters(request, params, user)

        assert request[AdyenAuthorizationBuilder.KEY_SHOPPER_INTERACTION] == AdyenShopperInteraction.ECOMMERCE.value
        assert !request[AdyenAuthorizationBuilder.KEY_RECURRING_OBJECT]
        assert !request[AdyenAuthorizationBuilder.KEY_RECURRING_PROCESSING_MODEL]
        assert !request[AdyenAuthorizationBuilder.KEY_SELECTED_RECURRING_DETAILS_REFERENCE]
        assert request[AdyenAuthorizationBuilder.KEY_ADDITIONAL_DATA][AdyenAuthorizationBuilder.KEY_CARD_ENCRYPTED_JSON] == params[AdyenAuthorizationBuilder.KEY_ADYEN_ENCRYPTED_DATA]
        assert !request[AdyenAuthorizationBuilder.KEY_SHOPPER_EMAIL]
        assert !request[AdyenAuthorizationBuilder.KEY_SHOPPER_REFERENCE]

        user.delete(flush: true)
    }

    @Test
    void testCreateNewDetailsAuthorizationWithStorage() {
        AdyenNewCardStoreDetailsAuthorizationBuilder adyenNewCardStoreDetailsAuthorizationBuilder = AdyenAuthorizationBuilderFactory.createNewDetailsAuthorization(true)

        assert adyenNewCardStoreDetailsAuthorizationBuilder.storePaymentDetails()

        Map params = [:]
        params.put(AdyenAuthorizationBuilder.KEY_ADYEN_ENCRYPTED_DATA, "someEncryptedData")

        Map request = [:]
        User user = new User(
                id: 123l,
                email: 'user@matchi.se',
                firstname: 'User',
                lastname: 'Matchisson'
        ).save(flush: true, failOnError: true)

        adyenNewCardStoreDetailsAuthorizationBuilder.setRequestParameters(request, params, user)

        assert request[AdyenAuthorizationBuilder.KEY_SHOPPER_INTERACTION] == AdyenShopperInteraction.ECOMMERCE.value
        assert request[AdyenAuthorizationBuilder.KEY_RECURRING_OBJECT][AdyenAuthorizationBuilder.KEY_CONTRACT] == AdyenSavedCardContract.list()*.value.join(AdyenAuthorizationBuilder.SEPARATOR)
        assert !request[AdyenAuthorizationBuilder.KEY_RECURRING_PROCESSING_MODEL]
        assert !request[AdyenAuthorizationBuilder.KEY_SELECTED_RECURRING_DETAILS_REFERENCE]
        assert request[AdyenAuthorizationBuilder.KEY_ADDITIONAL_DATA][AdyenAuthorizationBuilder.KEY_CARD_ENCRYPTED_JSON] == params[AdyenAuthorizationBuilder.KEY_ADYEN_ENCRYPTED_DATA]
        assert request[AdyenAuthorizationBuilder.KEY_SHOPPER_EMAIL]
        assert request[AdyenAuthorizationBuilder.KEY_SHOPPER_REFERENCE]

        user.delete(flush: true)
    }

}
