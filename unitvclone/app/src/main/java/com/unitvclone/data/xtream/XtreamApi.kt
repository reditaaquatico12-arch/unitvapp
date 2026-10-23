// language: Kotlin, file: XtreamApi.kt, target: Android 21+
// Xtream Codes é o padrão do mercado IPTV — compatível com 90%+ dos painéis
package com.unitvclone.data.xtream

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// --- DTOs ---

data class XtreamAuthResponse(
    @SerializedName("user_info")  val userInfo: XtreamUserInfo?,
    @SerializedName("server_info") val serverInfo: XtreamServerInfo?
)

data class XtreamUserInfo(
    @SerializedName("username")        val username: String = "",
    @SerializedName("password")        val password: String = "",
    @SerializedName("status")          val status: String = "",
    @SerializedName("exp_date")        val expDate: String = "",
    @SerializedName("max_connections") val maxConnections: String = "",
    @SerializedName("active_cons")     val activeCons: String = ""
)

data class XtreamServerInfo(
    @SerializedName("url")      val url: String = "",
    @SerializedName("port")     val port: String = "",
    @SerializedName("https_port") val httpsPort: String = "",
    @SerializedName("server_protocol") val protocol: String = "http",
    @SerializedName("timezone") val timezone: String = ""
)

data class XtreamStream(
    @SerializedName("stream_id")   val streamId: Int = 0,
    @SerializedName("name")        val name: String = "",
    @SerializedName("stream_icon") val icon: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("epg_channel_id") val epgId: String = "",
    @SerializedName("added")       val added: String = ""
)

data class XtreamVod(
    @SerializedName("stream_id")   val streamId: Int = 0,
    @SerializedName("name")        val name: String = "",
    @SerializedName("stream_icon") val icon: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("rating")      val rating: String = "",
    @SerializedName("added")       val added: String = "",
    @SerializedName("container_extension") val ext: String = "mp4"
)

data class XtreamSeries(
    @SerializedName("series_id")   val seriesId: Int = 0,
    @SerializedName("name")        val name: String = "",
    @SerializedName("cover")       val cover: String = "",
    @SerializedName("plot")        val plot: String = "",
    @SerializedName("genre")       val genre: String = "",
    @SerializedName("rating")      val rating: String = "",
    @SerializedName("category_id") val categoryId: String = ""
)

data class XtreamCategory(
    @SerializedName("category_id")   val id: String = "",
    @SerializedName("category_name") val name: String = "",
    @SerializedName("parent_id")     val parentId: Int = 0
)

data class XtreamSeriesDetail(
    @SerializedName("info")     val info: XtreamSeries?,
    @SerializedName("episodes") val episodes: Map<String, List<XtreamEpisode>>?
)

data class XtreamEpisode(
    @SerializedName("id")            val id: String = "",
    @SerializedName("title")         val title: String = "",
    @SerializedName("episode_num")   val episodeNum: Int = 0,
    @SerializedName("season")        val season: Int = 0,
    @SerializedName("info")          val info: XtreamEpisodeInfo?,
    @SerializedName("container_extension") val ext: String = "mp4"
)

data class XtreamEpisodeInfo(
    @SerializedName("movie_image") val cover: String = "",
    @SerializedName("plot")        val plot: String = ""
)

// --- Interface Retrofit ---

interface XtreamApiService {

    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String
    ): XtreamAuthResponse

    @GET("player_api.php?action=get_live_streams")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null
    ): List<XtreamStream>

    @GET("player_api.php?action=get_live_categories")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String
    ): List<XtreamCategory>

    @GET("player_api.php?action=get_vod_streams")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null
    ): List<XtreamVod>

    @GET("player_api.php?action=get_vod_categories")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String
    ): List<XtreamCategory>

    @GET("player_api.php?action=get_series")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("category_id") categoryId: String? = null
    ): List<XtreamSeries>

    @GET("player_api.php?action=get_series_categories")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String
    ): List<XtreamCategory>

    @GET("player_api.php?action=get_series_info")
    suspend fun getSeriesDetail(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("series_id") seriesId: Int
    ): XtreamSeriesDetail
}
