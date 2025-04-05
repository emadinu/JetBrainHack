package com.github.emadinu.jetbrainhack.toolWindow.modules

import javax.swing.JComponent

interface PluginModule {
    fun getTitle(): String
    fun getComponent(): JComponent
}
