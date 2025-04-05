package com.github.emadinu.jetbrainhack.toolWindow.modules

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument

class DiffCheckerModule(private val project: Project) : PluginModule {
    private val panel = JPanel(BorderLayout())

    init {
        val button = JButton("Compare Files")
        button.addActionListener { showDiff() }
        panel.add(button, BorderLayout.NORTH)
    }

    override fun getTitle() = "Diff Checker"
    override fun getComponent() = panel

    private fun showDiff() {
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor().apply {
            title = "Select a file to compare"
        }

        // Prompt user for the first file
        val file1: VirtualFile? = FileChooser.chooseFile(descriptor, project, null)
        if (file1 == null) return

        // Prompt user for the second file
        val file2: VirtualFile? = FileChooser.chooseFile(descriptor, project, null)
        if (file2 == null) return

        // Read the content of both files
        val content1 = readFileContent(file1)
        val content2 = readFileContent(file2)

        val lines1 = content1.lines()
        val lines2 = content2.lines()

        // Generate diff using our own implementation
        val (diffLeft, diffRight) = generateDiff(lines1, lines2)

        // Create styled text panes for displaying the diff side-by-side
        val textPaneLeft = JTextPane()
        val textPaneRight = JTextPane()
        val font = Font("Monospaced", Font.PLAIN, 12)
        textPaneLeft.font = font
        textPaneRight.font = font
        textPaneLeft.isEditable = false
        textPaneRight.isEditable = false

        // Append colored text to both text panes
        appendColoredText(textPaneLeft, diffLeft)
        appendColoredText(textPaneRight, diffRight)

        // Use a JSplitPane to show both diffs side by side
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, JScrollPane(textPaneLeft), JScrollPane(textPaneRight))
        splitPane.setDividerLocation(0.5)
        panel.add(splitPane, BorderLayout.CENTER)
        panel.revalidate()
        panel.repaint()
    }

    private fun readFileContent(file: VirtualFile): String {
        return file.inputStream.bufferedReader().use { it.readText() }
    }

    /**
     * Appends each line from the diff with a color according to its type:
     * - Lines starting with "- " (deletions) are red with a light red background.
     * - Lines starting with "+ " (insertions) are dark green with a light green background.
     * - Common lines are shown in black.
     */
    private fun appendColoredText(textPane: JTextPane, lines: List<String>) {
        val doc: StyledDocument = textPane.styledDocument
        for (line in lines) {
            val style = SimpleAttributeSet()
            var fgColor: Color = Color.BLACK
            var bgColor: Color? = null

            when {
                line.startsWith("- ") -> {
                    fgColor = Color.RED
                    bgColor = Color(255, 235, 235) // light red
                }
                line.startsWith("+ ") -> {
                    fgColor = Color(0, 128, 0) // dark green
                    bgColor = Color(235, 255, 235) // light green
                }
            }
            StyleConstants.setForeground(style, fgColor)
            bgColor?.let { StyleConstants.setBackground(style, it) }
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
