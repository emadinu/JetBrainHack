package com.github.emadinu.jetbrainhack.events

import ai.grazie.utils.json.JSONObject

// Definim un nou event data class pentru canalul de update
data class UpdateEvent(val payload: String)

// Un nou event bus pentru evenimentele de tip Update
object UpdateEventBus {
    private val listeners = mutableListOf<(UpdateEvent) -> Unit>()

    fun subscribe(listener: (UpdateEvent) -> Unit) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: (UpdateEvent) -> Unit) {
        listeners.remove(listener)
    }

    fun publish(event: UpdateEvent) {
        listeners.forEach { it.invoke(event) }
    }
}