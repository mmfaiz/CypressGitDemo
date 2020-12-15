package com.matchi

import com.matchi.external.ExternalSynchronizationEntity
import com.matchi.facility.Organization
import com.matchi.invoice.Invoice
import grails.test.mixin.*
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.joda.time.DateTime
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ExternalSynchronizationService)
@Mock([Facility, FacilityProperty, Customer, ExternalSynchronizationEntity, Organization])
@TestMixin(DomainClassUnitTestMixin)
class ExternalSynchronizationServiceTests {

    private ExternalSynchronizationEntity.ExternalSystem fortnox
    private ExternalSynchronizationEntity.LocalEntity customerEntity
    private def instance

    @Before
    public void setUp() {
        fortnox = ExternalSynchronizationEntity.ExternalSystem.FORTNOX
        customerEntity = ExternalSynchronizationEntity.LocalEntity.CUSTOMER
        instance = "00000"
    }

    @Test
    void testLookupRightTargetEntity() {
        assert service.lookUpEntity(new Invoice()).toString()  ==
                ExternalSynchronizationEntity.LocalEntity.INVOICE.toString()

        assert service.lookUpEntity(new Customer()).toString() ==
                ExternalSynchronizationEntity.LocalEntity.CUSTOMER.toString()
    }

    @Test
    void testLookupEntityThrowsExceptionIfNotSynchronizable() {
        shouldFail(IllegalArgumentException) {
            service.lookUpEntity(new User())
        }
    }

    @Test
    void testGetSynchornizationEntity() {

        mockDomain(ExternalSynchronizationEntity)

        def customer = new Customer()
        customer.id = 1
        customer.lastUpdated = createDate(5).toDate()

        new ExternalSynchronizationEntity(
                externalSystem: fortnox,
                entity: customerEntity,
                instance: instance,
                entityId: 1, externalEntityId: 2,
                lastSynchronized: createDate(6))
                .save()

        assert service.getSynchronization(fortnox, instance, customerEntity, customer) != null
    }

    @Test
    void testShouldSynchronizeReturnsTrueIfNoEntry() {
        mockDomain(ExternalSynchronizationEntity)
        def customer = new Customer()
        customer.id = 1

        assert service.shouldSynchronize(ExternalSynchronizationEntity.ExternalSystem.FORTNOX, instance, customer, createDate(1))
    }

    @Test
    void testShouldSynchronizeReturnsFalseIfEntryUpdated() {
        mockDomain(ExternalSynchronizationEntity)

        def customer = new Customer()
        customer.id = 1
        customer.lastUpdated = createDate(5).toDate()

        def existingEntity = new ExternalSynchronizationEntity(
                externalSystem: fortnox,
                entity: customerEntity,
                instance: instance,
                entityId: 1,
                externalEntityId: 2,
                lastSynchronized: createDate(6))
                .save()

        assert !service.shouldSynchronize(fortnox, instance, customer, createDate(1))
    }

    @Test
    void testShouldSynchronizeReturnsTrueIfEntryUpdatedEarlier() {
        mockDomain(ExternalSynchronizationEntity)

        def customer = new Customer()
        customer.id = 1
        customer.lastUpdated = createDate(5).toDate()

        def existingEntity = new ExternalSynchronizationEntity(
                externalSystem: fortnox,
                entity: customerEntity,
                instance: instance,
                entityId: 1,
                externalEntityId: 2,
                lastSynchronized: createDate(3))
                .save()

        assert service.shouldSynchornize(fortnox, instance, customer, customer.lastUpdated)
    }

    @Test
    void testGetSynchronizationEntityReturnsFalseAfterMarkedAsSynchronize() {

        mockDomain(ExternalSynchronizationEntity)

        def customer = new Customer()
        customer.id = 1
        customer.lastUpdated = createDate(5).toDate()

        new ExternalSynchronizationEntity(
                externalSystem: fortnox,
                entity: customerEntity,
                instance: instance,
                entityId: 1, externalEntityId: 2,
                lastSynchronized: createDate(1))
                .save()

        // need synchornization
        assert service.shouldSynchornize(fortnox, instance, customer, customer.lastUpdated)

        // synch the entity
        service.markSynchronized(fortnox, instance,customer, 2)

        // does not need synchornization anymore
        assert !service.shouldSynchornize(fortnox, instance, customer, customer.lastUpdated)
    }

    @Test
    void testGetFortnoxCustomerNumber() {
        def customer = new Customer()
        customer.id = 1

        def facility = new Facility(name: "test").save(validate: false)
        new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN.name(), facility: facility, value: instance).save(validate: false)
        customer.facility = facility
        customer.lastUpdated = createDate(5).toDate()
        customer.save(validate: false)

        new ExternalSynchronizationEntity(
                externalSystem: fortnox,
                entity: customerEntity,
                instance: customer.facility.fortnoxAuthentication.db,
                entityId: 1, externalEntityId: 2,
                lastSynchronized: createDate(1))
                .save()

        // need synchornization
        assert "2" == service.getFortnoxCustomerNumber(customer)
    }

    @Test
    void testGetFortnoxCustomerNumberForOrganization() {
        def organizationInstance = "1111111"
        def customer = new Customer()
        customer.id = 1

        def facility = new Facility(name: "test").save(validate: false)
        new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN.name(), facility: facility, value: instance).save(validate: false)
        def organization = new Organization(name: "test", fortnoxAccessToken: organizationInstance).save(validate: false)
        customer.facility = facility
        customer.lastUpdated = createDate(5).toDate()
        customer.save(validate: false)

        new ExternalSynchronizationEntity(
                externalSystem: fortnox,
                entity: customerEntity,
                instance: instance,
                entityId: 1, externalEntityId: 2,
                lastSynchronized: createDate(1))
                .save()
        new ExternalSynchronizationEntity(
                externalSystem: fortnox,
                entity: customerEntity,
                instance: organizationInstance,
                entityId: 1, externalEntityId: 3,
                lastSynchronized: createDate(1))
                .save()

        // need synchornization
        assert "3" == service.getFortnoxCustomerNumber(customer, organization)
    }

    private DateTime createDate(def time) {
        return new DateTime(2010, 1,1,1,1, 1, time)
    }
}
