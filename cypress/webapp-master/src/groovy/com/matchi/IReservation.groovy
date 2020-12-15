package com.matchi

import com.matchi.orders.Order
import com.matchi.payment.ArticleType

/**
 * Interface for different types of reservation in time, such as bookings, activities etc
 */
interface IReservation {
    Order getOrder()
    Payment getPayment()
    Long getId()
    ArticleType getArticleType()
    Date getDate()
    Facility getFacility()
}