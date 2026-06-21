package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- entities ---

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey val id: String,
    val title: String,
    val timestamp: Long,
    val modelName: String
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long,
    val fileMimeType: String? = null,
    val fileData: String? = null, // e.g., base64 or file URI
    val fileName: String? = null
)

@Entity(tableName = "local_models")
data class LocalModel(
    @PrimaryKey val id: String, // Repo ID like "TheBloke/Llama-2-7B-Chat-GGUF"
    val name: String,
    val sizeGbs: Double,
    val isDownloaded: Boolean,
    val downloadProgress: Int, // 0 to 100
    val activeQuantization: String, // "Q4_K_M", "Q8_0", etc.
    val isLoaded: Boolean = false,
    val downloadSpeedMbs: Double = 0.0,
    val systemPrompt: String? = null
)

@Entity(tableName = "morphic_assets")
data class MorphicAsset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String,
    val modalityType: String, // "TEXT_TO_IMAGE", "TEXT_TO_VIDEO", "IMAGE_TO_VIDEO", "IMAGE_TO_TEXT"
    val mediaUriOrPath: String, // Base64 or local file path
    val timestamp: Long,
    val details: String? = null
)

@Entity(tableName = "csv_dashboards")
data class CsvDashboard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val csvContent: String,
    val rowsCount: Int,
    val timestamp: Long
)

// --- DAOs ---

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(query: String): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)
}

@Dao
interface ModelDao {
    @Query("SELECT * FROM local_models")
    fun getAllModels(): Flow<List<LocalModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: LocalModel)

    @Query("UPDATE local_models SET isLoaded = (id = :modelId)")
    suspend fun setActiveModel(modelId: String)

    @Query("UPDATE local_models SET downloadProgress = :progress, isDownloaded = :isDownloaded, downloadSpeedMbs = :speed WHERE id = :modelId")
    suspend fun updateDownloadStatus(modelId: String, progress: Int, isDownloaded: Boolean, speed: Double)

    @Query("DELETE FROM local_models WHERE id = :modelId")
    suspend fun deleteModel(modelId: String)
}

@Dao
interface MorphicDao {
    @Query("SELECT * FROM morphic_assets ORDER BY timestamp DESC")
    fun getAllAssets(): Flow<List<MorphicAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: MorphicAsset)

    @Query("DELETE FROM morphic_assets WHERE id = :id")
    suspend fun deleteAsset(id: Long)
}

@Dao
interface CsvDao {
    @Query("SELECT * FROM csv_dashboards ORDER BY timestamp DESC")
    fun getAllDashboards(): Flow<List<CsvDashboard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDashboard(dashboard: CsvDashboard)

    @Query("DELETE FROM csv_dashboards WHERE id = :id")
    suspend fun deleteDashboard(id: Long)
}

// --- AppDatabase ---

@Database(
    entities = [
        ChatSession::class,
        ChatMessage::class,
        LocalModel::class,
        MorphicAsset::class,
        CsvDashboard::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun modelDao(): ModelDao
    abstract fun morphicDao(): MorphicDao
    abstract fun csvDao(): CsvDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aegis_local_ai_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
