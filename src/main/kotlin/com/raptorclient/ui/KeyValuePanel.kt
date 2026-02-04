package com.raptorclient.ui

import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.raptorclient.models.KeyValuePair
import java.awt.*
import javax.swing.*
import javax.swing.table.AbstractTableModel

class KeyValuePanel(
    private val title: String,
) : JPanel(BorderLayout()) {
    private val tableModel = KeyValueTableModel()
    private val table = JBTable(tableModel)

    init {
        border = JBUI.Borders.empty(8)

        table.setShowGrid(true)
        table.rowHeight = 28
        table.tableHeader.reorderingAllowed = false

        table.columnModel.getColumn(0).preferredWidth = 40
        table.columnModel.getColumn(0).maxWidth = 40
        table.columnModel.getColumn(1).preferredWidth = 200
        table.columnModel.getColumn(2).preferredWidth = 300
        table.columnModel.getColumn(3).preferredWidth = 200
        table.columnModel.getColumn(4).preferredWidth = 40
        table.columnModel.getColumn(4).maxWidth = 40

        table.columnModel.getColumn(0).cellRenderer = CheckBoxRenderer()
        table.columnModel.getColumn(0).cellEditor = DefaultCellEditor(JBCheckBox())
        table.columnModel.getColumn(4).cellRenderer = ButtonRenderer()
        table.columnModel.getColumn(4).cellEditor = ButtonEditor()

        val scrollPane = JScrollPane(table)
        scrollPane.border = JBUI.Borders.empty()

        val toolbar = createToolbar()

        add(toolbar, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
    }

    private fun createToolbar(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT, 4, 4))

        val addButton = JButton("Add", AllIcons.General.Add)
        addButton.addActionListener {
            tableModel.addRow(KeyValuePair())
        }

        val clearButton = JButton("Clear All", AllIcons.Actions.GC)
        clearButton.addActionListener {
            tableModel.clear()
        }

        panel.add(addButton)
        panel.add(clearButton)

        return panel
    }

    fun setItems(items: MutableList<KeyValuePair>) {
        tableModel.setItems(items)
    }

    fun getItems(): MutableList<KeyValuePair> = tableModel.getItems()

    private inner class KeyValueTableModel : AbstractTableModel() {
        private val columns = arrayOf("", "Key", "Value", "Description", "")
        private val items = mutableListOf<KeyValuePair>()

        override fun getRowCount(): Int = items.size

        override fun getColumnCount(): Int = columns.size

        override fun getColumnName(column: Int): String = columns[column]

        override fun getValueAt(
            rowIndex: Int,
            columnIndex: Int,
        ): Any? {
            val item = items[rowIndex]
            return when (columnIndex) {
                0 -> item.enabled
                1 -> item.key
                2 -> item.value
                3 -> item.description
                4 -> "X"
                else -> null
            }
        }

        override fun setValueAt(
            aValue: Any?,
            rowIndex: Int,
            columnIndex: Int,
        ) {
            val item = items[rowIndex]
            when (columnIndex) {
                0 -> item.enabled = aValue as Boolean
                1 -> item.key = aValue as String
                2 -> item.value = aValue as String
                3 -> item.description = aValue as String
            }
            fireTableCellUpdated(rowIndex, columnIndex)
        }

        override fun isCellEditable(
            rowIndex: Int,
            columnIndex: Int,
        ): Boolean = true

        override fun getColumnClass(columnIndex: Int): Class<*> =
            when (columnIndex) {
                0 -> java.lang.Boolean::class.java
                else -> String::class.java
            }

        fun addRow(item: KeyValuePair) {
            items.add(item)
            fireTableRowsInserted(items.size - 1, items.size - 1)
        }

        fun removeRow(rowIndex: Int) {
            if (rowIndex in 0 until items.size) {
                items.removeAt(rowIndex)
                fireTableRowsDeleted(rowIndex, rowIndex)
            }
        }

        fun clear() {
            val size = items.size
            items.clear()
            if (size > 0) {
                fireTableRowsDeleted(0, size - 1)
            }
        }

        fun setItems(newItems: MutableList<KeyValuePair>) {
            items.clear()
            items.addAll(newItems)
            fireTableDataChanged()
        }

        fun getItems(): MutableList<KeyValuePair> = items.toMutableList()
    }

    private inner class CheckBoxRenderer :
        JBCheckBox(),
        javax.swing.table.TableCellRenderer {
        init {
            horizontalAlignment = SwingConstants.CENTER
        }

        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int,
        ): Component {
            this.isSelected = value as? Boolean ?: false
            background = if (isSelected) table?.selectionBackground else table?.background
            return this
        }
    }

    private inner class ButtonRenderer :
        JButton(),
        javax.swing.table.TableCellRenderer {
        init {
            icon = AllIcons.Actions.Close
            isOpaque = true
            isBorderPainted = false
        }

        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int,
        ): Component {
            background = if (isSelected) table?.selectionBackground else table?.background
            return this
        }
    }

    private inner class ButtonEditor : DefaultCellEditor(JBCheckBox()) {
        private val button = JButton()
        private var currentRow = 0

        init {
            button.icon = AllIcons.Actions.Close
            button.addActionListener {
                fireEditingStopped()
                tableModel.removeRow(currentRow)
            }
        }

        override fun getTableCellEditorComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            row: Int,
            column: Int,
        ): Component {
            currentRow = row
            return button
        }
    }
}
