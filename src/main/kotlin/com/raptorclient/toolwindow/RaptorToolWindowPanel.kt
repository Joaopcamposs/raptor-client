package com.raptorclient.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import com.raptorclient.editor.RaptorEditorManager
import com.raptorclient.models.FolderItem
import com.raptorclient.models.RequestItem
import com.raptorclient.services.RequestStorageService
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.tree.*

class RaptorToolWindowPanel(
    private val project: Project,
) : SimpleToolWindowPanel(true, true) {
    private val storageService: RequestStorageService by lazy {
        project.getService(RequestStorageService::class.java)
    }
    private val tree: Tree
    private val treeModel: DefaultTreeModel
    private val rootNode = DefaultMutableTreeNode("Collections")
    private val draftsNode = DefaultMutableTreeNode("Drafts")
    private var initialized = false

    init {
        treeModel = DefaultTreeModel(rootNode)
        tree = Tree(treeModel)
        tree.isRootVisible = false
        tree.showsRootHandles = true
        tree.cellRenderer = RequestTreeCellRenderer()

        setupTree()
        setupToolbar()

        val scrollPane = JBScrollPane(tree)
        scrollPane.border = JBUI.Borders.empty()
        setContent(scrollPane)
    }

    override fun addNotify() {
        super.addNotify()
        if (!initialized) {
            initialized = true
            SwingUtilities.invokeLater {
                try {
                    storageService.addListener(
                        object : RequestStorageService.CollectionChangeListener {
                            override fun onCollectionChanged() {
                                SwingUtilities.invokeLater { safeRefreshTree() }
                            }
                        },
                    )
                    safeRefreshTree()
                } catch (_: Exception) {
                    // Ignore
                }
            }
        }
    }

    private fun setupTree() {
        tree.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        val path = tree.getPathForLocation(e.x, e.y) ?: return
                        val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
                        val userObject = node.userObject

                        if (userObject is RequestItem) {
                            RaptorEditorManager.openRequest(project, userObject)
                        }
                    }
                }

                override fun mousePressed(e: MouseEvent) {
                    if (e.isPopupTrigger) showPopupMenu(e)
                }

                override fun mouseReleased(e: MouseEvent) {
                    if (e.isPopupTrigger) showPopupMenu(e)
                }
            },
        )
    }

    private fun showPopupMenu(e: MouseEvent) {
        val path = tree.getPathForLocation(e.x, e.y) ?: return
        tree.selectionPath = path
        val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
        val userObject = node.userObject

        val popup = JPopupMenu()

        when (userObject) {
            is RequestItem -> {
                popup.add(
                    JMenuItem("Open").apply {
                        addActionListener { RaptorEditorManager.openRequest(project, userObject) }
                    },
                )
                popup.add(
                    JMenuItem("Duplicate").apply {
                        addActionListener {
                            val duplicated = userObject.duplicate()
                            storageService.addRequest(duplicated)
                        }
                    },
                )
                popup.addSeparator()
                popup.add(
                    JMenuItem("Delete").apply {
                        addActionListener { storageService.deleteRequest(userObject.id) }
                    },
                )
            }
            is FolderItem -> {
                popup.add(
                    JMenuItem("New Request in Folder").apply {
                        addActionListener {
                            val request = RequestItem(parentId = userObject.id)
                            storageService.addRequest(request)
                            RaptorEditorManager.openRequest(project, request)
                        }
                    },
                )
                popup.addSeparator()
                popup.add(
                    JMenuItem("Delete Folder").apply {
                        addActionListener { storageService.deleteFolder(userObject.id) }
                    },
                )
            }
            "Drafts" -> {
                popup.add(
                    JMenuItem("New Draft").apply {
                        addActionListener {
                            val request = RequestItem(name = "Draft")
                            storageService.addDraft(request)
                            RaptorEditorManager.openRequest(project, request)
                        }
                    },
                )
            }
        }

        if (popup.componentCount > 0) {
            popup.show(tree, e.x, e.y)
        }
    }

    private fun setupToolbar() {
        val actionGroup =
            DefaultActionGroup().apply {
                add(
                    object : AnAction("New Request", "Create a new HTTP request", AllIcons.General.Add) {
                        override fun actionPerformed(e: AnActionEvent) {
                            val request = RequestItem()
                            storageService.addDraft(request)
                            RaptorEditorManager.openRequest(project, request)
                        }
                    },
                )
                add(
                    object : AnAction("New Folder", "Create a new folder", AllIcons.Nodes.Folder) {
                        override fun actionPerformed(e: AnActionEvent) {
                            val name =
                                JOptionPane.showInputDialog(
                                    this@RaptorToolWindowPanel,
                                    "Folder name:",
                                    "New Folder",
                                    JOptionPane.PLAIN_MESSAGE,
                                )
                            if (!name.isNullOrBlank()) {
                                storageService.addFolder(FolderItem(name = name))
                            }
                        }
                    },
                )
                add(
                    object : AnAction("Import cURL", "Import request from cURL command", AllIcons.ToolbarDecorator.Import) {
                        override fun actionPerformed(e: AnActionEvent) {
                            com.raptorclient.actions
                                .ImportCurlAction()
                                .actionPerformed(e)
                        }
                    },
                )
                addSeparator()
                add(
                    object : AnAction("Refresh", "Refresh collections", AllIcons.Actions.Refresh) {
                        override fun actionPerformed(e: AnActionEvent) {
                            safeRefreshTree()
                        }
                    },
                )
            }

        val toolbar = ActionManager.getInstance().createActionToolbar("RaptorToolbar", actionGroup, true)
        toolbar.targetComponent = this
        setToolbar(toolbar.component)
    }

    private fun safeRefreshTree() {
        try {
            refreshTree()
        } catch (_: Exception) {
            // Ignore errors during refresh
        }
    }

    private fun refreshTree() {
        rootNode.removeAllChildren()

        val collection = storageService.getCollection()

        draftsNode.removeAllChildren()
        draftsNode.userObject = "Drafts"
        collection.drafts.forEach { draft ->
            draftsNode.add(DefaultMutableTreeNode(draft))
        }
        rootNode.add(draftsNode)

        fun addFolderContents(
            parentNode: DefaultMutableTreeNode,
            parentId: String?,
        ) {
            collection.getSubFolders(parentId).forEach { folder ->
                val folderNode = DefaultMutableTreeNode(folder)
                addFolderContents(folderNode, folder.id)
                collection.getRequestsInFolder(folder.id).forEach { request ->
                    folderNode.add(DefaultMutableTreeNode(request))
                }
                parentNode.add(folderNode)
            }
        }

        addFolderContents(rootNode, null)

        collection.getRequestsInFolder(null).forEach { request ->
            rootNode.add(DefaultMutableTreeNode(request))
        }

        treeModel.reload()
        expandAll()
    }

    private fun expandAll() {
        val rowCount = tree.rowCount
        for (row in 0 until rowCount) {
            tree.expandRow(row)
        }
    }

    private inner class RequestTreeCellRenderer : DefaultTreeCellRenderer() {
        override fun getTreeCellRendererComponent(
            tree: JTree,
            value: Any?,
            sel: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean,
        ): Component {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

            val node = value as? DefaultMutableTreeNode ?: return this
            val userObject = node.userObject

            when (userObject) {
                is RequestItem -> {
                    val methodColor =
                        try {
                            Color.decode(userObject.method.color)
                        } catch (_: Exception) {
                            Color.GRAY
                        }
                    text =
                        "<html><b><font color='${String.format(
                            "#%06x",
                            methodColor.rgb and 0xFFFFFF,
                        )}'>${userObject.method.name}</font></b> ${userObject.name}</html>"
                    icon = AllIcons.Nodes.DataTables
                }
                is FolderItem -> {
                    icon = AllIcons.Nodes.Folder
                    text = userObject.name
                }
                "Drafts" -> {
                    icon = AllIcons.Nodes.Folder
                    text = "Drafts"
                }
            }

            return this
        }
    }
}
