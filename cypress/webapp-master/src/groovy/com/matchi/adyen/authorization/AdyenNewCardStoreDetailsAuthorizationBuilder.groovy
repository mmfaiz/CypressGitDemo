package com.matchi.adyen.authorization

import com.matchi.User

/**
 * When shopper uses a new card and stores the details
 */
class AdyenNewCardStoreDetailsAuthorizationBuilder extends AdyenAuthorizationBuilder {

    @Override
    void setRequestParameters(Map request, Map params, User issuer) {
        setSavedCardContracts(request, AdyenSavedCardContract.list())
        setShopperInteraction(request, AdyenShopperInteraction.ECOMMERCE)
        setEncryptedCardData(request, params)
        setShopperIdentity(request, issuer)
    }

    @Override
    boolean storePaymentDetails() {
        return true
    }

}
