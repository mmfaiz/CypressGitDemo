package com.matchi.adyen.authorization

import com.matchi.User

/**
 * AuthorizationBuilder for cases when paying with new card, don't save
 */
class AdyenNewCardOneTimeAuthorizationBuilder extends AdyenAuthorizationBuilder {

    @Override
    void setRequestParameters(Map request, Map params, User issuer) {
        setShopperInteraction(request, AdyenShopperInteraction.ECOMMERCE)
        setEncryptedCardData(request, params)
    }
}
