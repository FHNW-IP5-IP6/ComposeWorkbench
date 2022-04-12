package model

import androidx.compose.runtime.*
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.DisplayType
import model.state.WorkbenchModuleState

internal class WorkbenchModel {

    private val selectedModules = Array<Array<MutableState<WorkbenchModuleState<*>?>>>(DisplayType.values().size) { i ->
        Array(
            ModuleType.values().size
        ) { j -> mutableStateOf(null) }
    }

    var appTitle: String = "Compose Workbench"

    val modules = mutableStateListOf<WorkbenchModuleState<*>>()

    val registeredExplorers = mutableMapOf<String, WorkbenchModule<*>>()
    val registeredEditors = mutableMapOf<String, WorkbenchModule<*>>()

    fun getSelectedModule (displayType: DisplayType, moduleType: ModuleType): MutableState<WorkbenchModuleState<*>?> {
        return selectedModules[displayType.ordinal][moduleType.ordinal]
    }

    fun setSelectedModule (state: WorkbenchModuleState<*>) {
        selectedModules[state.displayType.ordinal][state.module.moduleType.ordinal].value = state
    }

    fun setSelectedModuleNull (displayType: DisplayType, moduleType: ModuleType) {
        selectedModules[displayType.ordinal][moduleType.ordinal].value = null
    }

    fun addState(state: WorkbenchModuleState<*>) {
        modules += state
        setSelectedModule(state)
    }

    fun removeTab(tab: WorkbenchModuleState<*>) {
        reselectState(tab)
        modules.remove(tab)
    }

    private fun reselectState(state: WorkbenchModuleState<*>) {
        val filteredStates = modules.filter { it.displayType == state.displayType && it.module.moduleType == state.module.moduleType }
        if (filteredStates.size <= 1) {
            setSelectedModuleNull(state.displayType, state.module.moduleType)
        } else {
            val idx = filteredStates.indexOf(state).coerceAtLeast(0)
            when (idx) {
                0 -> {
                    setSelectedModule(filteredStates[1])
                }
                filteredStates.size-1 -> {
                    setSelectedModule(filteredStates[idx - 1])
                }
                else -> {
                    setSelectedModule(filteredStates[idx + 1])
                }
            }
        }
    }

    fun saveAll (moduleType: ModuleType) {
        modules.filter { it.module.moduleType == moduleType }.forEach{
            it.onSave()
        }
    }

}