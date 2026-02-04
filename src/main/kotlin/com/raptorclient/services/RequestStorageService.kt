package com.raptorclient.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.raptorclient.models.Collection
import com.raptorclient.models.FolderItem
import com.raptorclient.models.RequestItem

@Service(Service.Level.PROJECT)
@State(
    name = "RaptorClientStorage",
    storages = [Storage("raptorClient.xml")],
)
class RequestStorageService(
    @Suppress("unused") private val project: Project,
) : PersistentStateComponent<RequestStorageService.State> {
    private val objectMapper: ObjectMapper by lazy {
        ObjectMapper().registerModule(KotlinModule.Builder().build())
    }
    private var collection = Collection()
    private val listeners = mutableListOf<CollectionChangeListener>()

    data class State(
        var collectionJson: String = "{\"folders\":[],\"requests\":[],\"drafts\":[]}",
    )

    private var myState = State()

    override fun getState(): State {
        myState.collectionJson = objectMapper.writeValueAsString(collection)
        return myState
    }

    override fun loadState(state: State) {
        myState = state
        try {
            collection = objectMapper.readValue(state.collectionJson)
        } catch (_: Exception) {
            collection = Collection()
        }
    }

    fun getCollection(): Collection = collection

    fun addRequest(request: RequestItem) {
        collection.addRequest(request)
        notifyListeners()
    }

    fun addFolder(folder: FolderItem) {
        collection.addFolder(folder)
        notifyListeners()
    }

    fun addDraft(request: RequestItem) {
        collection.addDraft(request)
        notifyListeners()
    }

    fun updateRequest(request: RequestItem) {
        val index = collection.requests.indexOfFirst { it.id == request.id }
        if (index >= 0) {
            collection.requests[index] = request
        } else {
            val draftIndex = collection.drafts.indexOfFirst { it.id == request.id }
            if (draftIndex >= 0) {
                collection.drafts[draftIndex] = request
            }
        }
        notifyListeners()
    }

    fun deleteRequest(id: String) {
        collection.removeRequest(id)
        notifyListeners()
    }

    fun deleteFolder(id: String) {
        collection.removeFolder(id)
        notifyListeners()
    }

    fun getRequest(id: String): RequestItem? = collection.getRequest(id)

    fun getFolder(id: String): FolderItem? = collection.getFolder(id)

    fun moveToDrafts(request: RequestItem) {
        collection.requests.removeIf { it.id == request.id }
        if (collection.drafts.none { it.id == request.id }) {
            collection.drafts.add(request)
        }
        notifyListeners()
    }

    fun moveToCollection(
        request: RequestItem,
        folderId: String? = null,
    ) {
        collection.drafts.removeIf { it.id == request.id }
        request.parentId = folderId
        if (collection.requests.none { it.id == request.id }) {
            collection.requests.add(request)
        }
        notifyListeners()
    }

    fun addListener(listener: CollectionChangeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: CollectionChangeListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.onCollectionChanged() }
    }

    interface CollectionChangeListener {
        fun onCollectionChanged()
    }

    companion object {
        fun getInstance(project: Project): RequestStorageService = project.getService(RequestStorageService::class.java)
    }
}
