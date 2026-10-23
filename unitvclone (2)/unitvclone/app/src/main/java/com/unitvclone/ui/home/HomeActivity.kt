// language: Kotlin, file: HomeActivity.kt, target: Android 21+
// Tela principal — tabs: Canais Ao Vivo | Filmes | Séries | Animes | Favoritos
package com.unitvclone.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.unitvclone.R
import com.unitvclone.model.Channel
import com.unitvclone.model.ContentType
import com.unitvclone.model.Movie
import com.unitvclone.model.Series
import com.unitvclone.ui.activation.ActivationActivity
import com.unitvclone.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE    = "home_mode"
        const val EXTRA_M3U_URL = "m3u_url"
        const val MODE_XTREAM   = "xtream"
        const val MODE_M3U      = "m3u"
    }

    private lateinit var vm: HomeViewModel
    private lateinit var tabs: TabLayout
    private lateinit var rvContent: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    private val contentAdapter = ContentAdapter(
        onChannelClick = { ch -> openPlayer(ch.streamUrl, ch.name, ch.id, ch.logoUrl) },
        onMovieClick   = { mv -> openPlayer(mv.streamUrl, mv.name, mv.id, mv.coverUrl) },
        onSeriesClick  = { sr -> openSeriesDetail(sr) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tabs        = findViewById(R.id.tabs)
        rvContent   = findViewById(R.id.rvContent)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty     = findViewById(R.id.tvEmpty)

        vm = ViewModelProvider(this)[HomeViewModel::class.java]

        rvContent.layoutManager = GridLayoutManager(this, 4)
        rvContent.adapter = contentAdapter

        setupTabs()
        observeViewModel()

        val mode   = intent.getStringExtra(EXTRA_MODE) ?: MODE_XTREAM
        val m3uUrl = intent.getStringExtra(EXTRA_M3U_URL) ?: ""
        vm.loadAll(mode, m3uUrl)
    }

    private fun setupTabs() {
        listOf("Ao Vivo", "Filmes", "Séries", "Animes", "Favoritos").forEach {
            tabs.addTab(tabs.newTab().setText(it))
        }
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = vm.selectTab(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            vm.uiState.collect { state ->
                progressBar.visibility = if (state.loading) View.VISIBLE else View.GONE
                tvEmpty.visibility     = if (!state.loading && state.items.isEmpty()) View.VISIBLE else View.GONE
                contentAdapter.submitItems(state.items)
            }
        }

        lifecycleScope.launch {
            vm.error.collect { err ->
                if (err != null && err.contains("inativa")) {
                    // Credenciais inválidas — volta para ativação
                    startActivity(Intent(this@HomeActivity, ActivationActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun openPlayer(url: String, title: String, id: String, cover: String) {
        lifecycleScope.launch {
            // Verifica posição salva para retomada
            val db = com.unitvclone.data.local.AppDatabase.get(this@HomeActivity)
            val history = db.watchHistoryDao().getById(id)
            val resumeMs = history?.positionMs ?: 0L

            startActivity(Intent(this@HomeActivity, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_URL, url)
                putExtra(PlayerActivity.EXTRA_TITLE, title)
                putExtra(PlayerActivity.EXTRA_ID, id)
                putExtra(PlayerActivity.EXTRA_COVER, cover)
                putExtra(PlayerActivity.EXTRA_RESUME_MS, resumeMs)
            })
        }
    }

    private fun openSeriesDetail(series: Series) {
        startActivity(Intent(this, SeriesDetailActivity::class.java).apply {
            putExtra(SeriesDetailActivity.EXTRA_SERIES_ID, series.id)
            putExtra(SeriesDetailActivity.EXTRA_SERIES_NAME, series.name)
        })
    }
}
