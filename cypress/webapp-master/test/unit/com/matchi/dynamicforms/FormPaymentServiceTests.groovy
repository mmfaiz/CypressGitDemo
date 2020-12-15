package com.matchi.dynamicforms

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.User
import com.matchi.orders.Order
import com.matchi.price.PriceListCustomerCategory
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(FormPaymentService)
@Mock([Customer, Facility, Form, Municipality, Order, Region])
class FormPaymentServiceTests {

    void testCreateFormPaymentOrder() {
        def user = new User()
        def facility = createFacility()
        def form = createForm(facility)
        form.price = 100
        form.facility.vat = 25
        form.save(failOnError: true)

        def order = service.createFormPaymentOrder(user, form)

        assert order
        assert Order.Article.FORM_SUBMISSION == order.article
        assert form.createOrderDescription() == order.description
        assert order.metadata
        assert form.id.toString() == order.metadata.formId
        assert user == order.user
        assert user == order.issuer
        assert form.facility == order.facility
        assert order.dateDelivery
        assert form.price == order.price
        assert 20 == order.vat
        assert "web" == order.origin
        assert 1 == Order.count()
    }
}
