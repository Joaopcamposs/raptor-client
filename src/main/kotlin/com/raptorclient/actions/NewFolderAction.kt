package com.raptorclient.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.raptorclient.models.FolderItem
import com.raptorclient.services.RequestStorageService
import javax.swing.JOptionPane

class NewFolderAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val storageService = RequestStorageService.getInstance(project)

        val name =
            JOptionPane.showInputDialog(
                null,
                "Folder name:",
                "New Folder",
                JOptionPane.PLAIN_MESSAGE,
            )

        if (!name.isNullOrBlank()) {
            storageService.addFolder(FolderItem(name = name))
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
