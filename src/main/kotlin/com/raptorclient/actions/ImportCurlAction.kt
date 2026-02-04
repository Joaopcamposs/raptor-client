package com.raptorclient.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import com.raptorclient.editor.RaptorEditorManager
import com.raptorclient.services.CurlParser
import com.raptorclient.services.RequestStorageService
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class ImportCurlAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        importCurl(project)
    }

    companion object {
        fun importCurl(project: com.intellij.openapi.project.Project) {
            val dialog = ImportCurlDialog()
            if (dialog.showAndGet()) {
                val curlCommand = dialog.getCurlCommand()
                if (curlCommand.isNotBlank()) {
                    try {
                        val parser = CurlParser()
                        val request = parser.parse(curlCommand)

                        val storageService = RequestStorageService.getInstance(project)
                        storageService.addDraft(request)
                        RaptorEditorManager.openRequest(project, request)
                    } catch (ex: Exception) {
                        JOptionPane.showMessageDialog(
                            null,
                            "Failed to parse cURL command: ${ex.message}",
                            "Import Error",
                            JOptionPane.ERROR_MESSAGE,
                        )
                    }
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    private class ImportCurlDialog : DialogWrapper(true) {
        private val curlTextArea = JBTextArea()

        init {
            title = "Import cURL Command"
            init()
        }

        override fun createCenterPanel(): JComponent {
            val panel = JPanel(BorderLayout())
            panel.preferredSize = Dimension(600, 300)

            val label = JLabel("Paste your cURL command:")
            label.border = JBUI.Borders.emptyBottom(8)

            curlTextArea.lineWrap = true
            curlTextArea.wrapStyleWord = true
            curlTextArea.font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)

            val scrollPane = JBScrollPane(curlTextArea)

            panel.add(label, BorderLayout.NORTH)
            panel.add(scrollPane, BorderLayout.CENTER)

            return panel
        }

        fun getCurlCommand(): String = curlTextArea.text.trim()
    }
}
