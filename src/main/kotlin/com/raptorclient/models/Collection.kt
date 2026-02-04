package com.raptorclient.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Collection(
    @JsonProperty("folders")
    val folders: MutableList<FolderItem> = mutableListOf(),
    @JsonProperty("requests")
    val requests: MutableList<RequestItem> = mutableListOf(),
    @JsonProperty("drafts")
    val drafts: MutableList<RequestItem> = mutableListOf(),
) {
    fun addRequest(request: RequestItem) {
        requests.add(request)
    }

    fun addFolder(folder: FolderItem) {
        folders.add(folder)
    }

    fun addDraft(request: RequestItem) {
        drafts.add(request)
    }

    fun removeRequest(id: String) {
        requests.removeIf { it.id == id }
        drafts.removeIf { it.id == id }
    }

    fun removeFolder(id: String) {
        folders.removeIf { it.id == id }
        requests.filter { it.parentId == id }.forEach { it.parentId = null }
    }

    fun getRequest(id: String): RequestItem? = requests.find { it.id == id } ?: drafts.find { it.id == id }

    fun getFolder(id: String): FolderItem? = folders.find { it.id == id }

    fun getRequestsInFolder(folderId: String?): List<RequestItem> = requests.filter { it.parentId == folderId }

    fun getSubFolders(parentId: String?): List<FolderItem> = folders.filter { it.parentId == parentId }
}
