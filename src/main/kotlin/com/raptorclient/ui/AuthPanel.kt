package com.raptorclient.ui

import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.raptorclient.models.ApiKeyLocation
import com.raptorclient.models.AuthConfig
import com.raptorclient.models.AuthType
import java.awt.*
import javax.swing.*

class AuthPanel : JPanel(BorderLayout()) {
    private val authTypeGroup = ButtonGroup()
    private val noneRadio = JBRadioButton("No Auth")
    private val bearerRadio = JBRadioButton("Bearer Token")
    private val basicRadio = JBRadioButton("Basic Auth")
    private val apiKeyRadio = JBRadioButton("API Key")

    private val bearerTokenField = JBTextField()
    private val basicUsernameField = JBTextField()
    private val basicPasswordField = JBPasswordField()
    private val apiKeyNameField = JBTextField()
    private val apiKeyValueField = JBTextField()
    private val apiKeyLocationCombo = JComboBox(ApiKeyLocation.values())

    private val contentPanel = JPanel(CardLayout())

    private var currentAuth = AuthConfig()

    init {
        border = JBUI.Borders.empty(8)
        setupUI()
    }

    private fun setupUI() {
        val typePanel = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))

        authTypeGroup.add(noneRadio)
        authTypeGroup.add(bearerRadio)
        authTypeGroup.add(basicRadio)
        authTypeGroup.add(apiKeyRadio)

        noneRadio.isSelected = true

        val listener = { updateAuthTypeView() }
        noneRadio.addActionListener { listener() }
        bearerRadio.addActionListener { listener() }
        basicRadio.addActionListener { listener() }
        apiKeyRadio.addActionListener { listener() }

        typePanel.add(noneRadio)
        typePanel.add(bearerRadio)
        typePanel.add(basicRadio)
        typePanel.add(apiKeyRadio)

        val nonePanel = JPanel(BorderLayout())
        nonePanel.add(JLabel("This request does not use any authorization", SwingConstants.CENTER), BorderLayout.CENTER)

        val bearerPanel = createBearerPanel()
        val basicPanel = createBasicPanel()
        val apiKeyPanel = createApiKeyPanel()

        contentPanel.add(nonePanel, "none")
        contentPanel.add(bearerPanel, "bearer")
        contentPanel.add(basicPanel, "basic")
        contentPanel.add(apiKeyPanel, "apikey")

        add(typePanel, BorderLayout.NORTH)
        add(contentPanel, BorderLayout.CENTER)
    }

    private fun createBearerPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val gbc =
            GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                insets = JBUI.insets(4)
            }

        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        panel.add(JLabel("Token:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(bearerTokenField, gbc)

        gbc.gridy = 1
        gbc.weighty = 1.0
        panel.add(Box.createVerticalGlue(), gbc)

        return panel
    }

    private fun createBasicPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val gbc =
            GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                insets = JBUI.insets(4)
            }

        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        panel.add(JLabel("Username:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(basicUsernameField, gbc)

        gbc.gridx = 0
        gbc.gridy = 1
        gbc.weightx = 0.0
        panel.add(JLabel("Password:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(basicPasswordField, gbc)

        gbc.gridy = 2
        gbc.weighty = 1.0
        panel.add(Box.createVerticalGlue(), gbc)

        return panel
    }

    private fun createApiKeyPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val gbc =
            GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                insets = JBUI.insets(4)
            }

        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        panel.add(JLabel("Key:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(apiKeyNameField, gbc)

        gbc.gridx = 0
        gbc.gridy = 1
        gbc.weightx = 0.0
        panel.add(JLabel("Value:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(apiKeyValueField, gbc)

        gbc.gridx = 0
        gbc.gridy = 2
        gbc.weightx = 0.0
        panel.add(JLabel("Add to:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(apiKeyLocationCombo, gbc)

        gbc.gridy = 3
        gbc.weighty = 1.0
        panel.add(Box.createVerticalGlue(), gbc)

        return panel
    }

    private fun updateAuthTypeView() {
        val cardLayout = contentPanel.layout as CardLayout
        when {
            noneRadio.isSelected -> {
                currentAuth.type = AuthType.NONE
                cardLayout.show(contentPanel, "none")
            }
            bearerRadio.isSelected -> {
                currentAuth.type = AuthType.BEARER
                cardLayout.show(contentPanel, "bearer")
            }
            basicRadio.isSelected -> {
                currentAuth.type = AuthType.BASIC
                cardLayout.show(contentPanel, "basic")
            }
            apiKeyRadio.isSelected -> {
                currentAuth.type = AuthType.API_KEY
                cardLayout.show(contentPanel, "apikey")
            }
        }
    }

    fun setAuth(auth: AuthConfig) {
        currentAuth = auth

        when (auth.type) {
            AuthType.NONE -> noneRadio.isSelected = true
            AuthType.BEARER -> bearerRadio.isSelected = true
            AuthType.BASIC -> basicRadio.isSelected = true
            AuthType.API_KEY -> apiKeyRadio.isSelected = true
        }

        bearerTokenField.text = auth.bearerToken
        basicUsernameField.text = auth.basicUsername
        basicPasswordField.text = auth.basicPassword
        apiKeyNameField.text = auth.apiKeyName
        apiKeyValueField.text = auth.apiKeyValue
        apiKeyLocationCombo.selectedItem = auth.apiKeyLocation

        updateAuthTypeView()
    }

    fun getAuth(): AuthConfig {
        currentAuth.bearerToken = bearerTokenField.text
        currentAuth.basicUsername = basicUsernameField.text
        currentAuth.basicPassword = String(basicPasswordField.password)
        currentAuth.apiKeyName = apiKeyNameField.text
        currentAuth.apiKeyValue = apiKeyValueField.text
        currentAuth.apiKeyLocation = apiKeyLocationCombo.selectedItem as ApiKeyLocation
        return currentAuth
    }
}
