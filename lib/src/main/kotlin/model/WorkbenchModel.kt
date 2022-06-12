package model

import androidx.compose.runtime.*
import model.data.DisplayType
import model.data.ModuleType
import model.data.SplitViewMode
import model.data.WorkbenchModule
import model.state.WindowStateAware
import model.state.WorkbenchDefaultState
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState
import view.component.DefaultExplorerOverview

@OptIn(ExperimentalSplitPaneApi::class)
internal class WorkbenchModel(val appTitle: String = "") {
    private val selectedModules = Array<Array<MutableState<WorkbenchModuleState<*>?>>>(DisplayType.values().size) {
        Array(
            ModuleType.values().size
        ) { mutableStateOf(null) }
    }

    val modules = mutableStateListOf<WorkbenchModuleState<*>>()
    val windows = mutableStateListOf<WindowStateAware>()
    var previewModule: WorkbenchModuleState<*>? by mutableStateOf(null)
    private set

    val registeredExplorers = mutableMapOf<String, WorkbenchModule<*>>()
    val registeredDefaultExplorers = mutableMapOf<Int, WorkbenchDefaultState<*>>()
    val registeredEditors = mutableMapOf<String, WorkbenchModule<*>>()

    var splitViewMode by mutableStateOf(SplitViewMode.UNSPLIT)

    var bottomSplitState by mutableStateOf(SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.7f))
    var leftSplitState by  mutableStateOf(SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.25f))

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

    fun updatePreviewModule (state: WorkbenchModuleState<*>) {
        if(previewModule?.id != state.id){
            modules.remove(previewModule)
            previewModule = state
            modules += state
        }
    }

    fun clearPreview() {
        modules.remove(previewModule)
        previewModule = null
    }

    fun setSelectedModuleNull (displayType: DisplayType, moduleType: ModuleType) {
        selectedModules[displayType.ordinal][moduleType.ordinal].value = null
        hideDrawer(displayType)
    }

    fun addState(state: WorkbenchModuleState<*>) {
        if (modules.any{ state.model == it.model && !it.isPreview }) return
        modules += state
        setSelectedModule(state)
    }

    fun removeTab(tab: WorkbenchModuleState<*>) {
        reselectState(tab)
        modules.remove(tab)
    }

    fun reselectState(state: WorkbenchModuleState<*>) {
        val selected = getSelectedModule(state.displayType, state.module.moduleType)
        val filteredStates = modules.filter { it.displayType == state.displayType && it.module.moduleType == state.module.moduleType }.reversed()
        if (filteredStates.size <= 1) {
            setSelectedModuleNull(state.displayType, state.module.moduleType)
        } else if(selected.value == null || selected.value!!.id == state.id){
            when (val idx = filteredStates.indexOfFirst { it.id == state.id}.coerceAtLeast(0)) {
                0 -> {
                    setSelectedModule(filteredStates[1])
                }
                else -> {
                    setSelectedModule(filteredStates[idx - 1])
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

    fun hideDrawer(displayType: DisplayType) {
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

