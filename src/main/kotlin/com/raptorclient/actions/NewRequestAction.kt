package com.raptorclient.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.raptorclient.editor.RaptorEditorManager
import com.raptorclient.models.RequestItem
import com.raptorclient.services.RequestStorageService

class NewRequestAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val storageService = RequestStorageService.getInstance(project)

        val request = RequestItem()
        storageService.addDraft(request)
        RaptorEditorManager.openRequest(project, request)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
