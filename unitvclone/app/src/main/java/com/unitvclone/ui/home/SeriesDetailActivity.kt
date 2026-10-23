// language: Kotlin, file: SeriesDetailActivity.kt, target: Android 21+
// Detalhe de série — temporadas + episódios
package com.unitvclone.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unitvclone.R
import com.unitvclone.data.xtream.XtreamClient
import com.unitvclone.data.xtream.XtreamEpisode
import com.unitvclone.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class SeriesDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SERIES_ID   = "series_id"
        const val EXTRA_SERIES_NAME = "series_name"
    }

    private lateinit var rvEpisodes: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_series_detail)

        val seriesId   = intent.getStringExtra(EXTRA_SERIES_ID)?.toIntOrNull() ?: return
        val seriesName = intent.getStringExtra(EXTRA_SERIES_NAME) ?: ""

        tvTitle     = findViewById(R.id.tvTitle)
        progressBar = findViewById(R.id.progressBar)
        rvEpisodes  = findViewById(R.id.rvEpisodes)

        tvTitle.text = seriesName
        rvEpisodes.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            try {
                val detail   = XtreamClient.getSeriesDetail(seriesId)
                val episodes = detail.episodes?.values?.flatten() ?: emptyList()
                rvEpisodes.adapter = EpisodeAdapter(episodes) { ep ->
                    val url = XtreamClient.episodeStreamUrl(ep.id, ep.ext)
                    startActivity(Intent(this@SeriesDetailActivity, PlayerActivity::class.java).apply {
                        putExtra(PlayerActivity.EXTRA_URL,   url)
                        putExtra(PlayerActivity.EXTRA_TITLE, "${seriesName} S${ep.season}E${ep.episodeNum}")
                        putExtra(PlayerActivity.EXTRA_ID,    ep.id)
                        putExtra(PlayerActivity.EXTRA_COVER, ep.info?.cover ?: "")
                    })
                }
            } catch (e: Exception) {
                tvTitle.text = "Erro ao carregar: ${e.message}"
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    // Adapter de episódios inline
    private class EpisodeAdapter(
        private val episodes: List<XtreamEpisode>,
        private val onClick: (XtreamEpisode) -> Unit
    ) : RecyclerView.Adapter<EpisodeAdapter.EpVH>() {

        override fun getItemCount() = episodes.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpVH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_episode, parent, false)
            return EpVH(view)
        }

        override fun onBindViewHolder(holder: EpVH, position: Int) {
            val ep = episodes[position]
            holder.tvTitle.text = "S${ep.season}E${ep.episodeNum} — ${ep.title}"
            holder.tvPlot.text  = ep.info?.plot ?: ""
            holder.itemView.setOnClickListener { onClick(ep) }
            holder.itemView.isFocusable = true
        }

        class EpVH(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvEpTitle)
            val tvPlot:  TextView = view.findViewById(R.id.tvEpPlot)
        }
    }
}
