package com.github.emadinu.jetbrainhack.events

// Define the event data class (you can add more fields as needed)
data class IntegrationEvent(val payload: Map<String, String>)

// Simple event bus for integration events
object IntegrationEventBus {
    private val listeners = mutableListOf<(IntegrationEvent) -> Unit>()

    fun subscribe(listener: (IntegrationEvent) -> Unit) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: (IntegrationEvent) -> Unit) {
        listeners.remove(listener)
    }

    fun publish(event: IntegrationEvent) {
        listeners.forEach { it.invoke(event) }
    }
}