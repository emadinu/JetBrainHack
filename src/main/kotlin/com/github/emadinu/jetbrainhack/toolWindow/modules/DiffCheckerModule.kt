package com.github.emadinu.jetbrainhack.toolWindow.modules

import com.github.emadinu.jetbrainhack.events.IntegrationEvent
import com.github.emadinu.jetbrainhack.events.IntegrationEventBus
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument
import javax.swing.SwingUtilities

class DiffCheckerModule(private val project: Project) : PluginModule {
    private val panel = JPanel(BorderLayout())

    init {
        // Create a small toolbar at the top with margins
        val toolbarPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        toolbarPanel.border = JBUI.Borders.empty(5, 5, 5, 5)

        val compareButton = JButton("Compare Files").apply {
            addActionListener { showDiff(emptyMap()) }
        }
        val statusLabel = JLabel("")

        toolbarPanel.add(compareButton)
        toolbarPanel.add(statusLabel)

        val button = JButton("Apply Changes")
        panel.add(button, BorderLayout.EAST)

        panel.add(toolbarPanel, BorderLayout.NORTH)
        IntegrationEventBus.subscribe { event ->
            SwingUtilities.invokeLater {
//                statusLabel.text = "Received integration event: ${event.payload}"
                val files = event.payload
                showDiff(files)
            }
        }
    }

    override fun getTitle() = "Diff Checker"
    override fun getComponent() = panel

    fun readFileContent(filePath: String): String {
        return try {
            java.io.File(filePath).readText()
        } catch (e: Exception) {
            "Error reading file: ${e.message}"
        }
    }

    private fun showDiff(files: Map<String, String>) {
        // Prompt user for two files
//        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor().apply {
//            title = "Select a file to compare"
//        }
        val fileExists = files["fileExists"] ?: return
        val file1Path = files["filePath"] ?: return
        val file2Content= files["fileContent"] ?: return

        // Read file contents
        val content1: String
        val content2: String

        print ("fileExists: $fileExists")

        if (fileExists == "true") {
            print("AAAAA: $fileExists")
            content1 = ""
            content2 = file2Content
        } else {
            print("BBBBB: $fileExists")
            content1 = readFileContent(file1Path)
            content2 = file2Content
        }

        val lines1 = content1.lines()
        val lines2 = content2.lines()


        // Generate diff
        val (diffLeft, diffRight) = generateDiff(lines1, lines2)

        // Create styled text panes for displaying the diff side-by-side
        val textPaneLeft = createStyledTextPane()
        val textPaneRight = createStyledTextPane()

        // Append colored diff lines
        appendColoredText(textPaneLeft, diffLeft)
        appendColoredText(textPaneRight, diffRight)

        // Place the text panes in a split pane
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            JScrollPane(textPaneLeft), JScrollPane(textPaneRight)
        ).apply {
            // 50% / 50% initial split
            resizeWeight = 0.5
            // Optional: a little border around the split pane
            border = JBUI.Borders.empty(5)
        }

        // Clear any existing center content and add the split pane
        panel.removeAll()
        // Re-add the top toolbar
        (panel.layout as? BorderLayout)?.getLayoutComponent(BorderLayout.NORTH)?.let {
            panel.add(it, BorderLayout.NORTH)
        }
        panel.add(splitPane, BorderLayout.CENTER)
        panel.revalidate()
        panel.repaint()
    }

    private fun createStyledTextPane(): JTextPane {
        return JTextPane().apply {
            font = Font("Monospaced", Font.PLAIN, 12)
            isEditable = false
            // Match IDE’s background/foreground colors
            background = UIUtil.getTextFieldBackground()
            foreground = UIUtil.getLabelForeground()
            // Provide a bit of padding around the text
            margin = JBUI.insets(5)
        }
    }

    private fun readFileContent(file: VirtualFile): String {
        return file.inputStream.bufferedReader().use { it.readText() }
    }

    /**
     * Appends each line from the diff with a color according to its type:
     * - Lines starting with "- " (deletions) are red with a light red background.
     * - Lines starting with "+ " (insertions) are green with a light green background.
     * - Common lines are shown in default foreground/background.
     */
    private fun appendColoredText(textPane: JTextPane, lines: List<String>) {
        val doc: StyledDocument = textPane.styledDocument

        for (line in lines) {
            val style = SimpleAttributeSet()

            when {
                // Deleted lines
                line.startsWith("- ") -> {
                    StyleConstants.setForeground(style, JBColor(Color(0xBE7070), Color(255, 100, 100)))
                    StyleConstants.setBackground(style, JBColor(Color(255, 235, 235), Color(78, 13, 28)))
                }
                // Inserted lines
                line.startsWith("+ ") -> {
                    StyleConstants.setForeground(style, JBColor(Color(0, 130, 0), Color(0x5F826B)))
                    StyleConstants.setBackground(style, JBColor(Color(235, 255, 235), Color(13,37,20)))
                }
                // Common lines
                else -> {
                    // Use defaults
                    StyleConstants.setForeground(style, textPane.foreground)
                    StyleConstants.setBackground(style, textPane.background)
                }
            }

            doc.insertString(doc.length, line + "\n", style)
        }
    }

    /**
     * Computes the longest common subsequence (LCS) indices between two lists of strings.
     * Returns a list of index pairs (i, j) for lines that are common to both files.
     */
    private fun computeLcsIndices(a: List<String>, b: List<String>): List<Pair<Int, Int>> {
        val m = a.size
        val n = b.size
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1] + 1
                } else {
                    maxOf(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }

        val indices = mutableListOf<Pair<Int, Int>>()
        var i = m
        var j = n
        while (i > 0 && j > 0) {
            when {
                a[i - 1] == b[j - 1] -> {
                    indices.add(Pair(i - 1, j - 1))
                    i--
                    j--
                }
                dp[i - 1][j] >= dp[i][j - 1] -> i--
                else -> j--
            }
        }
        indices.reverse()
        return indices
    }

    /**
     * Generates two lists of strings representing the left and right columns of a side-by-side diff.
     *
     * Lines present only in file1 are prefixed with "- " on the left;
     * lines only in file2 are prefixed with "+ " on the right.
     * Matching lines are prefixed with "  " on both sides.
     */
    private fun generateDiff(lines1: List<String>, lines2: List<String>): Pair<List<String>, List<String>> {
        val lcsIndices = computeLcsIndices(lines1, lines2)
        val diffLeft = mutableListOf<String>()
        val diffRight = mutableListOf<String>()
        var i = 0
        var j = 0

        for ((index1, index2) in lcsIndices) {
            // Process extra lines in file1 (deletions)
            while (i < index1) {
                diffLeft.add("- " + lines1[i])
                diffRight.add("")
                i++
            }
            // Process extra lines in file2 (insertions)
            while (j < index2) {
                diffLeft.add("")
                diffRight.add("+ " + lines2[j])
                j++
            }
            // Common line
            diffLeft.add("  " + lines1[index1])
            diffRight.add("  " + lines2[index2])
            i = index1 + 1
            j = index2 + 1
        }

        // Process any remaining lines in file1
        while (i < lines1.size) {
            diffLeft.add("- " + lines1[i])
            diffRight.add("")
            i++
        }
        // Process any remaining lines in file2
        while (j < lines2.size) {
            diffLeft.add("")
            diffRight.add("+ " + lines2[j])
            j++
        }
        return Pair(diffLeft, diffRight)
    }
}