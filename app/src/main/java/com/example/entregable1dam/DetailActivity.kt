package com.example.entregable1dam

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.entregable1dam.data.ItanesRepository
import com.example.entregable1dam.data.TourPoint
import com.example.entregable1dam.util.ImageResolver
import com.example.entregable1dam.util.TourActions
import com.google.android.material.button.MaterialButton

class DetailActivity : AppCompatActivity() {
    private lateinit var repository: ItanesRepository
    private lateinit var point: TourPoint
    private lateinit var favoriteButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = ItanesRepository(this)
        val pointId = intent.getStringExtra(EXTRA_POINT_ID)
        val currentPoint = pointId?.let(repository::findPoint)
        if (currentPoint == null) {
            finish()
            return
        }
        point = currentPoint

        favoriteButton = findViewById(R.id.buttonDetailFavorite)
        findViewById<MaterialButton>(R.id.buttonBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.buttonDetailRoute).setOnClickListener {
            TourActions.openDrivingRoute(this, point, repository.findPreviousPoint(point.routeOrder))
        }
        favoriteButton.setOnClickListener {
            repository.setFavorite(point.id, !point.isFavorite)
            point = repository.findPoint(point.id) ?: point
            updateFavoriteButton()
        }
        findViewById<MaterialButton>(R.id.buttonDetailShare).setOnClickListener {
            TourActions.sharePoint(this, point, repository.findPreviousPoint(point.routeOrder))
        }

        bindPoint()
    }

    private fun bindPoint() {
        findViewById<TextView>(R.id.textDetailHeader).text = "Punto ${point.routeOrder} de 5"
        findViewById<TextView>(R.id.textDetailTitle).text = point.name
        findViewById<TextView>(R.id.textDetailCity).text = point.city
        findViewById<TextView>(R.id.textDetailDescription).text = point.description
        findViewById<TextView>(R.id.textDetailInfo).text = """
            Direccion: ${point.address}
            Horario: ${point.schedule}
            Ingreso: ${point.price}
            Auto: ${point.estimatedDrive}
        """.trimIndent()
        findViewById<TextView>(R.id.textDetailTips).text = "Recomendacion: ${point.tips}"
        val detailImage = findViewById<ImageView>(R.id.imageDetail)
        detailImage.contentDescription = "Fotografia de ${point.name}"
        Glide.with(this)
            .load(ImageResolver.resolve(this, point.imageName))
            .centerCrop()
            .into(detailImage)
        updateFavoriteButton()
    }

    private fun updateFavoriteButton() {
        favoriteButton.text = getString(if (point.isFavorite) R.string.saved else R.string.save)
        favoriteButton.setIconResource(if (point.isFavorite) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24)
    }

    companion object {
        private const val EXTRA_POINT_ID = "extra_point_id"

        fun newIntent(context: Context, pointId: String): Intent =
            Intent(context, DetailActivity::class.java).putExtra(EXTRA_POINT_ID, pointId)
    }
}
