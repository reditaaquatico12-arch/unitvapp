// language: Kotlin, file: HomeViewModel.kt, target: Android 21+
package com.unitvclone.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unitvclone.data.local.AppDatabase
import com.unitvclone.data.m3u.M3uParser
import com.unitvclone.data.xtream.XtreamClient
import com.unitvclone.ui.home.HomeActivity
import com.unitvclone.model.Channel
import com.unitvclone.model.Movie
import com.unitvclone.model.Series
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Item genérico para o RecyclerView unificado
sealed class ContentItem {
    data class ChannelItem(val channel: Channel) : ContentItem()
    data class MovieItem(val movie: Movie) : ContentItem()
    data class SeriesItem(val series: Series) : ContentItem()
}

data class HomeUiState(
    val loading: Boolean = false,
    val items: List<ContentItem> = emptyList()
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _error = MutableSharedFlow<String?>()
    val error: SharedFlow<String?> = _error.asSharedFlow()

    private var currentTab = 0

    // Cache local
    private var channels = emptyList<Channel>()
    private var movies   = emptyList<Movie>()
    private var series   = emptyList<Series>()

    fun loadAll(mode: String = HomeActivity.MODE_XTREAM, m3uUrl: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            try {
                if (mode == HomeActivity.MODE_M3U && m3uUrl.isNotEmpty()) {
                    // Modo teste — lista M3U pública, sem login
                    val entries = M3uParser.parseFromUrl(m3uUrl)
                    channels = M3uParser.toChannels(entries)
                    movies   = M3uParser.toMovies(entries)
                    series   = emptyList()
                } else {
                    // Modo Xtream Codes normal
                    val streams = XtreamClient.getLiveStreams(null)
                    channels = streams.map { with(XtreamClient) { it.toChannel() } }
                    db.channelDao().upsertAll(channels)

                    val vods = XtreamClient.getVodStreams(null)
                    movies = vods.map { with(XtreamClient) { it.toMovie() } }
                    db.movieDao().upsertAll(movies)

                    val seriesList = XtreamClient.getSeries(null)
                    series = seriesList.map { with(XtreamClient) { it.toSeries() } }
                    db.seriesDao().upsertAll(series)
                }
                selectTab(currentTab)
            } catch (e: Exception) {
                _error.emit(e.message)
                // Fallback para cache local
                channels = db.channelDao().getAll().first()
                movies   = db.movieDao().getAll().first()
                series   = db.seriesDao().getAll().first()
                selectTab(currentTab)
            }
        }
    }

    fun selectTab(tab: Int) {
        currentTab = tab
        val items: List<ContentItem> = when (tab) {
            0 -> channels.map { ContentItem.ChannelItem(it) }
            1 -> movies.filter { !it.genre.lowercase().contains("anime") }
                       .map { ContentItem.MovieItem(it) }
            2 -> series.filter { !it.genre.lowercase().contains("anime") }
                       .map { ContentItem.SeriesItem(it) }
            3 -> series.filter { it.genre.lowercase().contains("anime") }
                       .map { ContentItem.SeriesItem(it) } +
                 movies.filter { it.genre.lowercase().contains("anime") }
                       .map { ContentItem.MovieItem(it) }
            4 -> channels.filter { it.isFavorite }.map { ContentItem.ChannelItem(it) } +
                 movies.filter { it.isFavorite }.map { ContentItem.MovieItem(it) } +
                 series.filter { it.isFavorite }.map { ContentItem.SeriesItem(it) }
            else -> emptyList()
        }
        _uiState.update { it.copy(loading = false, items = items) }
    }
}
