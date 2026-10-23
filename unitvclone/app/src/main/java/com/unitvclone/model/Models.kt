// language: Kotlin, file: Models.kt, target: Android 21+
package com.unitvclone.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ContentType { LIVE, MOVIE, SERIES, ANIME }

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String = "",
    val group: String = "",
    val epgId: String = "",
    val isFavorite: Boolean = false
)

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val id: String,
    val name: String,
    val streamUrl: String,
    val coverUrl: String = "",
    val plot: String = "",
    val genre: String = "",
    val year: String = "",
    val rating: String = "",
    val duration: String = "",
    val isFavorite: Boolean = false
)

@Entity(tableName = "series")
data class Series(
    @PrimaryKey val id: String,
    val name: String,
    val coverUrl: String = "",
    val plot: String = "",
    val genre: String = "",
    val year: String = "",
    val rating: String = "",
    val isFavorite: Boolean = false
)

data class Episode(
    val id: String,
    val seriesId: String,
    val title: String,
    val season: Int,
    val episodeNum: Int,
    val streamUrl: String,
    val coverUrl: String = "",
    val plot: String = ""
)

data class EpgEntry(
    val channelId: String,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long
)

// Resposta genérica do Xtream
data class XtreamInfo(
    val serverUrl: String,
    val username: String,
    val password: String,
    val expiryDate: String = "",
    val maxConnections: String = "",
    val activeConnections: String = ""
)
