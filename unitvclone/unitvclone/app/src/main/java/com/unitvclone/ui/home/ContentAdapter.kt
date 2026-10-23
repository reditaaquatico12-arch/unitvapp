// language: Kotlin, file: ContentAdapter.kt, target: Android 21+
// RecyclerView adapter — grid 4 colunas, três tipos de item
package com.unitvclone.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unitvclone.R
import com.unitvclone.model.Channel
import com.unitvclone.model.Movie
import com.unitvclone.model.Series

class ContentAdapter(
    private val onChannelClick: (Channel) -> Unit,
    private val onMovieClick:   (Movie)   -> Unit,
    private val onSeriesClick:  (Series)  -> Unit
) : RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

    private var items: List<ContentItem> = emptyList()

    fun submitItems(newItems: List<ContentItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content, parent, false)
        return ContentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        when (val item = items[position]) {
            is ContentItem.ChannelItem -> holder.bind(
                name     = item.channel.name,
                imageUrl = item.channel.logoUrl,
                badge    = "AO VIVO",
                onClick  = { onChannelClick(item.channel) }
            )
            is ContentItem.MovieItem -> holder.bind(
                name     = item.movie.name,
                imageUrl = item.movie.coverUrl,
                badge    = item.movie.year,
                onClick  = { onMovieClick(item.movie) }
            )
            is ContentItem.SeriesItem -> holder.bind(
                name     = item.series.name,
                imageUrl = item.series.coverUrl,
                badge    = "SÉRIE",
                onClick  = { onSeriesClick(item.series) }
            )
        }
    }

    class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivCover: ImageView = view.findViewById(R.id.ivCover)
        private val tvName:  TextView  = view.findViewById(R.id.tvName)
        private val tvBadge: TextView  = view.findViewById(R.id.tvBadge)

        fun bind(name: String, imageUrl: String, badge: String, onClick: () -> Unit) {
            tvName.text  = name
            tvBadge.text = badge
            tvBadge.visibility = if (badge.isNotEmpty()) View.VISIBLE else View.GONE
            Glide.with(ivCover)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(ivCover)
            itemView.setOnClickListener { onClick() }
            // Foco para controle remoto
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true
        }
    }
}
