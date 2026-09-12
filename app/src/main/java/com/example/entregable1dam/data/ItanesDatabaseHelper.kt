package com.example.entregable1dam.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ItanesDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE tour_points (
                id TEXT PRIMARY KEY,
                route_order INTEGER NOT NULL,
                name TEXT NOT NULL,
                city TEXT NOT NULL,
                description TEXT NOT NULL,
                address TEXT NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                image_name TEXT NOT NULL,
                estimated_drive TEXT NOT NULL,
                schedule TEXT NOT NULL,
                price TEXT NOT NULL,
                tips TEXT NOT NULL,
                updated_at TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE favorites (
                point_id TEXT PRIMARY KEY,
                created_at INTEGER NOT NULL,
                FOREIGN KEY(point_id) REFERENCES tour_points(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        insertTourPoints(db, SeedData.tourPoints)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                "UPDATE tour_points SET estimated_drive = ? WHERE id = ?",
                arrayOf("20 min desde el aeropuerto", "cusco_plaza")
            )
        }
    }

    fun seedIfNeeded() {
        writableDatabase.rawQuery("SELECT COUNT(*) FROM tour_points", null).use { cursor ->
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                insertTourPoints(writableDatabase, SeedData.tourPoints)
            }
        }
    }

    fun replaceTourPoints(points: List<TourPoint>) {
        writableDatabase.beginTransaction()
        try {
            insertTourPoints(writableDatabase, points)
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    private fun insertTourPoints(db: SQLiteDatabase, points: List<TourPoint>) {
        points.forEach { point ->
            db.insertWithOnConflict(
                "tour_points",
                null,
                point.toValues(),
                SQLiteDatabase.CONFLICT_REPLACE
            )
        }
    }

    private fun TourPoint.toValues(): ContentValues = ContentValues().apply {
        put("id", id)
        put("route_order", routeOrder)
        put("name", name)
        put("city", city)
        put("description", description)
        put("address", address)
        put("latitude", latitude)
        put("longitude", longitude)
        put("image_name", imageName)
        put("estimated_drive", estimatedDrive)
        put("schedule", schedule)
        put("price", price)
        put("tips", tips)
        put("updated_at", updatedAt)
    }

    companion object {
        private const val DATABASE_NAME = "itanes_tour.db"
        private const val DATABASE_VERSION = 2
    }
}
