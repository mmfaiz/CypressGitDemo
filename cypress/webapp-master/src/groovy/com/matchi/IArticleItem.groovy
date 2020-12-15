package com.matchi

import com.matchi.orders.Order

interface IArticleItem {
    void replaceOrderAndSave(Order order)
}