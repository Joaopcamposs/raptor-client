package com.raptorclient.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "RaptorClientEnvironments",
    storages = [Storage("raptorClientEnv.xml")],
)
@Suppress("unused")
class EnvironmentService(
    @Suppress("unused") private val project: Project,
) : PersistentStateComponent<EnvironmentService.State> {
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    private var environments = mutableMapOf<String, MutableMap<String, String>>()
    private var activeEnvironment: String? = null
    private val listeners = mutableListOf<EnvironmentChangeListener>()

    data class State(
        var environmentsJson: String = "{}",
        var activeEnvironmentName: String? = null,
    )

    private var myState = State()

    override fun getState(): State {
        myState.environmentsJson = objectMapper.writeValueAsString(environments)
        myState.activeEnvironmentName = activeEnvironment
        return myState
    }

    override fun loadState(state: State) {
        myState = state
        try {
            environments = objectMapper.readValue(state.environmentsJson)
            activeEnvironment = state.activeEnvironmentName
        } catch (_: Exception) {
            environments = mutableMapOf()
            activeEnvironment = null
        }
    }

    fun getEnvironments(): Map<String, Map<String, String>> = environments.toMap()

    fun getActiveEnvironment(): String? = activeEnvironment

    fun setActiveEnvironment(name: String?) {
        activeEnvironment = name
        notifyListeners()
    }

    fun createEnvironment(name: String) {
        if (!environments.containsKey(name)) {
            environments[name] = mutableMapOf()
            notifyListeners()
        }
    }

    fun deleteEnvironment(name: String) {
        environments.remove(name)
        if (activeEnvironment == name) {
            activeEnvironment = null
        }
        notifyListeners()
    }

    fun setVariable(
        environmentName: String,
        key: String,
        value: String,
    ) {
        environments.getOrPut(environmentName) { mutableMapOf() }[key] = value
        notifyListeners()
    }

    fun removeVariable(
        environmentName: String,
        key: String,
    ) {
        environments[environmentName]?.remove(key)
        notifyListeners()
    }

    fun getVariables(environmentName: String): Map<String, String> = environments[environmentName] ?: emptyMap()

    fun resolveVariables(text: String): String {
        val activeEnv = activeEnvironment ?: return text
        val vars = environments[activeEnv] ?: return text

        var result = text
        val pattern = Regex("\\{\\{([^}]+)\\}\\}")

        pattern.findAll(text).forEach { match ->
            val varName = match.groupValues[1].trim()
            val value = vars[varName]
            if (value != null) {
                result = result.replace(match.value, value)
            }
        }

        return result
    }

    fun addListener(listener: EnvironmentChangeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: EnvironmentChangeListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.onEnvironmentChanged() }
    }

    interface EnvironmentChangeListener {
        fun onEnvironmentChanged()
    }

    companion object {
        fun getInstance(project: Project): EnvironmentService = project.getService(EnvironmentService::class.java)
    }
}
