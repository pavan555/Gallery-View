package com.example.pavan.galleryview

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.recyclical.ViewHolder

data class MyItem(
        val letter: String
)

class MyItemViewHolder(itemView: View) : ViewHolder(itemView) {
    val itemSquare: RectangleView = itemView.findViewById(R.id.itemSquare)
    val itemText: TextView? = itemView.findViewById(R.id.itemText)
    val imageView:ImageView = itemView.findViewById(R.id.imageView)
}

