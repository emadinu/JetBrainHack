package com.github.emadinu.jetbrainhack.toolWindow.modules

import com.github.emadinu.jetbrainhack.events.IntegrationEventBus
import com.github.emadinu.jetbrainhack.events.UpdateEventBus


import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.openapi.project.Project
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.*
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer

// Data class to represent the JSON structure
data class FieldMatch(
    val field: String,
    val matchesWith: String,
    val score: Double
)

class InterfaceModule(private val project: Project) : PluginModule {
    private val panel = JPanel(BorderLayout())
    private val tableModel = DefaultTableModel()
    private val table = JBTable(tableModel)
    private val statusLabel = JLabel()
    private var response: String? = null

    init {
        // Set up table columns
        tableModel.addColumn("Field")
        tableModel.addColumn("Matches With")
        tableModel.addColumn("Score")
        tableModel.addColumn("Action")

        // Configure only the action column (index 3) to use button renderer and editor
        table.getColumnModel().getColumn(3).cellRenderer = ButtonRenderer()
        table.getColumnModel().getColumn(3).cellEditor = ButtonEditor(table, project)

        // Add components to panel
        val scrollPane = JBScrollPane(table)
        panel.add(scrollPane, BorderLayout.CENTER)

        val bottomPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        bottomPanel.add(statusLabel)
        panel.add(bottomPanel, BorderLayout.SOUTH)

        // Example data
        val list = listOf(
            FieldMatch("exampleField", "exampleMatch", 0.9),
            FieldMatch("anotherField", "anotherMatch", 0.8),
            FieldMatch("thirdField", "thirdMatch", 1.0)
        )
        updateWithData(list)

        IntegrationEventBus.subscribe { event ->
            SwingUtilities.invokeLater {
                val data = event.payload
                if (data["type"] == "fieldMatch") {
                    val field = data["filePath"] ?: return@invokeLater
                    val matchesWith = data["fileContent"] ?: ""
                    val score = data["fileExists"]?.toDoubleOrNull() ?: 0.0

                    val match = FieldMatch(field, matchesWith, score)
                    updateWithData(listOf(match))
                    statusLabel.text = "Received match for $field"
                }
            }
        }

    }

    // Call this method when you receive the JSON data
    fun updateWithData(fieldMatches: List<FieldMatch>) {
        // Clear existing data
        tableModel.rowCount = 0

        if (fieldMatches.isEmpty()) {
            statusLabel.text = "No matches found."
            return
        }

        // Populate table with data
        for (match in fieldMatches) {
            val actionButton = when {
                match.score == 1.0 -> "Interface Exists"
                match.score > 0.8 -> "Modify"
                else -> "Create"
            }
            val fieldNew = if (match.field.startsWith("item_")) {
                "Object in list (not named)"
            } else {
                match.field
            }
            tableModel.addRow(arrayOf(fieldNew, match.matchesWith, match.score, actionButton))
        }

        // Set preferred width for action column
        val actionColumn = table.columnModel.getColumn(3)
        actionColumn.preferredWidth = 120
    }

    override fun getTitle() = "Interface"
    override fun getComponent() = panel
}

fun createTypeScriptInterface(project: Project, className: String, fields: Map<String, String>, folder: PsiDirectory) {
    val psiFileFactory = PsiFileFactory.getInstance(project)
    val content = buildString {
        append("export interface $className {\n")
        for ((name, type) in fields) {
            append("  $name: $type;\n")
        }
        append("}\n")
    }

    val file = psiFileFactory.createFileFromText("$className.ts", TypeScriptFileType.INSTANCE, content)
    ApplicationManager.getApplication().invokeLater {
        WriteCommandAction.runWriteCommandAction(project) {
            folder.add(file)
        }
    }
}

fun addImportToTypeScriptFile(project: Project, targetFile: PsiFile, className: String, relativePath: String) {
    ApplicationManager.getApplication().invokeLater {
        WriteCommandAction.runWriteCommandAction(project) {
            val document = PsiDocumentManager.getInstance(project).getDocument(targetFile)
            if (document != null) {
                val importLine = "import { $className } from '$relativePath';\n"
                document.insertString(0, importLine)
                PsiDocumentManager.getInstance(project).commitDocument(document)
            }
        }
    }
}


// Custom renderer for the button column
class ButtonRenderer : TableCellRenderer {
    private val panel = JPanel(FlowLayout(FlowLayout.CENTER))
    private val button = JButton()

    init {
        panel.add(button)
    }

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        if (value is String) {
            button.text = value

            // Set button appearance based on action type
            when (value) {
                "Interface Exists" -> {
                    button.isEnabled = false
                    button.foreground = Color.GRAY
                }
                "Modify" -> {
                    button.isEnabled = true
                    button.foreground = Color.ORANGE
                }
                "Create" -> {
                    button.isEnabled = true
                    button.foreground = Color.WHITE
                }
            }
        }
        return panel
    }
}

fun findPsiFileByName(project: Project, fileName: String): PsiFile? {
    val virtualFiles: Collection<VirtualFile> = FilenameIndex.getVirtualFilesByName(
        project,
        fileName,
        GlobalSearchScope.projectScope(project)
    )
    return virtualFiles.firstOrNull()?.let {
        PsiManager.getInstance(project).findFile(it)
    }
}

// Custom editor for the button column
class ButtonEditor(private val table: JTable, private val project: Project) : javax.swing.DefaultCellEditor(javax.swing.JTextField()) {
    private val panel = JPanel(FlowLayout(FlowLayout.CENTER))
    private val button = JButton()
    private var clickedValue: String? = null

    init {
        panel.add(button)
        button.addActionListener {
            fireEditingStopped()

            // Get the current row data
            val row = table.editingRow
            val field = table.getValueAt(row, 0) as String
            val matchesWith = table.getValueAt(row, 1) as String

            // Handle button action based on type
            when (clickedValue) {
                "Interface Exists" -> {
                    // Do nothing, button is disabled
                }
                "Modify" -> {
                    // Implement modify interface logic
                    println("Modifying interface for $field that matches with $matchesWith")
                }
                "Create" -> {
                    // Implement create interface logic
                    val className = field


                    println("Creating interface for $field")
                }
            }
        }
    }

    override fun getTableCellEditorComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        clickedValue = value as String?
        button.text = value

        // Set button appearance based on action type
        when (value) {
            "Interface Exists" -> {
                button.isEnabled = false
                button.foreground = Color.GRAY
            }
            "Modify" -> {
                button.isEnabled = true
                button.foreground = Color.ORANGE
            }
            "Create" -> {
                button.isEnabled = true
                button.foreground = Color.WHITE
            }
        }

        return panel
    }

    override fun getCellEditorValue(): Any {
        return clickedValue ?: ""
    }
}