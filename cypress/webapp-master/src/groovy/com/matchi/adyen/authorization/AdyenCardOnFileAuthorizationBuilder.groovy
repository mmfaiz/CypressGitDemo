package com.matchi.adyen.authorization

import com.matchi.User

/**
 * When user buys something online using saved payment details, and is present
 */
class AdyenCardOnFileAuthorizationBuilder extends AdyenAuthorizationBuilder {

    @Override
    void setRequestParameters(Map request, Map params, User issuer) {
        setShopperInteraction(request, AdyenShopperInteraction.CONTAUTH)
        setSavedCardContracts(request, [AdyenSavedCardContract.RECURRING])
        setRecurringProcessingModel(request, AdyenRecurringProcessingModel.CARD_ON_FILE)
        setRecurringDetailsReference(request)
        setShopperIdentity(request, issuer)
    }
}
