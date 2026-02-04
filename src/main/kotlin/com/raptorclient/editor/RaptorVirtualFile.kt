package com.raptorclient.editor

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.raptorclient.models.RequestItem
import java.io.InputStream
import java.io.OutputStream
import javax.swing.Icon

class RaptorVirtualFile(
    private var request: RequestItem,
) : VirtualFile() {
    private val fileType = RaptorFileType()
    private var modStamp = System.currentTimeMillis()

    fun getRequest(): RequestItem = request

    fun updateRequest(newRequest: RequestItem) {
        request = newRequest
        modStamp = System.currentTimeMillis()
    }

    override fun getName(): @NlsSafe String = "${request.method.name} ${request.name}"

    override fun getFileSystem(): VirtualFileSystem = RaptorFileSystem.getInstance()

    override fun getPath(): String = "raptor://${request.id}"

    override fun isWritable(): Boolean = false

    override fun isDirectory(): Boolean = false

    override fun isValid(): Boolean = true

    override fun getParent(): VirtualFile? = null

    override fun getChildren(): Array<VirtualFile>? = null

    override fun getOutputStream(
        @Suppress("unused") requestor: Any?,
        @Suppress("unused") newModificationStamp: Long,
        @Suppress("unused") newTimeStamp: Long,
    ): OutputStream =
        object : OutputStream() {
            override fun write(b: Int) {}
        }

    override fun contentsToByteArray(): ByteArray = "".toByteArray()

    override fun getTimeStamp(): Long = modStamp

    override fun getModificationStamp(): Long = modStamp

    override fun getLength(): Long = 0

    override fun refresh(
        @Suppress("unused") asynchronous: Boolean,
        @Suppress("unused") recursive: Boolean,
        @Suppress("unused") postRunnable: Runnable?,
    ) {}

    override fun getInputStream(): InputStream = "".byteInputStream()

    override fun getFileType(): FileType = fileType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RaptorVirtualFile) return false
        return request.id == other.request.id
    }

    override fun hashCode(): Int = request.id.hashCode()
}

class RaptorFileType : FileType {
    override fun getName(): String = "RaptorRequest"

    override fun getDescription(): String = "RaptorClient HTTP Request"

    override fun getDefaultExtension(): String = "raptor"

    override fun getIcon(): Icon? = null

    override fun isBinary(): Boolean = false

    override fun isReadOnly(): Boolean = false
}

class RaptorFileSystem : VirtualFileSystem() {
    companion object {
        private val instance = RaptorFileSystem()

        fun getInstance(): RaptorFileSystem = instance
    }

    override fun getProtocol(): String = "raptor"

    override fun findFileByPath(
        @Suppress("unused") path: String,
    ): VirtualFile? = null

    override fun refresh(
        @Suppress("unused") asynchronous: Boolean,
    ) {}

    override fun refreshAndFindFileByPath(
        @Suppress("unused") path: String,
    ): VirtualFile? = null

    override fun addVirtualFileListener(
        @Suppress("unused") listener: com.intellij.openapi.vfs.VirtualFileListener,
    ) {}

    override fun removeVirtualFileListener(
        @Suppress("unused") listener: com.intellij.openapi.vfs.VirtualFileListener,
    ) {}

    override fun deleteFile(
        @Suppress("unused") requestor: Any?,
        @Suppress("unused") vFile: VirtualFile,
    ) {}

    override fun moveFile(
        @Suppress("unused") requestor: Any?,
        @Suppress("unused") vFile: VirtualFile,
        @Suppress("unused") newParent: VirtualFile,
    ) {}

    override fun renameFile(
        @Suppress("unused") requestor: Any?,
        @Suppress("unused") vFile: VirtualFile,
        @Suppress("unused") newName: String,
    ) {}

    override fun createChildFile(
        requestor: Any?,
        vDir: VirtualFile,
        fileName: String,
    ): VirtualFile = throw UnsupportedOperationException()

    override fun createChildDirectory(
        requestor: Any?,
        vDir: VirtualFile,
        dirName: String,
    ): VirtualFile = throw UnsupportedOperationException()

    override fun copyFile(
        requestor: Any?,
        virtualFile: VirtualFile,
        newParent: VirtualFile,
        copyName: String,
    ): VirtualFile = throw UnsupportedOperationException()

    override fun isReadOnly(): Boolean = false
}
