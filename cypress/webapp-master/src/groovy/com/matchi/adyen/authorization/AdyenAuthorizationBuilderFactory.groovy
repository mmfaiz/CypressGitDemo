package com.matchi.adyen.authorization

/**
 * Singleton class for building AdyenAuthorizationBuilder instances based on type of request
 */
class AdyenAuthorizationBuilderFactory {

    /**
     * Private constructor to ensure Singleton behaviour
     */
    private AdyenAuthorizationBuilderFactory() {

    }

    /**
     * For stored payment details
     * @param adyenSavedCardContract
     * @return
     */
    static AdyenAuthorizationBuilder createStoredDetailsAuthorization(final AdyenRecurringProcessingModel recurringProcessingModel = AdyenRecurringProcessingModel.CARD_ON_FILE) {
        if (recurringProcessingModel == AdyenRecurringProcessingModel.SUBSCRIPTION) {
            return new AdyenSubscriptionAuthorizationBuilder()
        }

        return new AdyenCardOnFileAuthorizationBuilder()
    }

    /**
     * For new payment details
     * @param storeDetails
     * @return
     */
    static AdyenAuthorizationBuilder createNewDetailsAuthorization(final boolean storeDetails = false) {
        if(storeDetails) {
            return new AdyenNewCardStoreDetailsAuthorizationBuilder()
        }

        return new AdyenNewCardOneTimeAuthorizationBuilder()
    }

}
