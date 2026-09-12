package com.example.entregable1dam.data

import com.example.entregable1dam.network.WikipediaPage
import java.text.Normalizer
import java.util.Locale

data class WikipediaMergeResult(
    val points: List<TourPoint>,
    val updatedCount: Int
)

object WikipediaTourPointMapper {
    fun merge(
        currentPoints: List<TourPoint>,
        pages: List<WikipediaPage>,
        updatedAt: String
    ): WikipediaMergeResult {
        val currentById = currentPoints.associateBy(TourPoint::id)
        val updates = pages.mapNotNull { page ->
            val pointId = pointIdForTitle(page.title) ?: return@mapNotNull null
            val current = currentById[pointId] ?: return@mapNotNull null
            val description = page.extract?.trim().takeUnless { it.isNullOrBlank() }
            val coordinate = page.coordinates.firstOrNull()
            if (description == null && coordinate == null) return@mapNotNull null

            pointId to current.copy(
                description = description ?: current.description,
                latitude = coordinate?.lat ?: current.latitude,
                longitude = coordinate?.lon ?: current.longitude,
                updatedAt = updatedAt
            )
        }.toMap()

        return WikipediaMergeResult(
            points = currentPoints.map { updates[it.id] ?: it },
            updatedCount = updates.size
        )
    }

    private fun pointIdForTitle(title: String): String? {
        val normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .lowercase(Locale.ROOT)

        return when {
            normalized.startsWith("plaza de armas de cusco") -> "cusco_plaza"
            normalized == "coricancha" -> "qorikancha"
            normalized.startsWith("sacsayhuaman") -> "sacsayhuaman"
            normalized.startsWith("pisac") -> "pisac"
            normalized == "ollantaytambo" -> "ollantaytambo"
            else -> null
        }
    }
}
