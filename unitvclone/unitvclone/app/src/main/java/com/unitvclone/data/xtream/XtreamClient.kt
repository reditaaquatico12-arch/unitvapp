// language: Kotlin, file: XtreamClient.kt, target: Android 21+
// Singleton que mantém sessão Xtream e constrói URLs de stream
package com.unitvclone.data.xtream

import com.unitvclone.model.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object XtreamClient {

    private var api: XtreamApiService? = null
    private var baseUrl: String = ""
    private var username: String = ""
    private var password: String = ""
    var serverInfo: XtreamServerInfo? = null
        private set

    fun configure(server: String, user: String, pass: String) {
        baseUrl = if (server.endsWith("/")) server else "$server/"
        username = user
        password = pass
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamApiService::class.java)
    }

    // Retorna true se o login for válido
    suspend fun authenticate(): Result<XtreamInfo> = runCatching {
        val resp = requireApi().authenticate(username, password)
        serverInfo = resp.serverInfo
        val info = resp.userInfo ?: error("resposta vazia")
        if (info.status != "Active") error("conta inativa ou expirada")
        XtreamInfo(
            serverUrl       = baseUrl,
            username        = username,
            password        = password,
            expiryDate      = info.expDate,
            maxConnections  = info.maxConnections,
            activeConnections = info.activeCons
        )
    }

    // --- Live TV ---
    suspend fun getLiveCategories()         = requireApi().getLiveCategories(username, password)
    suspend fun getLiveStreams(cat: String?) = requireApi().getLiveStreams(username, password, cat)

    // URL do stream ao vivo: http://server/live/user/pass/streamId.m3u8
    fun liveStreamUrl(streamId: Int, ext: String = "m3u8") =
        "${baseUrl}live/$username/$password/$streamId.$ext"

    // --- VOD ---
    suspend fun getVodCategories()           = requireApi().getVodCategories(username, password)
    suspend fun getVodStreams(cat: String?)   = requireApi().getVodStreams(username, password, cat)

    // URL do filme: http://server/movie/user/pass/streamId.ext
    fun vodStreamUrl(streamId: Int, ext: String = "mp4") =
        "${baseUrl}movie/$username/$password/$streamId.$ext"

    // --- Séries ---
    suspend fun getSeriesCategories()          = requireApi().getSeriesCategories(username, password)
    suspend fun getSeries(cat: String?)        = requireApi().getSeries(username, password, cat)
    suspend fun getSeriesDetail(id: Int)       = requireApi().getSeriesDetail(username, password, id)

    // URL do episódio: http://server/series/user/pass/episodeId.ext
    fun episodeStreamUrl(episodeId: String, ext: String = "mp4") =
        "${baseUrl}series/$username/$password/$episodeId.$ext"

    // Mapeia DTOs para modelos internos
    fun XtreamStream.toChannel() = Channel(
        id        = streamId.toString(),
        name      = name,
        streamUrl = liveStreamUrl(streamId),
        logoUrl   = icon,
        group     = categoryId,
        epgId     = epgId
    )

    fun XtreamVod.toMovie() = Movie(
        id        = streamId.toString(),
        name      = name,
        streamUrl = vodStreamUrl(streamId, ext),
        coverUrl  = icon,
        genre     = categoryId
    )

    fun XtreamSeries.toSeries() = Series(
        id       = seriesId.toString(),
        name     = name,
        coverUrl = cover,
        plot     = plot,
        genre    = genre,
        rating   = rating
    )

    private fun requireApi() = api ?: error("XtreamClient não configurado — chame configure() primeiro")
}
