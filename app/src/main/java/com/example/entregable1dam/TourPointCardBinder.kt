package com.example.entregable1dam

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.entregable1dam.data.TourPoint
import com.example.entregable1dam.util.ImageResolver
import com.google.android.material.button.MaterialButton

object TourPointCardBinder {
    fun addCard(
        context: Context,
        inflater: LayoutInflater,
        parent: LinearLayout,
        point: TourPoint,
        onOpen: (TourPoint) -> Unit,
        onRoute: (TourPoint) -> Unit,
        onFavoriteChanged: (TourPoint, Boolean) -> Unit
    ) {
        val card = inflater.inflate(R.layout.item_tour_point, parent, false)
        bind(context, card, point, onOpen, onRoute, onFavoriteChanged)
        parent.addView(card)
    }

    private fun bind(
        context: Context,
        card: View,
        point: TourPoint,
        onOpen: (TourPoint) -> Unit,
        onRoute: (TourPoint) -> Unit,
        onFavoriteChanged: (TourPoint, Boolean) -> Unit
    ) {
        val image = card.findViewById<ImageView>(R.id.imagePlace)
        val favoriteButton = card.findViewById<MaterialButton>(R.id.buttonItemFavorite)

        Glide.with(context)
            .load(ImageResolver.resolve(context, point.imageName))
            .centerCrop()
            .into(image)
        image.contentDescription = "Fotografia de ${point.name}"

        card.findViewById<TextView>(R.id.textItemTitle).text = point.name
        card.findViewById<TextView>(R.id.textItemCity).text = point.city
        card.findViewById<TextView>(R.id.textItemDescription).text = point.description
        card.findViewById<TextView>(R.id.textItemMeta).text = "Auto: ${point.estimatedDrive}"

        updateFavoriteButton(favoriteButton, point.isFavorite)
        favoriteButton.setOnClickListener {
            onFavoriteChanged(point, !point.isFavorite)
        }

        card.setOnClickListener { onOpen(point) }
        card.findViewById<MaterialButton>(R.id.buttonItemOpen).setOnClickListener { onOpen(point) }
        card.findViewById<MaterialButton>(R.id.buttonItemRoute).setOnClickListener { onRoute(point) }
    }

    private fun updateFavoriteButton(button: MaterialButton, favorite: Boolean) {
        button.text = if (favorite) button.context.getString(R.string.saved) else button.context.getString(R.string.save)
        button.setIconResource(if (favorite) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24)
    }
}
