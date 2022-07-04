package model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.data.Command
import model.data.MenuEntry
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.MenuType
import model.data.enums.SplitViewMode
import model.state.WorkbenchDefaultState
import model.state.WorkbenchDragState
import model.state.WorkbenchModuleState
import model.state.WorkbenchWindowState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState

@OptIn(ExperimentalSplitPaneApi::class)
internal class WorkbenchModel(val appTitle: String = "") {

    val modules = mutableStateListOf<WorkbenchModuleState<*>>()
    val windows = mutableStateListOf<WorkbenchWindowState>()

    var commands = mutableStateListOf<Command>()
    var commandsMenus = mutableMapOf<MenuType, MenuEntry>()

    var unsavedState by mutableStateOf(false)
    val unsavedEditors = mutableMapOf<String, MutableSet<Int>>()

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

    var mainWindow = WorkbenchWindowState() // keeps track of the default window position
    val workbenchDragState = WorkbenchDragState(mainWindow)

    var uniqueKey = 0
}

