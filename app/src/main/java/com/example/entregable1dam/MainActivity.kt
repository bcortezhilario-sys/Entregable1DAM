package com.example.entregable1dam

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.entregable1dam.data.ItanesRepository
import com.example.entregable1dam.data.TourPoint
import com.example.entregable1dam.network.WikipediaApiClient
import com.example.entregable1dam.network.WikipediaResponse
import com.example.entregable1dam.util.NetworkStatus
import com.example.entregable1dam.util.TourActions
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var repository: ItanesRepository
    private lateinit var containerPoints: LinearLayout
    private lateinit var textNetworkStatus: TextView
    private lateinit var textRouteSummary: TextView
    private lateinit var buttonSync: MaterialButton
    private var autoRefreshDone = false
    private var refreshCall: Call<WikipediaResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = ItanesRepository(this)
        containerPoints = findViewById(R.id.containerPoints)
        textNetworkStatus = findViewById(R.id.textNetworkStatus)
        textRouteSummary = findViewById(R.id.textRouteSummary)

        findViewById<MaterialButton>(R.id.buttonFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
        buttonSync = findViewById(R.id.buttonSync)
        buttonSync.setOnClickListener {
            refreshFromApi(showMessage = true)
        }
        findViewById<MaterialButton>(R.id.buttonPhotoCredits).setOnClickListener {
            showPhotoCredits()
        }
    }

    override fun onResume() {
        super.onResume()
        updateNetworkStatus()
        if (!autoRefreshDone && NetworkStatus.isOnline(this)) {
            refreshFromApi(showMessage = false)
            autoRefreshDone = true
        }
        render()
    }

    private fun render() {
        val points = repository.getTourPoints()
        textRouteSummary.text = "${points.size} puntos del recorrido disponibles offline"
        containerPoints.removeAllViews()
        points.forEach { point ->
            TourPointCardBinder.addCard(
                context = this,
                inflater = layoutInflater,
                parent = containerPoints,
                point = point,
                onOpen = ::openDetail,
                onRoute = {
                    TourActions.openDrivingRoute(this, it, repository.findPreviousPoint(it.routeOrder))
                },
                onFavoriteChanged = ::changeFavorite
            )
        }
    }

    private fun refreshFromApi(showMessage: Boolean) {
        if (!NetworkStatus.isOnline(this)) {
            updateNetworkStatus()
            if (showMessage) Toast.makeText(this, "Sin conexion. Se mantienen los datos locales.", Toast.LENGTH_SHORT).show()
            return
        }

        if (refreshCall != null) return
        buttonSync.isEnabled = false
        textNetworkStatus.text = getString(R.string.api_syncing)

        refreshCall = WikipediaApiClient.api.getTouristPages(
            titles = WIKIPEDIA_TITLES,
            action = "query",
            format = "json",
            formatVersion = 2,
            redirects = 1,
            properties = "extracts|pageimages|coordinates",
            extractIntro = 1,
            extractPlainText = 1,
            extractSentences = 3,
            pageImageProperties = "thumbnail",
            thumbnailSize = 800
        ).also { call ->
            call.enqueue(object : Callback<WikipediaResponse> {
                override fun onResponse(
                    call: Call<WikipediaResponse>,
                    response: Response<WikipediaResponse>
                ) {
                    val pages = response.body()?.query?.pages
                    if (!response.isSuccessful || pages == null) {
                        finishRefreshWithError(showMessage)
                        return
                    }

                    val updated = repository.refreshFromWikipedia(pages)
                    refreshCall = null
                    buttonSync.isEnabled = true
                    render()
                    textNetworkStatus.setBackgroundResource(R.drawable.bg_status_chip)
                    textNetworkStatus.setTextColor(getColor(R.color.itanes_primary_dark))
                    textNetworkStatus.text = getString(R.string.api_updated, updated)
                    if (showMessage) {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.api_updated, updated),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<WikipediaResponse>, error: Throwable) {
                    if (!call.isCanceled) finishRefreshWithError(showMessage)
                }
            })
        }
    }

    private fun finishRefreshWithError(showMessage: Boolean) {
        refreshCall = null
        buttonSync.isEnabled = true
        textNetworkStatus.setBackgroundResource(R.drawable.bg_warning_chip)
        textNetworkStatus.setTextColor(getColor(R.color.itanes_warning))
        textNetworkStatus.setText(R.string.api_error)
        if (showMessage) {
            Toast.makeText(this, R.string.api_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNetworkStatus() {
        val online = NetworkStatus.isOnline(this)
        textNetworkStatus.setBackgroundResource(if (online) R.drawable.bg_status_chip else R.drawable.bg_warning_chip)
        textNetworkStatus.text = getString(if (online) R.string.online_ready else R.string.offline_mode)
        textNetworkStatus.setTextColor(getColor(if (online) R.color.itanes_primary_dark else R.color.itanes_warning))
    }

    private fun changeFavorite(point: TourPoint, favorite: Boolean) {
        repository.setFavorite(point.id, favorite)
        render()
    }

    private fun openDetail(point: TourPoint) {
        startActivity(DetailActivity.newIntent(this, point.id))
    }

    private fun showPhotoCredits() {
        val labels = resources.getStringArray(R.array.photo_credit_labels)
        val urls = resources.getStringArray(R.array.photo_credit_urls)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.photo_credits)
            .setItems(labels) { _, index -> TourActions.openWebPage(this, urls[index]) }
            .setNegativeButton(R.string.close, null)
            .show()
    }

    override fun onDestroy() {
        refreshCall?.cancel()
        refreshCall = null
        super.onDestroy()
    }

    companion object {
        private const val WIKIPEDIA_TITLES =
            "Plaza de Armas del Cuzco|Coricancha|Sacsayhuaman|P\u00edsac (sitio arqueol\u00f3gico)|Ollantaytambo"
    }
}
