package com.github.emadinu.jetbrainhack.toolWindow.panels

import com.github.emadinu.jetbrainhack.toolWindow.modules.FileTreeModule
import com.github.emadinu.jetbrainhack.toolWindow.modules.RestClientModule
import com.github.emadinu.jetbrainhack.toolWindow.modules.DiffCheckerModule
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import java.awt.BorderLayout
import javax.swing.JPanel

class MyToolWindowPanel(project: Project) : JPanel(BorderLayout()) {
    private val tabbedPane = JBTabbedPane()

    init {
        // Existing module
        val fileTreeModule = FileTreeModule(project)
        tabbedPane.addTab(fileTreeModule.getTitle(), fileTreeModule.getComponent())

        // New REST Client Module
        val restClientModule = RestClientModule(project)
        tabbedPane.addTab(restClientModule.getTitle(), restClientModule.getComponent())

        // In MyToolWindowPanel.kt
        val diffCheckerModule = DiffCheckerModule(project)
        tabbedPane.addTab(diffCheckerModule.getTitle(), diffCheckerModule.getComponent())

        add(tabbedPane, BorderLayout.CENTER)
    }
}
