package com.matchi.adyen.authorization

import com.matchi.User

/**
 * When auto-deducting money for subscriptions
 */
class AdyenSubscriptionAuthorizationBuilder extends AdyenAuthorizationBuilder {

    @Override
    void setRequestParameters(Map request, Map params, User issuer) {
        setShopperInteraction(request, AdyenShopperInteraction.CONTAUTH)
        setSavedCardContracts(request, [AdyenSavedCardContract.RECURRING])
        setRecurringProcessingModel(request, AdyenRecurringProcessingModel.SUBSCRIPTION)
        setRecurringDetailsReference(request)
        setShopperIdentity(request, issuer)
    }
}
