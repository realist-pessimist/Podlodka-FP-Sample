package com.example.podlodka.fpsample.testing.service

import com.example.podlodka.fpsample.testing.Order

interface OrderService {
  fun getPendingOrders(): List<Order>
}
