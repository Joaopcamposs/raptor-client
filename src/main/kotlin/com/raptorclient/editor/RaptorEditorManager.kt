package com.raptorclient.editor

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.raptorclient.models.RequestItem

object RaptorEditorManager {
    private val openRequests = mutableMapOf<String, RaptorVirtualFile>()

    fun openRequest(
        project: Project,
        request: RequestItem,
    ) {
        val existingFile = openRequests[request.id]
        if (existingFile != null) {
            FileEditorManager.getInstance(project).openFile(existingFile, true)
            return
        }

        val virtualFile = RaptorVirtualFile(request)
        openRequests[request.id] = virtualFile
        FileEditorManager.getInstance(project).openFile(virtualFile, true)
    }

    fun closeRequest(
        project: Project,
        requestId: String,
    ) {
        val file = openRequests.remove(requestId)
        if (file != null) {
            FileEditorManager.getInstance(project).closeFile(file)
        }
    }

    fun getVirtualFile(requestId: String): RaptorVirtualFile? = openRequests[requestId]

    fun updateRequest(
        requestId: String,
        request: RequestItem,
    ) {
        openRequests[requestId]?.updateRequest(request)
    }
}
