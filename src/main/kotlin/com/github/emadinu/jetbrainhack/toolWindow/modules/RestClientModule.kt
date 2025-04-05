package com.github.emadinu.jetbrainhack.toolWindow.modules

import com.github.emadinu.jetbrainhack.toolWindow.panels.RestClientPanelFactory
import com.intellij.openapi.project.Project
import javax.swing.JPanel

// Your REST Client panel is already defined in MyToolWindowFactory.
// Move the entire createRestClientPanel function here.

class RestClientModule(private val project: Project) : PluginModule {
    override fun getTitle() = "REST Client"

    override fun getComponent(): JPanel {
        val factory = RestClientPanelFactory()
        return factory.createPanel(project)
    }
}
