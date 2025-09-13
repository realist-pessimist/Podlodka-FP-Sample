package com.example.podlodka.fpsample.testing.service

interface WarehouseService {
  fun getAvailableItems(warehouseId: String): Map<String, Int>
}