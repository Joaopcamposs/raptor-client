package com.raptorclient.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class RaptorRequestEditorProvider :
    FileEditorProvider,
    DumbAware {
    override fun accept(
        project: Project,
        file: VirtualFile,
    ): Boolean = file is RaptorVirtualFile

    override fun createEditor(
        project: Project,
        file: VirtualFile,
    ): FileEditor {
        val raptorFile = file as RaptorVirtualFile
        return RaptorRequestEditor(project, raptorFile)
    }

    override fun getEditorTypeId(): String = "RaptorRequestEditor"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}
