package com.raptorclient.models

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CollectionTest {
    private lateinit var collection: Collection

    @BeforeEach
    fun setUp() {
        collection = Collection()
    }

    @Nested
    inner class AddOperations {
        @Test
        fun `should add request to collection`() {
            val request = RequestItem(name = "Test Request")

            collection.addRequest(request)

            assertEquals(1, collection.requests.size)
            assertEquals("Test Request", collection.requests[0].name)
        }

        @Test
        fun `should add folder to collection`() {
            val folder = FolderItem(name = "Test Folder")

            collection.addFolder(folder)

            assertEquals(1, collection.folders.size)
            assertEquals("Test Folder", collection.folders[0].name)
        }

        @Test
        fun `should add draft to collection`() {
            val draft = RequestItem(name = "Draft Request")

            collection.addDraft(draft)

            assertEquals(1, collection.drafts.size)
            assertEquals("Draft Request", collection.drafts[0].name)
        }
    }

    @Nested
    inner class RemoveOperations {
        @Test
        fun `should remove request by id`() {
            val request = RequestItem(name = "To Remove")
            collection.addRequest(request)

            collection.removeRequest(request.id)

            assertTrue(collection.requests.isEmpty())
        }

        @Test
        fun `should remove draft by id`() {
            val draft = RequestItem(name = "Draft to Remove")
            collection.addDraft(draft)

            collection.removeRequest(draft.id)

            assertTrue(collection.drafts.isEmpty())
        }

        @Test
        fun `should remove folder and unparent its requests`() {
            val folder = FolderItem(name = "Folder")
            collection.addFolder(folder)

            val request = RequestItem(name = "Child", parentId = folder.id)
            collection.addRequest(request)

            collection.removeFolder(folder.id)

            assertTrue(collection.folders.isEmpty())
            assertNull(collection.requests[0].parentId)
        }

        @Test
        fun `should not throw when removing non-existent request`() {
            assertDoesNotThrow { collection.removeRequest("non-existent-id") }
        }

        @Test
        fun `should not throw when removing non-existent folder`() {
            assertDoesNotThrow { collection.removeFolder("non-existent-id") }
        }
    }

    @Nested
    inner class LookupOperations {
        @Test
        fun `should find request by id`() {
            val request = RequestItem(name = "Findable")
            collection.addRequest(request)

            val found = collection.getRequest(request.id)

            assertNotNull(found)
            assertEquals("Findable", found?.name)
        }

        @Test
        fun `should find draft by id`() {
            val draft = RequestItem(name = "Draft Findable")
            collection.addDraft(draft)

            val found = collection.getRequest(draft.id)

            assertNotNull(found)
            assertEquals("Draft Findable", found?.name)
        }

        @Test
        fun `should return null for non-existent request`() {
            assertNull(collection.getRequest("non-existent"))
        }

        @Test
        fun `should find folder by id`() {
            val folder = FolderItem(name = "Findable Folder")
            collection.addFolder(folder)

            val found = collection.getFolder(folder.id)

            assertNotNull(found)
            assertEquals("Findable Folder", found?.name)
        }

        @Test
        fun `should return null for non-existent folder`() {
            assertNull(collection.getFolder("non-existent"))
        }
    }

    @Nested
    inner class HierarchyOperations {
        @Test
        fun `should get requests in folder`() {
            val folder = FolderItem(name = "Folder")
            collection.addFolder(folder)

            val request1 = RequestItem(name = "In Folder", parentId = folder.id)
            val request2 = RequestItem(name = "Root Level", parentId = null)
            collection.addRequest(request1)
            collection.addRequest(request2)

            val inFolder = collection.getRequestsInFolder(folder.id)

            assertEquals(1, inFolder.size)
            assertEquals("In Folder", inFolder[0].name)
        }

        @Test
        fun `should get root-level requests with null parentId`() {
            val request1 = RequestItem(name = "Root", parentId = null)
            val request2 = RequestItem(name = "Nested", parentId = "some-folder-id")
            collection.addRequest(request1)
            collection.addRequest(request2)

            val rootRequests = collection.getRequestsInFolder(null)

            assertEquals(1, rootRequests.size)
            assertEquals("Root", rootRequests[0].name)
        }

        @Test
        fun `should get subfolders by parentId`() {
            val parent = FolderItem(name = "Parent")
            val child = FolderItem(name = "Child", parentId = parent.id)
            val rootFolder = FolderItem(name = "Root Folder")
            collection.addFolder(parent)
            collection.addFolder(child)
            collection.addFolder(rootFolder)

            val subfolders = collection.getSubFolders(parent.id)

            assertEquals(1, subfolders.size)
            assertEquals("Child", subfolders[0].name)
        }

        @Test
        fun `should get root-level folders`() {
            val rootFolder = FolderItem(name = "Root", parentId = null)
            val childFolder = FolderItem(name = "Child", parentId = "some-id")
            collection.addFolder(rootFolder)
            collection.addFolder(childFolder)

            val rootFolders = collection.getSubFolders(null)

            assertEquals(1, rootFolders.size)
            assertEquals("Root", rootFolders[0].name)
        }
    }
}
