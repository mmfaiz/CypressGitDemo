package com.matchi

import com.matchi.external.ExternalSynchronizationEntity
import com.matchi.facility.Organization
import com.matchi.invoice.Invoice
import grails.transaction.Transactional
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.joda.time.DateTime

/**
 * External Synchronization Service keeps track of when an entity was last
 * synchornized against an external system.
 *
 * It basically keeps four pieces of information: external system, the entity, the external system identity
 * and last date and time of successful synchronization.
 */
class ExternalSynchronizationService {
    static transactional = false

    // Convenience map for looking up the local entity status
    static def entityClassMap = [
            (com.matchi.invoice.Invoice.class) : ExternalSynchronizationEntity.LocalEntity.INVOICE,
            (com.matchi.Customer.class)        : ExternalSynchronizationEntity.LocalEntity.CUSTOMER
    ]

    /**
     * Marks an external entity as synchornized
     * @param system The external system
     * @param systemInstance External system instance
     * @param object The entity
     * @param externalId The external id
     * @return
     */
    @Transactional
    def markSynchronized(ExternalSynchronizationEntity.ExternalSystem system, def systemInstance, def object, def externalId) {
        def entity = lookUpEntity(object)

        def synchronization = getOrCreate(system, systemInstance, entity, object, externalId)
        synchronization.lastSynchronized = new DateTime()
        synchronization.save()
    }

    @Transactional
    def delete(ExternalSynchronizationEntity.ExternalSystem system, def systemInstance, def object, def externalId) {
        def entity = lookUpEntity(object)

        def synchronization = getOrCreate(system, systemInstance, entity, object, externalId)
        synchronization.delete()
    }

    /**
     * Checks whether an entity should synchornize or not
     * @param system The external system
     * @param object The entity
     * @param lastLocalUpdate The last time the entity was saved
     * @return true if updated needed otherwise false
     */
    def shouldSynchronize(ExternalSynchronizationEntity.ExternalSystem system, def systemInstance, def object, DateTime lastLocalUpdate) {
        def entity = lookUpEntity(object)
        def synchronization = getSynchronization(system, systemInstance, entity, object)

        if (!synchronization || synchronization.lastSynchronized.isBefore(lastLocalUpdate)) {
            return true
        } else {
            return false
        }
    }

    // Convenience method
    def shouldSynchornize(ExternalSynchronizationEntity.ExternalSystem system, def systemInstance, def object, Date lastLocalUpdate) {
        return shouldSynchronize(system, systemInstance, object, new DateTime(lastLocalUpdate))
    }

    /**
     * Gets or creates an external synchronization entity
     * @param system The external system
     * @param systemInstance the system instance
     * @param entity The entity
     * @param object The local object
     * @param externalId The external id
     * @return
     */
    @Transactional
    def getOrCreate(ExternalSynchronizationEntity.ExternalSystem system, def systemInstance,
                    ExternalSynchronizationEntity.LocalEntity entity, def object, def externalId) {

        def synchronization = getSynchronization(system, systemInstance, entity, object)

        if (!synchronization) {
            synchronization = new ExternalSynchronizationEntity()
            synchronization.externalSystem = system
            synchronization.entity = entity
            synchronization.entityId = object.id
            synchronization.externalEntityId = externalId
            synchronization.lastSynchronized = new DateTime()
            synchronization.instance = systemInstance

            synchronization.save(failOnError: true)
        } else
            synchronization.refresh()

        log.info(synchronization.instance)
        synchronization
    }

    def getSynchronization(ExternalSynchronizationEntity.ExternalSystem system, def systemInstance,
                           ExternalSynchronizationEntity.LocalEntity entity, def object) {
        if (entity == ExternalSynchronizationEntity.LocalEntity.INVOICE) {
            //do not need systemInstance for Invoices - they are unique by entity id and type
            ExternalSynchronizationEntity.findByExternalSystemAndEntityAndEntityId(system, entity, object.id)
        } else {
            ExternalSynchronizationEntity.findByExternalSystemAndInstanceAndEntityAndEntityId(system, systemInstance, entity, object.id)
        }
    }

    def getEntityIdFromExternalId(ExternalSynchronizationEntity.ExternalSystem system, def systemInstance,
                           ExternalSynchronizationEntity.LocalEntity entity, def id) {
        ExternalSynchronizationEntity.findByExternalSystemAndInstanceAndEntityAndExternalEntityId(system, systemInstance, entity, id)?.entityId
    }

    // ----- Convenience methods
    def getFortnoxCustomerNumber(def customer, Organization organization = null) {
        def fortnox    = ExternalSynchronizationEntity.ExternalSystem.FORTNOX
        def entity     = ExternalSynchronizationEntity.LocalEntity.CUSTOMER
        def instance   = organization ? organization.fortnoxAccessToken : customer.facility.fortnoxAuthentication.db


        return getSynchronization(fortnox, instance, entity, customer)?.externalEntityId
    }

    def getLocalCustomerIdFromFortnoxCustomerNumber(def customerNumber, def instance) {
        def fortnox    = ExternalSynchronizationEntity.ExternalSystem.FORTNOX
        def entity     = ExternalSynchronizationEntity.LocalEntity.CUSTOMER

        return getEntityIdFromExternalId(fortnox, instance, entity, customerNumber)
    }

    def getFortnoxInvoiceNumber(Invoice invoice) {

        if(!invoice.customer.facility.hasFortnox()) {
            return null
        }

        def fortnox    = ExternalSynchronizationEntity.ExternalSystem.FORTNOX
        def entity     = ExternalSynchronizationEntity.LocalEntity.INVOICE
        def instance   = invoice.organization ? invoice.organization.fortnoxAccessToken : invoice.customer.facility.fortnoxAuthentication.db

        return getSynchronization(fortnox, instance, entity, invoice)?.externalEntityId
    }

    def lookUpEntity(def object) {
        def unwrapped = GrailsHibernateUtil.unwrapIfProxy(object)
        def entity = entityClassMap.get(unwrapped.class)
        if (!entity) {
            throw new IllegalArgumentException("Class ${unwrapped.class} is not configured as a synchronizable class")
        }
        entity
    }

}
