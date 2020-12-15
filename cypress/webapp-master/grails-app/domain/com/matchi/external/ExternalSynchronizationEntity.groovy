package com.matchi.external

import org.joda.time.DateTime

public class ExternalSynchronizationEntity {

    public static final enum ExternalSystem {
        FORTNOX
    }

    public static final enum LocalEntity {
        INVOICE, CUSTOMER
    }

    /* local instance identifier */
    Long entityId

    /* external instance identifier */
    String externalEntityId

    /* last time of synchronization */
    DateTime lastSynchronized

    /* external system identifier */
    ExternalSystem externalSystem

    /* external system instance identifier */
    String instance

    /* entity type */
    LocalEntity entity

    static constraints = {
        entityId(unique: ['externalEntityId', 'externalSystem', 'entityId'])
        instance(nullable: true)
    }
}
