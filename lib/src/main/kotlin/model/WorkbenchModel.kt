package model

import androidx.compose.runtime.*
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.DisplayType
import model.state.SplitViewMode
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.SplitPaneState

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

    var splitViewMode by mutableStateOf(SplitViewMode.UNSPLIT)

    var bottomSplitState by mutableStateOf(SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.7f))
    var leftSplitState by  mutableStateOf(SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.25f))

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

    fun changeSplitViewMode (mode: SplitViewMode) {

        if (mode == splitViewMode) return

        splitViewMode = mode;

        if (mode == SplitViewMode.UNSPLIT)
        {
            modules.filter { it.displayType == DisplayType.TAB2 }.forEach { it.displayType = DisplayType.TAB1 }
            setSelectedModuleNull(DisplayType.TAB2, ModuleType.EDITOR)
            return
        }

        var m1 = getSelectedModule(DisplayType.TAB1, ModuleType.EDITOR).value
        var m2 = getSelectedModule(DisplayType.TAB2, ModuleType.EDITOR).value

        if (m1 != null && m2 == null) {
            reselectState(m1)
            m1!!.displayType = DisplayType.TAB2
            setSelectedModule(m1)
        }
        if (m1 == null && m2 != null) {
            reselectState(m2)
            m2!!.displayType = DisplayType.TAB1
            setSelectedModule(m2)
        }
    }
}
