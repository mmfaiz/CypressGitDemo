package com.matchi

class PaymentInfo implements Serializable {

    static belongsTo = [user: User]

    // Netaxept
    String hash
    String maskedPan
    String issuer
    String expiryDate
    String transactionId

    // Adyen
    String expiryMonth
    String expiryYear
    String holderName
    String number

    PaymentProvider provider

    Date dateCreated
    Date lastUpdated

    static constraints = {
        // Netaxept
        hash nullable: true
        maskedPan nullable: true
        issuer nullable: true
        expiryDate nullable: true
        transactionId nullable: true

        // Adyen
        expiryMonth nullable: true
        expiryYear nullable: true
        holderName nullable: true
        number nullable: true

        provider nullable: false
    }

    static mapping = {
        autoTimestamp true
    }

    String formatExpiryDate() {
        if (expiryDate) {
            return "${expiryDate.substring(0, 2)} / ${expiryDate.substring(2, 4)}"
        } else if (expiryMonth && expiryYear) {
            return "${expiryMonth} / ${expiryYear.substring(2, 4)}"
        }

        return null
    }

    static enum PaymentProvider {
        ADYEN
    }
}
