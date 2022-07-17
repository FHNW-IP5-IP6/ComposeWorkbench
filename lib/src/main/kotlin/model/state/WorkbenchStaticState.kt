package model.state

import androidx.compose.runtime.mutableStateListOf
import model.data.Command
import model.data.MenuEntry
import model.data.WorkbenchModule
import model.data.enums.MenuType
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi

/**
 * State which holds all static workbench Data which do not change
 *
 * Do not use this to for displaying purposes
 */
@OptIn(ExperimentalSplitPaneApi::class)
internal class WorkbenchStaticState(val appTitle: String = "") {

    var commands = mutableStateListOf<Command>()
    var commandsMenus = mutableMapOf<MenuType, MenuEntry>()

    val registeredExplorers = mutableMapOf<String, WorkbenchModule<*>>()
    val registeredDefaultExplorers = mutableMapOf<Int, WorkbenchDefaultState<*>>()
    val registeredEditors = mutableMapOf<String, MutableList<WorkbenchModule<*>>>()

    var mainWindow = getMainWorkbenchWindowState() // keeps track of the default window position

    var uniqueKey = 0

    init {
        MenuType.values().forEach {
            commandsMenus[it] = MenuEntry(it.name)
        }
    }
}

