// language: Kotlin, file: AppDatabase.kt, target: Android 21+
// Room — favoritos, histórico, cache de conteúdo
package com.unitvclone.data.local

import android.content.Context
import androidx.room.*
import com.unitvclone.model.Channel
import com.unitvclone.model.Movie
import com.unitvclone.model.Series
import kotlinx.coroutines.flow.Flow

// --- DAOs ---

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY name ASC")
    fun getAll(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE `group` = :group")
    fun getByGroup(group: String): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<Channel>

    @Upsert
    suspend fun upsertAll(channels: List<Channel>)

    @Query("UPDATE channels SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: String, fav: Boolean)

    @Query("DELETE FROM channels")
    suspend fun clearAll()
}

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY name ASC")
    fun getAll(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE genre = :genre")
    fun getByGenre(genre: String): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE name LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<Movie>

    @Upsert
    suspend fun upsertAll(movies: List<Movie>)

    @Query("UPDATE movies SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: String, fav: Boolean)

    @Query("DELETE FROM movies")
    suspend fun clearAll()
}

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series ORDER BY name ASC")
    fun getAll(): Flow<List<Series>>

    @Query("SELECT * FROM series WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<Series>>

    @Query("SELECT * FROM series WHERE name LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<Series>

    @Upsert
    suspend fun upsertAll(seriesList: List<Series>)

    @Query("UPDATE series SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: String, fav: Boolean)

    @Query("DELETE FROM series")
    suspend fun clearAll()
}

// Histórico de reprodução
@Entity(tableName = "watch_history")
data class WatchHistoryEntry(
    @PrimaryKey val contentId: String,
    val contentName: String,
    val coverUrl: String,
    val streamUrl: String,
    val watchedAt: Long = System.currentTimeMillis(),
    val positionMs: Long = 0L   // retomar de onde parou
)

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 50")
    fun getRecent(): Flow<List<WatchHistoryEntry>>

    @Upsert
    suspend fun upsert(entry: WatchHistoryEntry)

    @Query("SELECT * FROM watch_history WHERE contentId = :id")
    suspend fun getById(id: String): WatchHistoryEntry?

    @Query("DELETE FROM watch_history WHERE contentId = :id")
    suspend fun delete(id: String)
}

// --- Database ---

@Database(
    entities = [Channel::class, Movie::class, Series::class, WatchHistoryEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun watchHistoryDao(): WatchHistoryDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unitv.db"
                ).build().also { instance = it }
            }
    }
}
