package com.example.entregable1dam.data

data class TourPoint(
    val id: String,
    val routeOrder: Int,
    val name: String,
    val city: String,
    val description: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val imageName: String,
    val estimatedDrive: String,
    val schedule: String,
    val price: String,
    val tips: String,
    val updatedAt: String,
    val isFavorite: Boolean = false
)
