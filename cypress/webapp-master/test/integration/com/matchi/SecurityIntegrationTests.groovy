package com.matchi

import org.junit.Test

import static com.matchi.TestUtils.*

/**
 * Test cases to experiment with different solutions
 */
class SecurityIntegrationTests extends GroovyTestCase {

    private final String injectionQuery = "hej%' or 1=1 or firstname like '%hej"
    private final String injectionQueryDoubleQuotes = 'hej%" or 1=1 or firstname like "%hej'

    @Test
    void testUnSafeWithSQLInjection() {
        Facility facility = createFacility()
        Customer customer1 = createCustomer(facility, "sune@matchi.se", "Sune", "Andersson")
        Customer customer2 = createCustomer(facility, "rudolf@matchi.se", "Rudolf", "Svensson")

        List<Customer> goodResult = fetchCustomersUnsafe("une anders")
        assert goodResult.size() == 1
        assert goodResult[0] == customer1

        List<Customer> badResult = fetchCustomersUnsafe(injectionQuery)
        assert badResult.size() >= 2 // Due to a lot of other customers in bootstrapping integration tests
        assert badResult.contains(customer1)
        assert badResult.contains(customer2)
    }

    @Test
    void testSafeWithSQLInjection() {
        Facility facility = createFacility()
        Customer customer1 = createCustomer(facility, "sune@matchi.se", "Sune", "Andersson")
        Customer customer2 = createCustomer(facility, "rudolf@matchi.se", "Rudolf", "Svensson")

        List<Customer> goodResult = fetchCustomersSafe("une anders")
        assert goodResult.size() == 1
        assert goodResult[0] == customer1

        List<Customer> badResult = fetchCustomersSafe(injectionQuery)
        assert badResult.size() == 0

        badResult = fetchCustomersSafe(injectionQueryDoubleQuotes)
        assert badResult.size() == 0
    }

    /**
     * This is how we should NOT do!
     * @param fullname
     * @return
     */
    private List<Customer> fetchCustomersUnsafe(String fullname) {
        return Customer.createCriteria().list {
            sqlRestriction("concat(firstname, ' ', lastname) like '%${fullname}%'")
        }
    }

    /**
     * This is how we should do :)
     * @param fullname
     * @return
     */
    private List<Customer> fetchCustomersSafe(String fullname) {
        return Customer.createCriteria().list {
            sqlRestriction("concat(firstname, ' ', lastname) like ?", ["%${fullname}%" as String])
        }
    }

}
