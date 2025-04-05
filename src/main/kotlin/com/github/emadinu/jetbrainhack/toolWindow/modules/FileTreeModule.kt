package com.github.emadinu.jetbrainhack.toolWindow.modules

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.BorderLayout
import java.io.File
import javax.swing.JButton
import javax.swing.JPanel

class FileTreeModule(private val project: Project) : PluginModule {
    private val panel = JPanel(BorderLayout())
    private val textArea = JBTextArea()
    private val ignoreList = setOf(
        ".git", "node_modules", "build", "out", "target", "bin",
        "obj", "__pycache__", ".idea", ".vscode"
    )

    init {
        val button = JButton("Show Tree")
        button.addActionListener { showTree() }
        panel.add(button, BorderLayout.NORTH)

        textArea.isEditable = false
        textArea.lineWrap = false
        panel.add(JBScrollPane(textArea), BorderLayout.CENTER)
    }

    override fun getTitle() = "File Tree"
    override fun getComponent() = panel

    private fun shouldIgnore(file: File): Boolean {
        return ignoreList.contains(file.name) || file.name.endsWith(".pyc")
    }

    private fun showTree() {
        val projectBasePath = project.basePath ?: run {
            textArea.text = "Project base path not found."
            return
        }
        val root = File(projectBasePath)
        textArea.text = buildTreeString(root)
    }

    private fun buildTreeString(root: File): String {
        val sb = StringBuilder()
        sb.append(root.name).append("\n")
        val children = root.listFiles()?.filter { !shouldIgnore(it) }?.sortedBy { it.name } ?: emptyList()
        for ((index, child) in children.withIndex()) {
            val isLast = index == children.size - 1
            sb.append(buildTree(child, "", isLast))
        }
        return sb.toString()
    }

    private fun buildTree(file: File, prefix: String, isLast: Boolean): String {
        val sb = StringBuilder()
        sb.append(prefix)
        sb.append(if (isLast) "`-- " else "|-- ")
        sb.append(file.name).append("\n")
        if (file.isDirectory) {
            val newPrefix = prefix + if (isLast) "    " else "|   "
            val children = file.listFiles()?.filter { !shouldIgnore(it) }?.sortedBy { it.name } ?: emptyList()
            for ((index, child) in children.withIndex()) {
                val last = index == children.size - 1
                sb.append(buildTree(child, newPrefix, last))
            }
        }
        return sb.toString()
    }
}
