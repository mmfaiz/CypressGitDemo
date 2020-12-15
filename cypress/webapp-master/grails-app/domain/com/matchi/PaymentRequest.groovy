package com.matchi

class PaymentRequest implements Serializable {

    public static enum Status {
        INITIATED, RECEIVED
    }

    String orderNr
    String email
    Status status

    Date dateCreated
    Date lastUpdated

    static mapping = {
        autoTimestamp true
        sort 'dateCreated'
    }


    static constraints = {
    }
}
