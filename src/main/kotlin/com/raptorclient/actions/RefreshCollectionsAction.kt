package com.raptorclient.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager
import com.raptorclient.toolwindow.RaptorToolWindowPanel

class RefreshCollectionsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("RaptorClient")
        toolWindow?.contentManager?.contents?.forEach { content ->
            val component = content.component
            if (component is RaptorToolWindowPanel) {
                component.repaint()
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
