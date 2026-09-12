package com.example.entregable1dam.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItanesApi {
    @GET("w/api.php")
    fun getTouristPages(
        @Query("titles") titles: String,
        @Query("action") action: String,
        @Query("format") format: String,
        @Query("formatversion") formatVersion: Int,
        @Query("redirects") redirects: Int,
        @Query("prop") properties: String,
        @Query("exintro") extractIntro: Int,
        @Query("explaintext") extractPlainText: Int,
        @Query("exsentences") extractSentences: Int,
        @Query("piprop") pageImageProperties: String,
        @Query("pithumbsize") thumbnailSize: Int
    ): Call<WikipediaResponse>
}

data class WikipediaResponse(
    val query: WikipediaQuery? = null
)

data class WikipediaQuery(
    val pages: List<WikipediaPage> = emptyList()
)

data class WikipediaPage(
    val title: String = "",
    val extract: String? = null,
    val thumbnail: WikipediaThumbnail? = null,
    val coordinates: List<WikipediaCoordinate> = emptyList()
)

data class WikipediaThumbnail(
    val source: String = "",
    val width: Int = 0,
    val height: Int = 0
)

data class WikipediaCoordinate(
    val lat: Double,
    val lon: Double
)
