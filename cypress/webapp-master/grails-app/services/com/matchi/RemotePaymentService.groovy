package com.matchi

import com.matchi.membership.Membership
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import grails.transaction.NotTransactional
import org.hibernate.criterion.CriteriaSpecification

class RemotePaymentService {

    static transactional = false

    /**
     * Returns all the customer objects having a facility for which you can pay remote
     * @param user
     * @return
     */
    List<Customer> getRemotePayableCustomersFor(User user) {
        if(!user) return []

        List<Customer> connectedCustomers = Customer.findAllByUser(user)
        return connectedCustomers.findAll { Customer customer -> customer.facility.hasEnabledRemotePayments() }
    }

    /**
     * Fetches remote payable orders, according to the current logic stated in each article domain class.
     * @param user
     * @return
     */
    List<Order> getRemotePayableOrdersFor(User user) {
        if(!user) return []

        List<Customer> connectedCustomers = getRemotePayableCustomersFor(user)
        List<Order.Article> remotePayableArticles = Order.Article.getRemotePayables()
        if(!connectedCustomers || !remotePayableArticles) return []

        // Fetch potential orders to reduce search space
        List<Order> potentialOrders = getPotentialOrders(connectedCustomers)
        if(!potentialOrders) return []

        // Group by customer to clear away customers having none
        Map<Order.Article, List<Order>> articleOrderMap = mapToArticle(potentialOrders)

        // For each customer, we get the remote payable articles
        return articleOrderMap.collect { Order.Article article, List<Order> orders ->
            return getRemotePayableOrders(article, orders)
        }.flatten()
    }

    /**
     * Fetching payable orders for an article, with a list of potential orders.
     * While we lack Java 8, where you can have static interface methods, this is what we've got.
     * @param article
     * @param orders
     * @return
     */
    List<Order> getRemotePayableOrders(Order.Article article, List<Order> orders) {
        if(!orders) return []

        switch(article) {
            case Order.Article.BOOKING:
                return Booking.getRemotePayablesForOrders(orders)
            case Order.Article.MEMBERSHIP:
                return Membership.getRemotePayablesForOrders(orders)
            default:
                throw new IllegalStateException("Trying to get remote payable orders without implementation")
        }
    }

    /**
     * Method that fetches potentially remote payable orders, without looking at the article.
     * @param customers
     * @return
     */
    List<Order> getPotentialOrders(List<Customer> customers) {
        // To avoid checking against facilities without this set
        customers = customers?.findAll { Customer customer -> customer.facility.hasEnabledRemotePayments() }

        if(!customers) return []

        List<Order.Article> remotePayableArticles = Order.Article.getRemotePayables()
        return Order.createCriteria().list {
            createAlias("payments", "op", CriteriaSpecification.LEFT_JOIN)

            gt("price", new BigDecimal(0))
            inList("article", remotePayableArticles)

            // For each customer, we look at what
            or {
                customers.each { Customer customer ->
                    and {
                        eq("customer", customer)
                        inList("article", customer.facility.getRemotePaymentArticles())
                    }
                }
            }

            or {
                and {
                    eq("status", Order.Status.NEW)
                    isNull("op.id")
                }

                // If someone tries to pay remotely and fails, we need to catch that
                and {
                    eq("status", Order.Status.CANCELLED)
                    eq("op.status", OrderPayment.Status.FAILED)
                }

                // Lost in 3DS verification could lead to this
                and {
                    eq("status", Order.Status.CONFIRMED)
                    eq("op.status", OrderPayment.Status.NEW)
                }
            }

        }
    }

    /**
     * Maps orders to customers
     * @param orders
     * @return
     */
    Map<Order.Article, List<Order>> mapToArticle(List<Order> orders) {
        if(!orders) return [:]

        return orders.groupBy { Order order ->
            return order.article
        }
    }
}
