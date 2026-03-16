package com.raptorclient.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FolderItemTest {
    @Test
    fun `should create with default values`() {
        val folder = FolderItem()

        assertEquals("New Folder", folder.name)
        assertNull(folder.parentId)
        assertTrue(folder.expanded)
        assertNotNull(folder.id)
        assertTrue(folder.id.isNotBlank())
        assertTrue(folder.createdAt > 0)
    }

    @Test
    fun `should create with custom name`() {
        val folder = FolderItem(name = "My Folder")
        assertEquals("My Folder", folder.name)
    }

    @Test
    fun `should support parent-child hierarchy`() {
        val parent = FolderItem(name = "Parent")
        val child = FolderItem(name = "Child", parentId = parent.id)

        assertEquals(parent.id, child.parentId)
    }

    @Test
    fun `should generate unique IDs`() {
        val folder1 = FolderItem()
        val folder2 = FolderItem()

        assertNotEquals(folder1.id, folder2.id)
    }
}
