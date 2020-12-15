package com.matchi.adyen.authorization

/**
 * How the saved card payment should be processed
 */
enum AdyenRecurringProcessingModel {
    CARD_ON_FILE("CardOnFile"),
    SUBSCRIPTION("Subscription")

    final String value

    AdyenRecurringProcessingModel(String value) {
        this.value = value
    }
}