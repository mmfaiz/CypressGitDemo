package com.matchi.orders

import com.matchi.adyen.AdyenNotification

class AdyenOrderPaymentError implements Serializable {
    private static final long serialVersionUID = 12L

    AdyenNotification.EventCode action
    String reason

    Date dateCreated
    Date lastUpdated

    static constraints = {
        action nullable: false
        reason nullable: false
    }
}
