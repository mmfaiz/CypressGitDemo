package com.matchi.fortnox.v3

import com.matchi.Facility
import com.matchi.facility.Organization

import java.lang.reflect.Field
/**
 * @author Michael Astreiko
 */
class Fortnox3CustomerService {
    static transactional = false
    private static final String CUSTOMERS = "customers"

    def fortnox3Service

    /**
     *
     * @return
     */
    Boolean isFortnoxEnabledForFacility(Facility facility) {
        boolean result = false
        fortnox3Service.doGet(facility, CUSTOMERS, null, ['limit' : 1]) { def customers ->
            result = true
        }
        result
    }

    /**
     *
     * @return
     */
    Boolean isFortnoxEnabledForOrganization(Organization organization) {
        boolean result = false
        fortnox3Service.doGetForOrganization(organization, CUSTOMERS, null, ['limit' : 1]) { def customers ->
            result = true
        }
        result
    }
    /**
     *
     * @return
     */
    List<FortnoxCustomer> list(Facility facility, Integer page = null) {
        def result = []
        def requestParams = ['limit': 500]
        if(page) {
            requestParams['page'] = page
        }
        fortnox3Service.doGet(facility, CUSTOMERS, null, requestParams) { def responseJSON ->
            responseJSON.Customers.each { customerJSON ->
                result << new FortnoxCustomer(getCustomerPropertiesBasedOnJSON(customerJSON))
            }

            if(responseJSON.MetaInformation.'@TotalPages' > responseJSON.MetaInformation.'@CurrentPage') {
                result += list(facility, responseJSON.MetaInformation.'@CurrentPage' + 1)
            }
        }
        result
    }

    /**
     *
     * @param customerNumber
     * @return
     */
    FortnoxCustomer get(Facility facility, String customerNumber) {
        FortnoxCustomer result = null
        fortnox3Service.doGet(facility, CUSTOMERS, customerNumber) { def customerJSON ->
            result = new FortnoxCustomer(getCustomerPropertiesBasedOnJSON(customerJSON.Customer))
        }
        result
    }

    /**
     *
     * @param customer
     * @return
     */
    FortnoxCustomer set(Facility facility, FortnoxCustomer customer) {
        FortnoxCustomer result = null
        LinkedHashMap<String, Map> requestBody = prepareCustomerFields(customer)
        if (customer.id) {//updating existing
            fortnox3Service.doPut(facility, CUSTOMERS, customer.id, null, requestBody) { def customerJSON ->
                log.info("Updated customer with name ${customerJSON.Customer.CustomerNumber}")
                result = new FortnoxCustomer(getCustomerPropertiesBasedOnJSON(customerJSON.Customer))
            }
        } else {//new entry
            fortnox3Service.doPost(facility, CUSTOMERS, requestBody) { def customerJSON ->
                log.info("Created customer with name ${customerJSON.Customer.CustomerNumber}")
                result = new FortnoxCustomer(getCustomerPropertiesBasedOnJSON(customerJSON.Customer))
            }
        }
        return result
    }

    /**
     *
     * @param customer
     * @return
     */
    FortnoxCustomer setForOrganization(Organization organization, FortnoxCustomer customer) {
        FortnoxCustomer result = null
        LinkedHashMap<String, Map> requestBody = prepareCustomerFields(customer)
        if (customer.id) {//updating existing
            fortnox3Service.doPutForOrganization(organization, CUSTOMERS, customer.id, null, requestBody) { def customerJSON ->
                log.info("Updated customer with name ${customerJSON.Customer.CustomerNumber}")
                result = new FortnoxCustomer(getCustomerPropertiesBasedOnJSON(customerJSON.Customer))
            }
        } else {//new entry
            fortnox3Service.doPostForOrganization(organization, CUSTOMERS, requestBody) { def customerJSON ->
                log.info("Created customer with name ${customerJSON.Customer.CustomerNumber}")
                result = new FortnoxCustomer(getCustomerPropertiesBasedOnJSON(customerJSON.Customer))
            }
        }
        return result
    }

    private LinkedHashMap<String, Map> prepareCustomerFields(FortnoxCustomer customer) {
        Map customerMap = [:]
        FortnoxCustomer.class.getDeclaredFields().each { Field property ->
            if (property.getModifiers() == 2) {
                if (customer[property.name] != null) {
                    customerMap[property.name] = customer[property.name]
                } else if (customer.id) {//Only for update
                    customerMap[property.name] = Fortnox3Service.NULL_VALUE
                }
            }
        }
        [Customer: customerMap]
    }

    /**
     *
     * @param customerJSON
     * @return
     */
    private LinkedHashMap getCustomerPropertiesBasedOnJSON(customerJSON) {
        def availableFieldNames = FortnoxCustomer.class.getDeclaredFields()*.name
        def customerProperties = [:]
        customerJSON.entrySet().each {
            if (availableFieldNames.contains(it.key) && !it.key.startsWith('@') && !['DefaultDeliveryTypes', 'DefaultTemplates'].contains(it.key)) {
                //Few fields on List view differ from detail view
                if (it.key == 'Phone') {
                    customerProperties['Phone1'] = it.value
                } else if (it.value == '0,00') {//workaround for Fortnox API bug
                    customerProperties[it.key] = 0
                } else if (it.value == '1.0000') {//workaround for another Fortnox API bug :)
                    customerProperties[it.key] = 1
                } else {
                    customerProperties[it.key] = it.value?.getClass() == net.sf.json.JSONNull ? null : it.value
                }
            }
        }
        customerProperties
    }
}
