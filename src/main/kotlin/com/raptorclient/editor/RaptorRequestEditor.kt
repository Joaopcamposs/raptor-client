package com.raptorclient.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.raptorclient.models.RequestItem
import com.raptorclient.ui.RequestEditorPanel
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class RaptorRequestEditor(
    private val project: Project,
    private val virtualFile: RaptorVirtualFile,
) : UserDataHolderBase(),
    FileEditor {
    private val wrapper = JPanel(BorderLayout())
    private var editorPanel: RequestEditorPanel? = null
    private var initialized = false

    override fun getComponent(): JComponent {
        if (!initialized) {
            initialized = true
            SwingUtilities.invokeLater {
                editorPanel = RequestEditorPanel(project, virtualFile.getRequest())
                wrapper.add(editorPanel, BorderLayout.CENTER)
                wrapper.revalidate()
                wrapper.repaint()
            }
        }
        return wrapper
    }

    override fun getPreferredFocusedComponent(): JComponent = wrapper

    override fun getName(): String = "RaptorClient"

    override fun setState(state: FileEditorState) {}

    override fun isModified(): Boolean = editorPanel?.isModified() ?: false

    override fun isValid(): Boolean = true

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

    override fun getCurrentLocation(): FileEditorLocation? = null

    override fun dispose() {}

    override fun getFile(): VirtualFile = virtualFile

    fun getRequest(): RequestItem = editorPanel?.getRequest() ?: virtualFile.getRequest()
}
