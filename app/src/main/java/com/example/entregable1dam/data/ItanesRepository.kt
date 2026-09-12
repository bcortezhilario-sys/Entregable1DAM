package com.example.entregable1dam.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.entregable1dam.network.WikipediaPage
import java.time.Instant

class ItanesRepository(private val context: Context) {
    private val databaseHelper = ItanesDatabaseHelper(context.applicationContext)

    init {
        databaseHelper.seedIfNeeded()
    }

    fun getTourPoints(): List<TourPoint> = queryPoints()

    fun getFavorites(): List<TourPoint> = queryPoints(onlyFavorites = true)

    fun findPoint(id: String): TourPoint? =
        queryPoints(pointId = id).firstOrNull()

    fun findPreviousPoint(routeOrder: Int): TourPoint? =
        getTourPoints().lastOrNull { it.routeOrder < routeOrder }

    fun setFavorite(pointId: String, favorite: Boolean) {
        val db = databaseHelper.writableDatabase
        if (favorite) {
            val values = ContentValues().apply {
                put("point_id", pointId)
                put("created_at", System.currentTimeMillis())
            }
            db.insertWithOnConflict("favorites", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
        } else {
            db.delete("favorites", "point_id = ?", arrayOf(pointId))
        }
    }

    fun refreshFromMockApi(): Int {
        val json = context.assets.open(MOCK_API_FILE).bufferedReader().use { it.readText() }
        val points = TourPointJsonParser.parse(json)
        databaseHelper.replaceTourPoints(points)
        return points.size
    }

    fun refreshFromWikipedia(pages: List<WikipediaPage>): Int {
        val result = WikipediaTourPointMapper.merge(
            currentPoints = getTourPoints(),
            pages = pages,
            updatedAt = Instant.now().toString()
        )
        if (result.updatedCount > 0) {
            databaseHelper.replaceTourPoints(result.points)
        }
        return result.updatedCount
    }

    private fun queryPoints(pointId: String? = null, onlyFavorites: Boolean = false): List<TourPoint> {
        val where = buildList {
            if (pointId != null) add("p.id = ?")
            if (onlyFavorites) add("f.point_id IS NOT NULL")
        }.joinToString(separator = " AND ")

        val sql = """
            SELECT p.id, p.route_order, p.name, p.city, p.description, p.address,
                   p.latitude, p.longitude, p.image_name, p.estimated_drive,
                   p.schedule, p.price, p.tips, p.updated_at,
                   CASE WHEN f.point_id IS NULL THEN 0 ELSE 1 END AS is_favorite
            FROM tour_points p
            LEFT JOIN favorites f ON f.point_id = p.id
            ${if (where.isBlank()) "" else "WHERE $where"}
            ORDER BY p.route_order ASC
        """.trimIndent()

        val args = pointId?.let { arrayOf(it) }
        return databaseHelper.readableDatabase.rawQuery(sql, args).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toTourPoint())
                }
            }
        }
    }

    private fun Cursor.toTourPoint(): TourPoint = TourPoint(
        id = getString(getColumnIndexOrThrow("id")),
        routeOrder = getInt(getColumnIndexOrThrow("route_order")),
        name = getString(getColumnIndexOrThrow("name")),
        city = getString(getColumnIndexOrThrow("city")),
        description = getString(getColumnIndexOrThrow("description")),
        address = getString(getColumnIndexOrThrow("address")),
        latitude = getDouble(getColumnIndexOrThrow("latitude")),
        longitude = getDouble(getColumnIndexOrThrow("longitude")),
        imageName = getString(getColumnIndexOrThrow("image_name")),
        estimatedDrive = getString(getColumnIndexOrThrow("estimated_drive")),
        schedule = getString(getColumnIndexOrThrow("schedule")),
        price = getString(getColumnIndexOrThrow("price")),
        tips = getString(getColumnIndexOrThrow("tips")),
        updatedAt = getString(getColumnIndexOrThrow("updated_at")),
        isFavorite = getInt(getColumnIndexOrThrow("is_favorite")) == 1
    )

    companion object {
        private const val MOCK_API_FILE = "mock_tour_points.json"
    }
}
