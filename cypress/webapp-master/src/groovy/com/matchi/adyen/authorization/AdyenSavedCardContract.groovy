package com.matchi.adyen.authorization

/**
 * Two different types of contracts for using Adyen saved cards
 *
 */
enum AdyenSavedCardContract {
    ONE_CLICK("ONECLICK"), RECURRING("RECURRING")

    final String value

    AdyenSavedCardContract(String value) {
        this.value = value
    }

    static List<AdyenSavedCardContract> list() {
        return [ONE_CLICK, RECURRING]
    }
}