package model

import androidx.compose.runtime.*
import model.data.*
import model.state.DragState
import model.state.WindowStateAware
import model.state.WorkbenchDefaultState
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState

@OptIn(ExperimentalSplitPaneApi::class)
internal class WorkbenchModel(val appTitle: String = "") {
    val selectedModules = Array<Array<MutableState<WorkbenchModuleState<*>?>>>(DisplayType.values().size) {
        Array(
            ModuleType.values().size
        ) { mutableStateOf(null) }
    }

    val modules = mutableStateListOf<WorkbenchModuleState<*>>()
    val windows = mutableStateListOf<WindowStateAware>()

    var commands = mutableStateListOf<Command>()
    var commandsMenus = mutableMapOf<MenuType, MenuEntry>()
    init {
        MenuType.values().forEach {
            commandsMenus[it] = MenuEntry(it.name)
        }
    }

    val registeredExplorers = mutableMapOf<String, WorkbenchModule<*>>()
    val registeredDefaultExplorers = mutableMapOf<Int, WorkbenchDefaultState<*>>()
    val registeredEditors = mutableMapOf<String, MutableList<WorkbenchModule<*>>>()

    var splitViewMode by mutableStateOf(SplitViewMode.UNSPLIT)
    var currentTabSpace = DisplayType.TAB1

    var bottomSplitState by mutableStateOf(SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.7f))
    var leftSplitState by  mutableStateOf(SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.25f))

    var mainWindow = WindowStateAware(modules = emptyList()) // keeps track of the default window position
    val dragState = DragState(mainWindow)

    var uniqueKey = 0
}

