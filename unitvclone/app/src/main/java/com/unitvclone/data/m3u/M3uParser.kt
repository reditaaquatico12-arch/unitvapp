// language: Kotlin, file: M3uParser.kt, target: Android 21+
// Parseia playlists M3U/M3U8 — suporta EXTINF com atributos TVG e group-title
package com.unitvclone.data.m3u

import com.unitvclone.model.Channel
import com.unitvclone.model.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.UUID

data class M3uEntry(
    val name: String,
    val url: String,
    val logo: String,
    val group: String,
    val tvgId: String,
    val tvgName: String
)

object M3uParser {

    private val client = OkHttpClient()

    // Regex para extrair atributos da linha #EXTINF
    private val attrRegex = Regex("""([\w-]+)="([^"]*?)"""")
    private val nameRegex = Regex(""",(.+)$""")

    suspend fun parseFromUrl(url: String): List<M3uEntry> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        val body = client.newCall(request).execute().body?.byteStream()
            ?: return@withContext emptyList()
        parseStream(body)
    }

    suspend fun parseFromString(content: String): List<M3uEntry> = withContext(Dispatchers.IO) {
        parseLines(content.lines())
    }

    private fun parseStream(stream: InputStream): List<M3uEntry> =
        parseLines(stream.bufferedReader().readLines())

    private fun parseLines(lines: List<String>): List<M3uEntry> {
        val entries = mutableListOf<M3uEntry>()
        var pendingInfo: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("#EXTINF") -> pendingInfo = trimmed
                trimmed.startsWith("http") || trimmed.startsWith("rtmp") ||
                trimmed.startsWith("rtsp") -> {
                    if (pendingInfo != null) {
                        entries.add(buildEntry(pendingInfo, trimmed))
                        pendingInfo = null
                    }
                }
            }
        }
        return entries
    }

    private fun buildEntry(extinf: String, url: String): M3uEntry {
        val attrs = attrRegex.findAll(extinf).associate { it.groupValues[1] to it.groupValues[2] }
        val name = nameRegex.find(extinf)?.groupValues?.get(1)?.trim() ?: "Sem nome"
        return M3uEntry(
            name    = name,
            url     = url,
            logo    = attrs["tvg-logo"] ?: attrs["logo"] ?: "",
            group   = attrs["group-title"] ?: "",
            tvgId   = attrs["tvg-id"] ?: "",
            tvgName = attrs["tvg-name"] ?: name
        )
    }

    // Classifica entries em canais ou filmes pelo group-title
    fun toChannels(entries: List<M3uEntry>): List<Channel> =
        entries.filter { !isVod(it.group) }.map { e ->
            Channel(
                id        = e.tvgId.ifEmpty { UUID.randomUUID().toString() },
                name      = e.name,
                streamUrl = e.url,
                logoUrl   = e.logo,
                group     = e.group,
                epgId     = e.tvgId
            )
        }

    fun toMovies(entries: List<M3uEntry>): List<Movie> =
        entries.filter { isVod(it.group) }.map { e ->
            Movie(
                id        = UUID.randomUUID().toString(),
                name      = e.name,
                streamUrl = e.url,
                coverUrl  = e.logo,
                genre     = e.group
            )
        }

    private fun isVod(group: String): Boolean {
        val g = group.lowercase()
        return g.contains("filme") || g.contains("movie") ||
               g.contains("vod")   || g.contains("serie") ||
               g.contains("anime")
    }
}
