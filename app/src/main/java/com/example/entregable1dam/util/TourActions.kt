package com.example.entregable1dam.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.entregable1dam.data.TourPoint

object TourActions {
    fun openDrivingRoute(context: Context, point: TourPoint, previousPoint: TourPoint?) {
        val uri = buildDrivingRouteUri(point, previousPoint)
        val mapsIntent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(mapsIntent)
        } catch (_: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    fun sharePoint(context: Context, point: TourPoint, previousPoint: TourPoint?) {
        val routeUrl = buildDrivingRouteUri(point, previousPoint).toString()
        val text = """
            ITANES Tour - ${point.name}
            ${point.description}
            Ruta en auto: $routeUrl
        """.trimIndent()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Recomendacion ITANES: ${point.name}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir punto turistico"))
    }

    fun openWebPage(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun buildDrivingRouteUri(point: TourPoint, previousPoint: TourPoint?): Uri {
        val origin = previousPoint?.let { "${it.name}, ${it.city}, Peru" }
            ?: AIRPORT_NAME
        val destination = "${point.name}, ${point.city}, Peru"
        return Uri.parse("https://www.google.com/maps/dir/").buildUpon()
            .appendQueryParameter("api", "1")
            .appendQueryParameter("origin", origin)
            .appendQueryParameter("destination", destination)
            .appendQueryParameter("travelmode", "driving")
            .build()
    }

    private const val AIRPORT_NAME =
        "Aeropuerto Internacional Alejandro Velasco Astete, Cusco, Peru"
}
