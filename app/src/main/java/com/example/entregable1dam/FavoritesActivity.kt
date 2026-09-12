package com.example.entregable1dam

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.entregable1dam.data.ItanesRepository
import com.example.entregable1dam.data.TourPoint
import com.example.entregable1dam.util.TourActions
import com.google.android.material.button.MaterialButton

class FavoritesActivity : AppCompatActivity() {
    private lateinit var repository: ItanesRepository
    private lateinit var containerFavorites: LinearLayout
    private lateinit var textEmptyFavorites: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favorites)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = ItanesRepository(this)
        containerFavorites = findViewById(R.id.containerFavorites)
        textEmptyFavorites = findViewById(R.id.textEmptyFavorites)
        findViewById<MaterialButton>(R.id.buttonBack).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val favorites = repository.getFavorites()
        textEmptyFavorites.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE
        containerFavorites.removeAllViews()
        favorites.forEach { point ->
            TourPointCardBinder.addCard(
                context = this,
                inflater = layoutInflater,
                parent = containerFavorites,
                point = point,
                onOpen = ::openDetail,
                onRoute = {
                    TourActions.openDrivingRoute(this, it, repository.findPreviousPoint(it.routeOrder))
                },
                onFavoriteChanged = ::changeFavorite
            )
        }
    }

    private fun changeFavorite(point: TourPoint, favorite: Boolean) {
        repository.setFavorite(point.id, favorite)
        render()
    }

    private fun openDetail(point: TourPoint) {
        startActivity(DetailActivity.newIntent(this, point.id))
    }
}
