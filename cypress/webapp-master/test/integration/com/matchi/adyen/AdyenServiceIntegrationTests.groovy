package com.matchi.adyen

class AdyenServiceIntegrationTests extends GroovyTestCase {
    def adyenService

    // TODO: Make tests work
    void testSomething() {

    }
    /*@Test
    void testGetPaymentsToCapture() {
        User user         = createUser("test@adyen.com")
        Facility facility = createFacility()
        Order order1 = createOrder(user, facility, Order.Article.BOOKING, [:],
                new LocalDateTime().minusDays(8).toDate())
        Order order2 = createOrder(user, facility, Order.Article.BOOKING, [:],
                        new Date(), new LocalDateTime().minusHours(13).toDate())

        // Payments which should NOT be fetched
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.NEW)
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.CAPTURED)
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.CREDITED)
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.ANNULLED)
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.FAILED)
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.PENDING)

        // Two payments which should be fetched
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.AUTHED)
        createAdyenOrderPayment(user, order2, RandomStringUtils.random(4), OrderPayment.Status.AUTHED)

        assert adyenService.getPaymentsToBeCaptured()?.size() == 2
    }

    @Test
    void testGetPaymentsToRetry() {
        User user         = createUser("test@adyen.com")
        Facility facility = createFacility()
        Order order = createOrder(user, facility, Order.Article.BOOKING)

        AdyenOrderPaymentError error1 = createAdyenOrderPaymentError(new LocalDateTime().minusDays(8).toDate())
        AdyenOrderPaymentError error2 = createAdyenOrderPaymentError(new Date(), new LocalDateTime().minusDays(2).toDate())

        // Payments which should NOT be fetched
        createAdyenOrderPayment(user, order, RandomStringUtils.random(4), OrderPayment.Status.NEW)
        createAdyenOrderPayment(user, order, RandomStringUtils.random(4), OrderPayment.Status.AUTHED)
        createAdyenOrderPayment(user, order, RandomStringUtils.random(4), OrderPayment.Status.CAPTURED)
        createAdyenOrderPayment(user, order, RandomStringUtils.random(4), OrderPayment.Status.CREDITED)
        createAdyenOrderPayment(user, order, RandomStringUtils.random(4), OrderPayment.Status.ANNULLED)
        createAdyenOrderPayment(user, order, RandomStringUtils.random(4), OrderPayment.Status.FAILED)
        createAdyenOrderPayment(user, order, RandomStringUtils.random(4), OrderPayment.Status.PENDING)

        // Two payments which should be fetched
        createAdyenOrderPayment(user, order, RandomStringUtils.random(4), OrderPayment.Status.FAILED,
                PaymentMethod.CREDIT_CARD, error1)
        createAdyenOrderPayment(user, order, RandomStringUtils.random(4), OrderPayment.Status.FAILED,
                PaymentMethod.CREDIT_CARD, error2)

        assert adyenService.getPaymentsToRetry()?.size() == 2
    }

    @Test
    void testGetPaymentsToAnnull() {
        User user         = createUser("test@adyen.com")
        Facility facility = createFacility()
        Order order1 = createOrder(user, facility, Order.Article.BOOKING)
        Order order2 = createOrder(user, facility, Order.Article.BOOKING, [:],
                new LocalDateTime().minusHours(7).toDate())
        Order order3 = createOrder(user, facility, Order.Article.BOOKING, [:],
                new LocalDateTime().toDate(), new LocalDateTime().minusHours(2).toDate())

        // Payments which should NOT be fetched
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.NEW)
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.AUTHED)
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.CAPTURED)
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.CREDITED)
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.ANNULLED)
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.FAILED)
        createAdyenOrderPayment(user, order1, RandomStringUtils.random(4), OrderPayment.Status.PENDING)

        // Two payments which should be fetched
        createAdyenOrderPayment(user, order2, RandomStringUtils.random(4), OrderPayment.Status.PENDING)
        createAdyenOrderPayment(user, order3, RandomStringUtils.random(4), OrderPayment.Status.PENDING)


        assert adyenService.getPendingPaymentsToAnnull()?.size() == 2
    }*/
}
