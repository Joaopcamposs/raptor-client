package com.raptorclient.ui

import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import com.raptorclient.models.BodyType
import com.raptorclient.models.RawBodyType
import com.raptorclient.models.RequestBody
import java.awt.*
import javax.swing.*

class BodyEditorPanel : JPanel(BorderLayout()) {
    private val bodyTypeGroup = ButtonGroup()
    private val noneRadio = JBRadioButton("none")
    private val rawRadio = JBRadioButton("raw")
    private val formDataRadio = JBRadioButton("form-data")
    private val urlEncodedRadio = JBRadioButton("x-www-form-urlencoded")

    private val rawTypeCombo = JComboBox(RawBodyType.values())
    private val rawTextArea = JBTextArea()
    private val formDataPanel = KeyValuePanel("Form Data")
    private val urlEncodedPanel = KeyValuePanel("URL Encoded")

    private val contentPanel = JPanel(CardLayout())

    private var currentBody = RequestBody()

    init {
        border = JBUI.Borders.empty(8)

        setupUI()
        updateBodyTypeView()
    }

    private fun setupUI() {
        val typePanel = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))

        bodyTypeGroup.add(noneRadio)
        bodyTypeGroup.add(rawRadio)
        bodyTypeGroup.add(formDataRadio)
        bodyTypeGroup.add(urlEncodedRadio)

        noneRadio.isSelected = true

        val listener = { updateBodyTypeView() }
        noneRadio.addActionListener { listener() }
        rawRadio.addActionListener { listener() }
        formDataRadio.addActionListener { listener() }
        urlEncodedRadio.addActionListener { listener() }

        rawTypeCombo.addActionListener {
            currentBody.rawType = rawTypeCombo.selectedItem as RawBodyType
        }

        typePanel.add(noneRadio)
        typePanel.add(rawRadio)
        typePanel.add(rawTypeCombo)
        typePanel.add(formDataRadio)
        typePanel.add(urlEncodedRadio)

        val nonePanel = JPanel(BorderLayout())
        nonePanel.add(JLabel("This request does not have a body", SwingConstants.CENTER), BorderLayout.CENTER)

        rawTextArea.font = Font("Monospaced", Font.PLAIN, 12)
        rawTextArea.lineWrap = true

        contentPanel.add(nonePanel, "none")
        contentPanel.add(JBScrollPane(rawTextArea), "raw")
        contentPanel.add(formDataPanel, "form-data")
        contentPanel.add(urlEncodedPanel, "x-www-form-urlencoded")

        add(typePanel, BorderLayout.NORTH)
        add(contentPanel, BorderLayout.CENTER)
    }

    private fun updateBodyTypeView() {
        val cardLayout = contentPanel.layout as CardLayout
        when {
            noneRadio.isSelected -> {
                currentBody.type = BodyType.NONE
                cardLayout.show(contentPanel, "none")
                rawTypeCombo.isVisible = false
            }
            rawRadio.isSelected -> {
                currentBody.type = BodyType.RAW
                cardLayout.show(contentPanel, "raw")
                rawTypeCombo.isVisible = true
            }
            formDataRadio.isSelected -> {
                currentBody.type = BodyType.FORM_DATA
                cardLayout.show(contentPanel, "form-data")
                rawTypeCombo.isVisible = false
            }
            urlEncodedRadio.isSelected -> {
                currentBody.type = BodyType.URL_ENCODED
                cardLayout.show(contentPanel, "x-www-form-urlencoded")
                rawTypeCombo.isVisible = false
            }
        }
    }

    fun setBody(body: RequestBody) {
        currentBody = body

        when (body.type) {
            BodyType.NONE -> noneRadio.isSelected = true
            BodyType.RAW -> rawRadio.isSelected = true
            BodyType.FORM_DATA -> formDataRadio.isSelected = true
            BodyType.URL_ENCODED -> urlEncodedRadio.isSelected = true
            BodyType.BINARY -> noneRadio.isSelected = true
        }

        rawTypeCombo.selectedItem = body.rawType
        rawTextArea.text = body.raw
        formDataPanel.setItems(body.formData)
        urlEncodedPanel.setItems(body.urlEncoded)

        updateBodyTypeView()
    }

    fun getBody(): RequestBody {
        currentBody.raw = rawTextArea.text
        currentBody.rawType = rawTypeCombo.selectedItem as RawBodyType
        currentBody.formData = formDataPanel.getItems()
        currentBody.urlEncoded = urlEncodedPanel.getItems()
        return currentBody
    }
}
