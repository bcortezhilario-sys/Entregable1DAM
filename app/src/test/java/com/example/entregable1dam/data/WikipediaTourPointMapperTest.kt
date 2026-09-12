package com.example.entregable1dam.data

import com.example.entregable1dam.network.WikipediaCoordinate
import com.example.entregable1dam.network.WikipediaPage
import org.junit.Assert.assertEquals
import org.junit.Test

class WikipediaTourPointMapperTest {
    @Test
    fun merge_updatesApiFields_andPreservesItanesFields() {
        val current = SeedData.tourPoints
        val page = WikipediaPage(
            title = "Coricancha",
            extract = "Descripcion recibida desde Wikipedia.",
            coordinates = listOf(WikipediaCoordinate(-13.52, -71.97))
        )

        val result = WikipediaTourPointMapper.merge(current, listOf(page), "api-test")
        val updated = result.points.first { it.id == "qorikancha" }

        assertEquals(1, result.updatedCount)
        assertEquals("Descripcion recibida desde Wikipedia.", updated.description)
        assertEquals(-13.52, updated.latitude, 0.0)
        assertEquals(-71.97, updated.longitude, 0.0)
        assertEquals("photo_qorikancha", updated.imageName)
        assertEquals("08:30 - 17:30", updated.schedule)
        assertEquals("api-test", updated.updatedAt)
    }
}
