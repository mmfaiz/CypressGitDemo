package com.matchi.adyen.authorization

enum AdyenShopperInteraction {
    ECOMMERCE("Ecommerce"),
    CONTAUTH("ContAuth")

    final String value

    AdyenShopperInteraction(String value) {
        this.value = value
    }
}