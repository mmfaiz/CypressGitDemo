package com.matchi

import grails.test.mixin.Mock
import org.springframework.transaction.TransactionStatus

import static com.matchi.TestUtils.*

/**
 * This file is to test Grails/Groovy related things for educational purposes.
 */
class HypothesisTests extends GroovyTestCase {

    /**
     * Just tests an ordinary transaction.
     */
    void testTransactionSucceeding() {
        Facility facility = createFacility()
        Customer customer1 = createCustomer(facility)
        Customer customer2 = createCustomer(facility)

        Customer.withTransaction { TransactionStatus status ->
            [customer1, customer2].each { Customer customer ->
                customer.firstname = "Sune Rudolf"
                customer.lastname = "Andersson"
                customer.save()
            }
        }

        assert customer1.fullName() == "Sune Rudolf Andersson"
        assert customer2.fullName() == "Sune Rudolf Andersson"
    }

    /**
     * Tests how objects are affected by being interrupted by an exception.
     */
    void testTransactionFailing() {
        Facility facility = createFacility()
        Customer customer1 = createCustomer(facility)
        Customer customer2 = createCustomer(facility)

        String customer1name = customer1.fullName()
        String customer2name = customer2.fullName()

        shouldFail(Exception) {
            Customer.withTransaction { TransactionStatus status ->
                [customer1, customer2].each { Customer customer ->

                    if(customer == customer2) {
                        throw new Exception("Let's fail this transaction!")
                    }

                    customer.firstname = "Sune Rudolf"
                    customer.lastname = "Andersson"
                    customer.save()
                }
            }
        }

        /*
            This first object should not be saved to the database, since it was not flushed.
         */
        assert customer1.isDirty()
        assert customer1.fullName() == "Sune Rudolf Andersson"

        /*
            By refreshing, the object is restored to its database state.
         */
        customer1.refresh()
        assert customer1.fullName() == customer1name

        /*
         * Since this was never affected, it should remain as before the transaction.
         */
        assert !customer2.isDirty()
        assert customer2.fullName() == customer2name
    }

    /**
     * Tests how objects are affected by being interrupted by an exception, and then rolled back.
     * This should result in the same state as the above test (testTransactionFailing), since the transaction
     * there is automatically rolled back.
     */
    void testTransactionFailingCatchingException() {
        Facility facility = createFacility()
        Customer customer1 = createCustomer(facility)
        Customer customer2 = createCustomer(facility)

        String customer1name = customer1.fullName()
        String customer2name = customer2.fullName()

        Customer.withTransaction { TransactionStatus status ->
            try {
                [customer1, customer2].each { Customer customer ->

                    if(customer == customer2) {
                        throw new Exception("Let's fail this transaction!")
                    }

                    customer.firstname = "Sune Rudolf"
                    customer.lastname = "Andersson"
                    customer.save()
                }
            } catch (Exception e) {
                status.setRollbackOnly()
            }
        }

        /*
            This first object should not be saved to the database, since it was not flushed.
         */
        assert customer1.isDirty()
        assert customer1.fullName() == "Sune Rudolf Andersson"

        /*
            By refreshing, the object is restored to its database state.
         */
        customer1.refresh()
        assert customer1.fullName() == customer1name

        /*
         * Since this was never affected, it should remain as before the transaction.
         */
        assert !customer2.isDirty()
        assert customer2.fullName() == customer2name
    }

    /**
     * This demonstrates how flushing objects inside a transaction sabotages the purpose of using a transaction.
     */
    void testTransactionFailingAndFlushing() {
        Facility facility = createFacility()
        Customer customer1 = createCustomer(facility)
        Customer customer2 = createCustomer(facility)

        String customer2name = customer2.fullName()

        shouldFail(Exception) {
            Customer.withTransaction { TransactionStatus status ->
                [customer1, customer2].each { Customer customer ->

                    if(customer == customer2) {
                        throw new Exception("Let's fail this transaction!")
                    }

                    customer.firstname = "Sune Rudolf"
                    customer.lastname = "Andersson"
                    customer.save(flush: true)
                }
            }
        }

        /*
            The object is persisted to the database due to flushing
         */
        assert !customer1.isDirty()
        assert customer1.fullName() == "Sune Rudolf Andersson"

        /*
            As you can see, no difference
         */
        customer1.refresh()
        assert customer1.fullName() == "Sune Rudolf Andersson"

        /*
            Not affected at all
         */
        assert !customer2.isDirty()
        assert customer2.fullName() == customer2name
    }

}
