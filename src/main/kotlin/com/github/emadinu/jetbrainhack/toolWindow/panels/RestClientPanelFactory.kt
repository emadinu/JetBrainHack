package com.github.emadinu.jetbrainhack.toolWindow.panels


import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout // <- add this missing import!
import javax.swing.JPanel
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JTextArea
import javax.swing.JComboBox
import javax.swing.JTable
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.BorderFactory
import javax.swing.UIManager
import javax.swing.JSplitPane
import com.intellij.util.ui.JBUI
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import java.awt.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.concurrent.thread

class RestClientPanelFactory {

    private fun createResponsePanel(
        responseBodyTextArea: JTextArea,
        responseHeadersTextArea: JTextArea,
        statusLabel: JLabel,
        timeLabel: JLabel,
        sizeLabel: JLabel
    ): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(5)

        // Response tabs
        val responseTabs = JBTabbedPane()

        // Response body tab
        val bodyScrollPane = JBScrollPane(responseBodyTextArea)
        responseTabs.addTab("Response", bodyScrollPane)

        // Headers tab
        val headersScrollPane = JBScrollPane(responseHeadersTextArea)
        responseTabs.addTab("Headers", headersScrollPane)

        // Status panel
        val statusPanel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints()

        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.weightx = 0.33
        statusPanel.add(statusLabel, constraints)

        constraints.gridx = 1
        constraints.gridy = 0
        constraints.weightx = 0.33
        statusPanel.add(timeLabel, constraints)

        constraints.gridx = 2
        constraints.gridy = 0
        constraints.weightx = 0.33
        statusPanel.add(sizeLabel, constraints)

        panel.add(statusPanel, BorderLayout.NORTH)
        panel.add(responseTabs, BorderLayout.CENTER)

        return panel
    }

    private fun createParamsPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(5)

        // Table model for params
        val columnNames = arrayOf("Key", "Value", "Description")
        val data = Array(10) { arrayOf("", "", "") }
        data[0] = arrayOf("id", "123", "Resource identifier")
        data[1] = arrayOf("filter", "active", "Filter results")

        val table = JTable(data, columnNames)
        val scrollPane = JBScrollPane(table)

        panel.add(scrollPane, BorderLayout.CENTER)

        // Buttons panel
        val buttonsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val addButton = JButton("Add")
        val removeButton = JButton("Remove")

        buttonsPanel.add(addButton)
        buttonsPanel.add(removeButton)

        panel.add(buttonsPanel, BorderLayout.SOUTH)

        return panel
    }

    private fun createHeadersPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(5)

        // Table model for headers
        val columnNames = arrayOf("Key", "Value", "Description")
        val data = Array(10) { arrayOf("", "", "") }
        data[0] = arrayOf("Content-Type", "application/json", "The MIME type of the body of the request")
        data[1] = arrayOf("Authorization", "Bearer token", "Authentication token")

        val table = JTable(data, columnNames)
        val scrollPane = JBScrollPane(table)

        panel.add(scrollPane, BorderLayout.CENTER)

        // Buttons panel
        val buttonsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val addButton = JButton("Add")
        val removeButton = JButton("Remove")

        buttonsPanel.add(addButton)
        buttonsPanel.add(removeButton)

        panel.add(buttonsPanel, BorderLayout.SOUTH)

        return panel
    }

    private fun createBodyPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(5)

        // Content type selector
        val contentTypePanel = JPanel(FlowLayout(FlowLayout.LEFT))
        contentTypePanel.add(JLabel("Content Type:"))

        val contentTypes = arrayOf(
            "application/json",
            "application/xml",
            "application/x-www-form-urlencoded",
            "multipart/form-data",
            "text/plain"
        )
        val contentTypeCombo = JComboBox(contentTypes)
        contentTypePanel.add(contentTypeCombo)

        panel.add(contentTypePanel, BorderLayout.NORTH)

        // Text area for body content
        val textArea = JTextArea()
        textArea.text = "{\n  \"key\": \"value\"\n}"

        val scrollPane = JBScrollPane(textArea)
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }


    fun createPanel(project: Project): JPanel {
        // Paste your entire createRestClientPanel() function from MyToolWindowFactory here.
        // Rename the function from createRestClientPanel to createPanel.

        val panel = JPanel(BorderLayout())

        panel.border = JBUI.Borders.empty(5)

        // Top panel with HTTP method dropdown and URL field
        val topPanel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints()

        // HTTP Method dropdown
        val httpMethods = arrayOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
        val methodComboBox = JComboBox(httpMethods)
        methodComboBox.preferredSize = Dimension(100, methodComboBox.preferredSize.height)
        methodComboBox.minimumSize = Dimension(80, methodComboBox.minimumSize.height)

        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.weightx = 0.1
        constraints.insets = JBUI.insets(0, 0, 0, 5)
        topPanel.add(methodComboBox, constraints)

        // URL text field
        val urlField = JBTextField("https://api.example.com")
        urlField.emptyText.text = "Enter URL"

        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 1
        constraints.gridy = 0
        constraints.weightx = 0.8
        constraints.insets = JBUI.insets(0, 0, 0, 5)
        topPanel.add(urlField, constraints)

        // Send button
        val sendButton = JButton("Send")
        sendButton.preferredSize = Dimension(80, sendButton.preferredSize.height)
        sendButton.minimumSize = Dimension(80, sendButton.minimumSize.height)

        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 2
        constraints.gridy = 0
        constraints.weightx = 0.1
        constraints.insets = JBUI.insets(0)
        topPanel.add(sendButton, constraints)

        panel.add(topPanel, BorderLayout.NORTH)

        // Create tabbed pane for request details
        val requestTabbedPane = JBTabbedPane()

        // Body tab
        val bodyPanel = createBodyPanel()
        requestTabbedPane.addTab("Body", bodyPanel)

        // Headers tab
        val headersPanel = createHeadersPanel()
        requestTabbedPane.addTab("Headers", headersPanel)

        // Params tab
        val paramsPanel = createParamsPanel()
        requestTabbedPane.addTab("Params", paramsPanel)

        // Response panel
        val responseBodyTextArea = JTextArea()
        responseBodyTextArea.isEditable = false

        val responseHeadersTextArea = JTextArea()
        responseHeadersTextArea.isEditable = false

        val statusLabel = JLabel("Status: ")
        val timeLabel = JLabel("Time: ")
        val sizeLabel = JLabel("Size: ")

        val responsePanel = createResponsePanel(responseBodyTextArea, responseHeadersTextArea, statusLabel, timeLabel, sizeLabel)

        // Split pane to divide request and response
        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, requestTabbedPane, responsePanel)
        splitPane.resizeWeight = 0.5
        splitPane.dividerLocation = 300

        // Create a bottom panel for the "Integrate into my code" button
        val bottomPanel = JPanel(BorderLayout())

        // Create the "Integrate into my code" button
        val integrateButton = JButton("Integrate into my code")
        integrateButton.background = Color(0x2679CA)
        integrateButton.addActionListener {
            // Handle integration into code
            JOptionPane.showMessageDialog(
                panel,
                "This will generate code to integrate the current request into your project.",
                "Integrate into Code",
                JOptionPane.INFORMATION_MESSAGE
            )

            // Here you would implement the actual code generation and integration
            // For example, generating a Java/Kotlin class that makes this HTTP request
        }

        // Create a panel to hold the button at the right side
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        buttonPanel.add(integrateButton)
        bottomPanel.add(buttonPanel, BorderLayout.EAST)

        // Add a border to separate it visually
        bottomPanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")),
            JBUI.Borders.empty(5)
        )

        // Main content panel that will hold the split pane
        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(splitPane, BorderLayout.CENTER)
        contentPanel.add(bottomPanel, BorderLayout.SOUTH)

        panel.add(contentPanel, BorderLayout.CENTER)

        // Add action listener to send button
        sendButton.addActionListener {
            val method = methodComboBox.selectedItem as String
            val url = urlField.text
            val contentTypeCombo = (bodyPanel.components[0] as JPanel).components[1] as JComboBox<*>
            val contentType = contentTypeCombo.selectedItem as String
            val bodyContent = ((bodyPanel.components[1] as JBScrollPane).viewport.view as JTextArea).text

            // Get headers from the headers table
            val headersTable = ((headersPanel.components[0] as JBScrollPane).viewport.view as JTable)
            val headers = mutableMapOf<String, String>()
            for (i in 0 until headersTable.rowCount) {
                val key = headersTable.getValueAt(i, 0) as? String ?: ""
                val value = headersTable.getValueAt(i, 1) as? String ?: ""
                if (key.isNotEmpty() && value.isNotEmpty()) {
                    headers[key] = value
                }
            }

            // Get params from the params table
            val paramsTable = ((paramsPanel.components[0] as JBScrollPane).viewport.view as JTable)
            val params = mutableMapOf<String, String>()
            for (i in 0 until paramsTable.rowCount) {
                val key = paramsTable.getValueAt(i, 0) as? String ?: ""
                val value = paramsTable.getValueAt(i, 1) as? String ?: ""
                if (key.isNotEmpty() && value.isNotEmpty()) {
                    params[key] = value
                }
            }

            // Build URL with query parameters
            var urlWithParams = url
            if (params.isNotEmpty()) {
                urlWithParams += "?" + params.entries.joinToString("&") { "${it.key}=${it.value}" }
            }

            // Disable send button while request is in progress
            sendButton.isEnabled = false
            statusLabel.text = "Status: Sending request..."
            timeLabel.text = "Time: "
            sizeLabel.text = "Size: "

            // Execute request in a background thread
            thread {
                try {
                    val startTime = System.currentTimeMillis()

                    val client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build()

                    val requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(urlWithParams))
                        .timeout(Duration.ofSeconds(30))

                    // Add headers
                    headers.forEach { (key, value) ->
                        requestBuilder.header(key, value)
                    }

                    // Add content type if not already in headers
                    if (!headers.containsKey("Content-Type")) {
                        requestBuilder.header("Content-Type", contentType)
                    }

                    // Set method and body
                    val request = when (method) {
                        "GET" -> requestBuilder.GET().build()
                        "DELETE" -> requestBuilder.DELETE().build()
                        "OPTIONS" -> requestBuilder.method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build()
                        else -> {
                            // POST, PUT, PATCH with body
                            val bodyPublisher = HttpRequest.BodyPublishers.ofString(bodyContent)
                            requestBuilder.method(method, bodyPublisher).build()
                        }
                    }

                    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime

                    // Update UI in EDT
                    SwingUtilities.invokeLater {
                        // Update response body
                        responseBodyTextArea.text = response.body()

                        // Update response headers
                        val headersText = response.headers().map().entries.joinToString("\n") { "${it.key}: ${it.value.joinToString(", ")}" }
                        responseHeadersTextArea.text = headersText

                        // Update status
                        statusLabel.text = "Status: ${response.statusCode()}"
                        timeLabel.text = "Time: $duration ms"
                        sizeLabel.text = "Size: ${response.body().length} bytes"

                        // Re-enable send button
                        sendButton.isEnabled = true
                    }
                } catch (e: Exception) {
                    // Handle errors
                    SwingUtilities.invokeLater {
                        responseBodyTextArea.text = "Error: ${e.message}"
                        statusLabel.text = "Status: Error"
                        sendButton.isEnabled = true
                    }
                }
            }
        }


        return panel
    }
}
