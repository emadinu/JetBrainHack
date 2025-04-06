package com.github.emadinu.jetbrainhack.toolWindow.panels

import com.github.emadinu.jetbrainhack.events.IntegrationEventBus
import com.github.emadinu.jetbrainhack.toolWindow.modules.FileTreeModule
import com.github.emadinu.jetbrainhack.toolWindow.modules.RestClientModule
import com.github.emadinu.jetbrainhack.toolWindow.modules.DiffCheckerModule
import com.github.emadinu.jetbrainhack.toolWindow.modules.InterfaceModule
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities

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

        val interfaceModule = InterfaceModule(project)
        tabbedPane.addTab(interfaceModule.getTitle(), interfaceModule.getComponent())

        add(tabbedPane, BorderLayout.CENTER)

        IntegrationEventBus.subscribe { event ->
            // This code will be executed when an integration event is published.
            SwingUtilities.invokeLater {
                // Switch to the DiffChecker module tab.
                // Here we assume DiffChecker is the third tab (index 2).
                tabbedPane.selectedIndex = 2
            }
        }
    }

    // Switch to other tabs
    fun switchToTab(index: Int) {
        if (index in 0 until tabbedPane.tabCount) {
            tabbedPane.selectedIndex = index
        }
    }
}