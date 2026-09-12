package com.example.entregable1dam.data

import org.json.JSONObject

object TourPointJsonParser {
    fun parse(json: String): List<TourPoint> {
        val root = JSONObject(json)
        val updatedAt = root.optString("updatedAt", System.currentTimeMillis().toString())
        val points = root.getJSONArray("points")

        return List(points.length()) { index ->
            val item = points.getJSONObject(index)
            TourPoint(
                id = item.getString("id"),
                routeOrder = item.optInt("order", index + 1),
                name = item.getString("name"),
                city = item.getString("city"),
                description = item.getString("description"),
                address = item.getString("address"),
                latitude = item.getDouble("latitude"),
                longitude = item.getDouble("longitude"),
                imageName = item.getString("imageName"),
                estimatedDrive = item.getString("estimatedDrive"),
                schedule = item.getString("schedule"),
                price = item.getString("price"),
                tips = item.getString("tips"),
                updatedAt = updatedAt
            )
        }
    }
}
