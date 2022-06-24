package model

import MENU_IDENTIFIER_MENU_BAR
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import model.data.*
import model.state.DragState
import model.state.WindowStateAware
import model.state.WorkbenchDefaultState
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class)
internal class WorkbenchModel(val appTitle: String = "") {
    private val selectedModules = Array<Array<MutableState<WorkbenchModuleState<*>?>>>(DisplayType.values().size) {
        Array(
            ModuleType.values().size
        ) { mutableStateOf(null) }
    }

    val modules = mutableStateListOf<WorkbenchModuleState<*>>()
    val windows = mutableStateListOf<WindowStateAware>()

    var commands = mutableStateListOf<Command>()
    val menu = MenuEntry(MENU_IDENTIFIER_MENU_BAR)

    val registeredExplorers = mutableMapOf<String, WorkbenchModule<*>>()
    val registeredDefaultExplorers = mutableMapOf<Int, WorkbenchDefaultState<*>>()
    val registeredEditors = mutableMapOf<String, MutableList<WorkbenchModule<*>>>()

    var splitViewMode by mutableStateOf(SplitViewMode.UNSPLIT)
    var currentTabSpace = DisplayType.TAB1

    var bottomSplitState by mutableStateOf(SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.7f))
    var leftSplitState by  mutableStateOf(SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.25f))

    var mainWindow = WindowStateAware(modules = emptyList()) // keeps track of the default window position
    val dragState = DragState(mainWindow)

    private var uniqueKey = 0

    init {
        commands.addAll(
            listOf(
                Command(text = "Save All",
                    path = "$MENU_IDENTIFIER_MENU_BAR.File",
                    action = { saveAll(ModuleType.EDITOR) },
                    shortcut = KeyShortcut(Key.S, ctrl = true, alt = true)
                ),
                Command(text = "Horizontal",
                    path = "$MENU_IDENTIFIER_MENU_BAR.View.Split TabSpace",
                    action = { changeSplitViewMode(SplitViewMode.HORIZONTAL) },
                    shortcut = KeyShortcut(Key.H , ctrl = true, shift = true)
                ),
                Command(text = "Vertical",
                    path = "$MENU_IDENTIFIER_MENU_BAR.View.Split TabSpace",
                    action = { changeSplitViewMode(SplitViewMode.VERTICAL) },
                    shortcut = KeyShortcut(Key.V , ctrl = true, shift = true)
                ),
                Command(text = "Unsplit",
                    path = "$MENU_IDENTIFIER_MENU_BAR.View.Split TabSpace",
                    action = { changeSplitViewMode(SplitViewMode.UNSPLIT) },
                    shortcut = KeyShortcut(Key.U , ctrl = true, shift = true)
                ),
            )
        )
    }

    fun getNextKey():Int = uniqueKey++

    fun registerEditor(key: String, editor: WorkbenchModule<*>){
        var editors = registeredEditors[key]
        when (editors) {
            null -> registeredEditors[key] = mutableListOf(editor)
            else -> {
                editors += editor
                registeredEditors[key] = editors
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun createExplorerFromDefault (id: Int) {
        val defaultState = if(registeredDefaultExplorers[id] != null) registeredDefaultExplorers[id]!!as WorkbenchDefaultState<Any> else return
        val explorer = registeredExplorers[defaultState.type] as WorkbenchModule<Any>
        val state = WorkbenchModuleState(id = id, model = defaultState.model, module = explorer, close = ::removeTab, displayType = DisplayType.LEFT)
        addState(state)
    }

    fun getSelectedModule (displayType: DisplayType, moduleType: ModuleType): MutableState<WorkbenchModuleState<*>?> {
        return selectedModules[displayType.ordinal][moduleType.ordinal]
    }

    fun setSelectedModule (state: WorkbenchModuleState<*>) {
        selectedModules[state.displayType.ordinal][state.module.moduleType.ordinal].value = state
        showDrawer(state.displayType)
        if (state.displayType == DisplayType.TAB1 || state.displayType == DisplayType.TAB2)
            currentTabSpace = state.displayType
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
        reselectEditorSpace()
    }

    fun moduleToWindow(module: WorkbenchModuleState<*>) {
        switchDisplayType(module, DisplayType.WINDOW)
        windows += WindowStateAware(position = dragState.getWindowPosition(), modules = listOf(module))
    }

    fun updateModuleState(state: WorkbenchModuleState<*>, updater: (WorkbenchModuleState<*>) -> Unit){
        modules.remove(state)
        updater.invoke(state)
        addState(state)
    }

    fun switchDisplayType(state: WorkbenchModuleState<*>, displayType: DisplayType) {
        updateModuleState(state) {it.displayType = displayType}
        reselectEditorSpace()
    }

    private fun reselectEditorSpace() {
        val tabs1 = modules.filter { it.displayType == DisplayType.TAB1 }
        val tabs2 = modules.filter { it.displayType == DisplayType.TAB2 }
        if (tabs1.isEmpty() || tabs2.isEmpty()) {
            changeSplitViewMode(SplitViewMode.UNSPLIT)
        }
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
        saveAll(modules.filter { it.module.moduleType == moduleType })
        windows.forEach{
            saveAll(it.modules.filter{ it.module.moduleType == moduleType })
        }
    }

    private fun saveAll(modules: List<WorkbenchModuleState<*>>){
        modules.forEach{
            it.onSave()
        }
    }

    private fun changeSplitViewMode (mode: SplitViewMode) {
        if (mode == splitViewMode) return

        splitViewMode = mode

        val tabs1 = modules.filter { it.displayType == DisplayType.TAB1 }
        val tabs2 = modules.filter { it.displayType == DisplayType.TAB2 }

        if (mode == SplitViewMode.UNSPLIT) {
            val m2 = getSelectedModule(DisplayType.TAB2, ModuleType.EDITOR).value
            if (m2 != null) {
                reselectState(m2)
                m2.displayType = DisplayType.TAB1
                setSelectedModule(m2)
                setSelectedModuleNull(DisplayType.TAB2, ModuleType.EDITOR)
            }
            tabs2.forEach { it.displayType = DisplayType.TAB1 }
            currentTabSpace = DisplayType.TAB1
            return
        }

        if (tabs1.isEmpty() && tabs2.size==1 || tabs1.size==1 && tabs2.isEmpty()) return

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

    fun dispatchCommands() {
        var m = menu
        for (c in commands) {
            val path = c.path.split(".")
            if (path.size > 4) return
            for (i in path.indices) {
                if (i==0 && path[0] != MENU_IDENTIFIER_MENU_BAR) break
                m = if (i==0) {
                    menu
                } else {
                    m.getMenu(path[i])
                }
            }
            m.children.add(c)
        }
    }
}

