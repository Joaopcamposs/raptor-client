package com.raptorclient.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.raptorclient.models.*
import com.raptorclient.services.EnvironmentService
import com.raptorclient.services.HttpClientService
import com.raptorclient.services.RequestStorageService
import java.awt.*
import javax.swing.*

class RequestEditorPanel(
    private val project: Project,
    private var request: RequestItem,
) : JPanel(BorderLayout()) {
    private val httpClient: HttpClientService by lazy { HttpClientService() }
    private val storageService: RequestStorageService by lazy { project.getService(RequestStorageService::class.java) }
    private val environmentService: EnvironmentService by lazy { project.getService(EnvironmentService::class.java) }

    private val methodComboBox = JComboBox(HttpMethod.values())
    private val urlField = JBTextField()
    private val sendButton = JButton("Send")
    private val saveButton = JButton("Save")

    private val requestTabs = JBTabbedPane()
    private val responseTabs = JBTabbedPane()

    private val jsonResponseArea = JTextArea()
    private val rawResponseArea = JTextArea()
    private val headersResponseArea = JTextArea()

    private val statusLabel = JLabel()
    private val timeLabel = JLabel()
    private val sizeLabel = JLabel()

    private var paramsPanel: KeyValuePanel? = null
    private var headersPanel: KeyValuePanel? = null
    private var bodyPanel: BodyEditorPanel? = null
    private var authPanel: AuthPanel? = null

    private var modified = false
    private var currentResponse: HttpResponse? = null

    init {
        border = JBUI.Borders.empty(8)
        setupUI()
        loadRequest()
    }

    private fun setupUI() {
        val topPanel = createTopPanel()
        add(topPanel, BorderLayout.NORTH)

        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)
        splitPane.topComponent = createRequestPanel()
        splitPane.bottomComponent = createResponsePanel()
        splitPane.resizeWeight = 0.5
        splitPane.dividerLocation = 300

        add(splitPane, BorderLayout.CENTER)
    }

    private fun createTopPanel(): JPanel {
        val panel = JPanel(BorderLayout(8, 0))
        panel.border = JBUI.Borders.emptyBottom(8)

        methodComboBox.preferredSize = Dimension(100, 32)
        methodComboBox.renderer = MethodComboBoxRenderer()
        methodComboBox.addActionListener {
            request.method = methodComboBox.selectedItem as HttpMethod
            modified = true
        }

        urlField.preferredSize = Dimension(400, 32)
        urlField.emptyText.text = "Enter request URL..."
        urlField.addActionListener { sendRequest() }
        urlField.document.addDocumentListener(
            object : javax.swing.event.DocumentListener {
                override fun insertUpdate(e: javax.swing.event.DocumentEvent?) {
                    request.url = urlField.text
                    modified = true
                }

                override fun removeUpdate(e: javax.swing.event.DocumentEvent?) {
                    request.url = urlField.text
                    modified = true
                }

                override fun changedUpdate(e: javax.swing.event.DocumentEvent?) {
                    request.url = urlField.text
                    modified = true
                }
            },
        )

        sendButton.icon = AllIcons.Actions.Execute
        sendButton.addActionListener { sendRequest() }

        saveButton.icon = AllIcons.Actions.MenuSaveall
        saveButton.addActionListener { saveRequest() }

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 4, 0))
        buttonPanel.add(sendButton)
        buttonPanel.add(saveButton)

        panel.add(methodComboBox, BorderLayout.WEST)
        panel.add(urlField, BorderLayout.CENTER)
        panel.add(buttonPanel, BorderLayout.EAST)

        return panel
    }

    private fun createRequestPanel(): JComponent {
        paramsPanel = KeyValuePanel("Query Parameters")
        headersPanel = KeyValuePanel("Headers")
        bodyPanel = BodyEditorPanel()
        authPanel = AuthPanel()

        requestTabs.addTab("Params", JBScrollPane(paramsPanel))
        requestTabs.addTab("Body", JBScrollPane(bodyPanel))
        requestTabs.addTab("Headers", JBScrollPane(headersPanel))
        requestTabs.addTab("Auth", JBScrollPane(authPanel))

        return requestTabs
    }

    private fun createResponsePanel(): JComponent {
        val panel = JPanel(BorderLayout())

        val statusPanel = createStatusPanel()
        panel.add(statusPanel, BorderLayout.NORTH)

        jsonResponseArea.isEditable = false
        jsonResponseArea.font = Font("Monospaced", Font.PLAIN, 12)

        rawResponseArea.isEditable = false
        rawResponseArea.font = Font("Monospaced", Font.PLAIN, 12)

        headersResponseArea.isEditable = false
        headersResponseArea.font = Font("Monospaced", Font.PLAIN, 12)

        responseTabs.addTab("JSON", JBScrollPane(jsonResponseArea))
        responseTabs.addTab("Raw", JBScrollPane(rawResponseArea))
        responseTabs.addTab("Headers", JBScrollPane(headersResponseArea))

        panel.add(responseTabs, BorderLayout.CENTER)

        return panel
    }

    private fun createStatusPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT, 16, 4))
        panel.border = JBUI.Borders.emptyTop(8)

        statusLabel.text = "Status: --"
        timeLabel.text = "Time: --"
        sizeLabel.text = "Size: --"

        panel.add(statusLabel)
        panel.add(timeLabel)
        panel.add(sizeLabel)

        return panel
    }

    private fun loadRequest() {
        methodComboBox.selectedItem = request.method
        urlField.text = request.url
        paramsPanel?.setItems(request.params)
        headersPanel?.setItems(request.headers)
        bodyPanel?.setBody(request.body)
        authPanel?.setAuth(request.auth)
        modified = false
    }

    private fun collectRequest() {
        request.method = methodComboBox.selectedItem as HttpMethod
        request.url = urlField.text
        request.params = paramsPanel?.getItems() ?: request.params
        request.headers = headersPanel?.getItems() ?: request.headers
        request.body = bodyPanel?.getBody() ?: request.body
        request.auth = authPanel?.getAuth() ?: request.auth
        request.updatedAt = System.currentTimeMillis()
    }

    private fun sendRequest() {
        collectRequest()

        sendButton.isEnabled = false
        sendButton.text = "Sending..."
        statusLabel.text = "Status: Sending..."
        statusLabel.foreground = JBColor.foreground()

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val response = httpClient.executeRequest(request, environmentService)
                currentResponse = response

                SwingUtilities.invokeLater {
                    displayResponse(response)
                    sendButton.isEnabled = true
                    sendButton.text = "Send"
                }
            } catch (ex: Exception) {
                SwingUtilities.invokeLater {
                    statusLabel.text = "Status: Error"
                    statusLabel.foreground = JBColor.RED
                    jsonResponseArea.text = "Error: ${ex.message}"
                    rawResponseArea.text = "Error: ${ex.message}"
                    sendButton.isEnabled = true
                    sendButton.text = "Send"
                }
            }
        }
    }

    private fun displayResponse(response: HttpResponse) {
        val statusColor =
            when {
                response.statusCode in 200..299 -> JBColor(Color(73, 204, 144), Color(73, 204, 144))
                response.statusCode in 300..399 -> JBColor(Color(252, 161, 48), Color(252, 161, 48))
                response.statusCode in 400..499 -> JBColor(Color(249, 62, 62), Color(249, 62, 62))
                response.statusCode >= 500 -> JBColor(Color(249, 62, 62), Color(249, 62, 62))
                else -> JBColor.foreground()
            }

        statusLabel.text = "Status: ${response.statusCode} ${response.statusText}"
        statusLabel.foreground = statusColor
        timeLabel.text = "Time: ${response.formattedTime}"
        sizeLabel.text = "Size: ${response.formattedSize}"

        if (response.contentType.contains("json", ignoreCase = true)) {
            jsonResponseArea.text = formatJson(response.body)
        } else {
            jsonResponseArea.text = response.body
        }

        rawResponseArea.text = response.body

        val headersText = StringBuilder()
        response.headers.forEach { (key, values) ->
            values.forEach { value ->
                headersText.appendLine("$key: $value")
            }
        }
        headersResponseArea.text = headersText.toString()

        responseTabs.selectedIndex = 0
    }

    private fun formatJson(json: String): String =
        try {
            val gson =
                com.google.gson
                    .GsonBuilder()
                    .setPrettyPrinting()
                    .create()
            val element =
                com.google.gson.JsonParser
                    .parseString(json)
            gson.toJson(element)
        } catch (_: Exception) {
            json
        }

    private fun saveRequest() {
        collectRequest()
        storageService.updateRequest(request)
        modified = false
    }

    fun isModified(): Boolean = modified

    fun getRequest(): RequestItem {
        collectRequest()
        return request
    }

    private inner class MethodComboBoxRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            val method = value as? HttpMethod
            if (method != null) {
                text = method.displayName
                foreground = if (isSelected) list?.selectionForeground else Color.decode(method.color)
                font = font.deriveFont(Font.BOLD)
            }
            return this
        }
    }
}
