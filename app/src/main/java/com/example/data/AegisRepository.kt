package com.example.data

import kotlinx.coroutines.flow.Flow

class AegisRepository(private val db: AppDatabase) {

    private val chatDao = db.chatDao()
    private val modelDao = db.modelDao()
    private val morphicDao = db.morphicDao()
    private val csvDao = db.csvDao()

    // --- Sessions & Chats ---
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun insertSession(session: ChatSession) {
        chatDao.insertSession(session)
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
        chatDao.deleteMessagesForSession(sessionId)
    }

    suspend fun searchMessages(query: String): List<ChatMessage> {
        return chatDao.searchMessages(query)
    }

    suspend fun insertMessage(message: ChatMessage) {
        chatDao.insertMessage(message)
    }

    // --- Models ---
    val allModels: Flow<List<LocalModel>> = modelDao.getAllModels()

    suspend fun insertModel(model: LocalModel) {
        modelDao.insertModel(model)
    }

    suspend fun setActiveModel(modelId: String) {
        modelDao.setActiveModel(modelId)
    }

    suspend fun updateDownloadStatus(modelId: String, progress: Int, isDownloaded: Boolean, speed: Double) {
        modelDao.updateDownloadStatus(modelId, progress, isDownloaded, speed)
    }

    suspend fun deleteModel(modelId: String) {
        modelDao.deleteModel(modelId)
    }

    // --- Morphic Assets ---
    val allAssets: Flow<List<MorphicAsset>> = morphicDao.getAllAssets()

    suspend fun insertAsset(asset: MorphicAsset) {
        morphicDao.insertAsset(asset)
    }

    suspend fun deleteAsset(id: Long) {
        morphicDao.deleteAsset(id)
    }

    // --- CSV Dashboards ---
    val allDashboards: Flow<List<CsvDashboard>> = csvDao.getAllDashboards()

    suspend fun insertDashboard(dashboard: CsvDashboard) {
        csvDao.insertDashboard(dashboard)
    }

    suspend fun deleteDashboard(id: Long) {
        csvDao.deleteDashboard(id)
    }
}
