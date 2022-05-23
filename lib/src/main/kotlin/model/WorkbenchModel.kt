package model

import androidx.compose.runtime.*
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState
import view.conponent.DefaultExplorerOverview

@OptIn(ExperimentalSplitPaneApi::class)
internal class WorkbenchModel {

    private val selectedModules = Array<Array<MutableState<WorkbenchModuleState<*>?>>>(DisplayType.values().size) {
        Array(
            ModuleType.values().size
        ) { mutableStateOf(null) }
    }

    var appTitle: String = "Compose Workbench"
    val modules = mutableStateListOf<WorkbenchModuleState<*>>()

    val registeredExplorers = mutableMapOf<String, WorkbenchModule<*>>()
    val registeredDefaultExplorers = mutableMapOf<Int, WorkbenchDefaultState<*>>()
    val registeredEditors = mutableMapOf<String, WorkbenchModule<*>>()

    var splitViewMode by mutableStateOf(SplitViewMode.UNSPLIT)

    var bottomSplitState by mutableStateOf(SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.7f))
    var leftSplitState by  mutableStateOf(SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.25f))

    var dragState by mutableStateOf( DragState() )
    private var uniqueKey = 0

    init {
        showDefaultExplorersOverview()
    }

    fun getNextKey():Int = uniqueKey++

    @Suppress("UNCHECKED_CAST")
    fun createExplorerFromDefault (id: Int) {
        val defaultState = if(registeredDefaultExplorers[id] != null) registeredDefaultExplorers[id]!!as WorkbenchDefaultState<Any> else return
        val explorer = registeredExplorers[defaultState.type] as WorkbenchModule<Any>

        val state = WorkbenchModuleState(id, defaultState.title, defaultState.model, explorer, ::removeTab, DisplayType.LEFT)
        addState(state)
    }

    fun getSelectedModule (displayType: DisplayType, moduleType: ModuleType): MutableState<WorkbenchModuleState<*>?> {
        return selectedModules[displayType.ordinal][moduleType.ordinal]
    }

    fun setSelectedModule (state: WorkbenchModuleState<*>) {
        selectedModules[state.displayType.ordinal][state.module.moduleType.ordinal].value = state
        showDrawer(state.displayType)
    }

    fun setSelectedModuleNull (displayType: DisplayType, moduleType: ModuleType) {
        selectedModules[displayType.ordinal][moduleType.ordinal].value = null
        hideDrawer(displayType)
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
            when (val idx = filteredStates.indexOf(state).coerceAtLeast(0)) {
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

        splitViewMode = mode
        if (mode == SplitViewMode.UNSPLIT) {
            modules.filter { it.displayType == DisplayType.TAB2 }.forEach { it.displayType = DisplayType.TAB1 }
            setSelectedModuleNull(DisplayType.TAB2, ModuleType.EDITOR)
            return
        }

        val m1 = getSelectedModule(DisplayType.TAB1, ModuleType.EDITOR).value
        val m2 = getSelectedModule(DisplayType.TAB2, ModuleType.EDITOR).value
        if (m1 != null && m2 == null) {
            reselectState(m1)
            m1.displayType = DisplayType.TAB2
            setSelectedModule(m1)
        }
        if (m1 == null && m2 != null) {
            reselectState(m2)
            m2.displayType = DisplayType.TAB1
            setSelectedModule(m2)
        }
    }

    fun showDefaultExplorersOverview() {
        val modelType = "DefaultExplorers"
        val existing = modules.firstOrNull{ it.module.modelType == modelType }

        if (existing != null) {
            setSelectedModule(existing)
            return
        }

        val editor = WorkbenchModule<WorkbenchModel>(
            moduleType = ModuleType.EDITOR,
            modelType = modelType,
            content = {
                DefaultExplorerOverview(it)
            }
        )
        val t = WorkbenchModuleState(id = getNextKey(), title = { "Default Explorers" }, model = this, module = editor, ::removeTab, DisplayType.TAB1)
        addState(t)
    }

    private fun hideDrawer(displayType: DisplayType) {
        when(displayType) {
            DisplayType.LEFT -> leftSplitState = SplitPaneState(moveEnabled = false, initialPositionPercentage = 0f)
            DisplayType.BOTTOM -> bottomSplitState = SplitPaneState(moveEnabled = false, initialPositionPercentage = 1f)
            else -> Unit
        }
    }

    private fun showDrawer(displayType: DisplayType) {
        when(displayType) {
            DisplayType.LEFT -> leftSplitState = SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.25f)
            DisplayType.BOTTOM -> bottomSplitState = SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.7f)
            else -> Unit
        }
    }
}

