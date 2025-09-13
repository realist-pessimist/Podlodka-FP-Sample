package com.example.podlodka.fpsample.testing.service

import com.example.podlodka.fpsample.testing.model.Order

interface OrderService {
  fun getPendingOrders(): List<Order>
}
