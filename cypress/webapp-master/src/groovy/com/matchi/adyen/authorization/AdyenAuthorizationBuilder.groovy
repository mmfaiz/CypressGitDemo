package com.matchi.adyen.authorization

import com.matchi.User

/**
 * Interface for different builders of Adyen authorization requests
 */
abstract class AdyenAuthorizationBuilder {

    static final String KEY_ADDITIONAL_DATA = "additionalData"
    static final String KEY_CARD_ENCRYPTED_JSON = "card.encrypted.json"
    static final String KEY_ADYEN_ENCRYPTED_DATA = "adyen-encrypted-data"
    static final String KEY_SHOPPER_INTERACTION = "shopperInteraction"
    static final String KEY_SHOPPER_EMAIL = "shopperEmail"
    static final String KEY_SHOPPER_REFERENCE = "shopperReference"
    static final String KEY_RECURRING_OBJECT = "recurring"
    static final String KEY_CONTRACT = "contract"
    static final String KEY_RECURRING_PROCESSING_MODEL = "recurringProcessingModel"
    static final String KEY_SELECTED_RECURRING_DETAILS_REFERENCE = "selectedRecurringDetailReference"

    static final String DEFAULT_DETAILS_REFERENCE = "LATEST"
    static final String SEPARATOR = ","

    abstract void setRequestParameters(Map request, Map params, User issuer)

    /**
     * Add encrypted card data to additionalData
     * @param request
     * @param params
     */
    protected void setEncryptedCardData(Map request, Map params) {
        if(!request[KEY_ADDITIONAL_DATA]) {
            request.put(KEY_ADDITIONAL_DATA, [:])
        }

        request[KEY_ADDITIONAL_DATA].put(KEY_CARD_ENCRYPTED_JSON, params.get(KEY_ADYEN_ENCRYPTED_DATA))
    }

    /**
     * Sets the shopper interaction, indicating if shopper was present online or not
     * @param request
     * @param shopperInteraction
     */
    protected void setShopperInteraction(Map request, AdyenShopperInteraction shopperInteraction) {
        request.put(KEY_SHOPPER_INTERACTION, shopperInteraction.value)
    }

    /**
     * Sets shopper identity which is used for saving and retrieving stored card details
     * @param request
     * @param issuer
     */
    protected void setShopperIdentity(Map request, User issuer) {
        String shopperID    = issuer.id
        String shopperEmail = issuer.email

        request.put(KEY_SHOPPER_EMAIL, shopperEmail)
        request.put(KEY_SHOPPER_REFERENCE, shopperID)
    }

    /**
     * The type of contract used, first specified when saving the card
     * @param request
     * @param contracts
     */
    protected void setSavedCardContracts(Map request, List<AdyenSavedCardContract> contracts) {
        Map contractsMap = [:]
        contractsMap.put(KEY_CONTRACT, contracts*.value.join(SEPARATOR))
        request.put(KEY_RECURRING_OBJECT, contractsMap)
    }

    /**
     * Sets recurringProcessingModel, how the transaction should be processed
     * @param request
     * @param adyenRecurringProcessingModel
     */
    protected void setRecurringProcessingModel(Map request,
                                               AdyenRecurringProcessingModel adyenRecurringProcessingModel) {
        request.put(KEY_RECURRING_PROCESSING_MODEL, adyenRecurringProcessingModel.value)
    }

    /**
     * Sets which stored details to use, currently we only set "LATEST"
     * @param request
     */
    protected void setRecurringDetailsReference(Map request) {
        request.put(KEY_SELECTED_RECURRING_DETAILS_REFERENCE, DEFAULT_DETAILS_REFERENCE)
    }

    /**
     * Default behaviour is not to save new card
     * @return
     */
    boolean storePaymentDetails() {
        return false
    }

}