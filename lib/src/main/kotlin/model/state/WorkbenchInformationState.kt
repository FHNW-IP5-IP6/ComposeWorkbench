package model.state

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import controller.WorkbenchController
import model.data.*
import model.data.enums.DisplayType
import model.data.enums.MenuType
import model.data.enums.ModuleType
import model.data.enums.SplitViewMode
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState

/**
 * Immutable State which holds all display relevant Data that is subject to changes
 */
@OptIn(ExperimentalSplitPaneApi::class)
internal data class WorkbenchInformationState(
    val modules: List<WorkbenchModuleState<*>>,
    val windows: List<WorkbenchWindowState>,
    val tabRowState: Map<TabRowKey, WorkbenchTabRowState>,
    val unsavedEditors: Map<String, MutableSet<Int>>,
    val splitViewMode: SplitViewMode,
    val currentTabSpace: DisplayType,
    val bottomSplitState: SplitPaneState,
    val leftSplitState: SplitPaneState,
    val commands:List<Command>,
    val commandsMenus: Map<MenuType, MenuEntry>,
    val registeredExplorers: Map<String, WorkbenchModule<*>>,
    val registeredDefaultExplorers: Map<Int, WorkbenchDefaultState<*>>,
    val registeredEditors: Map<String, MutableList<WorkbenchModule<*>>>,
    val mainWindow: WorkbenchWindowState,
    val appTitle: String,
) {

    fun <C> getRegisteredExplorer(key: String): WorkbenchModule<C> {
        val explorer = registeredExplorers[key]
            ?: throw IllegalStateException("Could not find registered Explorer of type $key")
        return explorer as WorkbenchModule<C>
    }

    fun <C> getRegisteredEditors(key: String): List<WorkbenchModule<C>> {
        val editors = registeredEditors[key]
        if (editors == null || editors.isEmpty()) {
            throw IllegalStateException("Could not find registered Editor of type $key")
        }
        return editors as List<WorkbenchModule<C>>
    }

    fun <C> getRegisteredEditors(moduleState: WorkbenchModuleState<*>?): List<WorkbenchModule<C>> {
        if (moduleState == null || moduleState.module.moduleType != ModuleType.EDITOR) return emptyList()
        return getRegisteredEditors(moduleState.module.modelType)
    }

    internal fun hasModules(tabRowKey: TabRowKey): Boolean {
        return tabRowState[tabRowKey]?.preview != null || getModulesFiltered(
            tabRowKey
        ).isNotEmpty()
    }

    internal fun getMenuEntry(type: MenuType): MenuEntry {
        return commandsMenus[type]!!
    }

    internal fun getCommandsForType(type: MenuType): MutableList<MenuItem> {
        if (commandsMenus[type] != null){
            return commandsMenus[type]!!.children
        }
        return mutableListOf()
    }

    internal fun getScrollToIndex(tabRowKey: TabRowKey): Int {
        val id = tabRowState[tabRowKey]?.selected?.id
        return getIndex(id, tabRowKey)
    }

    internal fun getSelectedModule(
        tabRowKey: TabRowKey
    ): WorkbenchModuleState<*>? {
        return tabRowState[tabRowKey]?.selected
    }

    internal fun isShowPopUp(tabRowKey: TabRowKey): Boolean {
        return tabRowState[tabRowKey]?.popUpState != null
    }

    internal fun getPreviewTitle(tabRowKey: TabRowKey): String? {
        return tabRowState[tabRowKey]?.preview
    }

    internal fun getIndex(moduleId: Int?, tabRowKey: TabRowKey): Int {
        val index = getModulesFiltered(tabRowKey).indexOfFirst { it.id == moduleId }
        return index.coerceAtLeast(0)
    }

    internal fun getModulesFiltered(
        key: TabRowKey,
    ): List<WorkbenchModuleState<*>> {
        return modules.filter {
            key.displayType == it.displayType
                    && (ModuleType.BOTH == key.moduleType || key.moduleType == it.module.moduleType)
                    && it.window == key.windowState
        }.reversed()
    }

    internal fun isUnsaved(state: WorkbenchModuleState<*>): Boolean {
        unsavedEditors.forEach { entry ->
            if (state.module.modelType == entry.key) {
                if (entry.value.contains(state.dataId ?: state.id)) {
                    return true
                }
            }
        }
        return false
    }
}

// Initial workbench state
@OptIn(ExperimentalSplitPaneApi::class)
internal fun getDefaultWorkbenchDisplayInformation(): WorkbenchInformationState {
    return WorkbenchInformationState(
        modules = listOf(),
        windows = listOf(),
        tabRowState = mapOf(),
        unsavedEditors = mapOf(),
        splitViewMode = SplitViewMode.UNSPLIT,
        currentTabSpace = DisplayType.TAB1,
        bottomSplitState = SplitPaneState(moveEnabled = true, initialPositionPercentage = 1f),
        leftSplitState =  SplitPaneState(moveEnabled = true, initialPositionPercentage = 0f),
        commands = getWorkbenchCommands(),
        commandsMenus = getWorkbenchMenuEntries(),
        registeredDefaultExplorers = mapOf(),
        registeredEditors = mapOf(),
        registeredExplorers = mapOf(),
        mainWindow = getMainWorkbenchWindowState(),
        appTitle = ""
    )
}

internal fun getWorkbenchMenuEntries(): Map<MenuType, MenuEntry> {
    val commandMenus = mutableMapOf<MenuType, MenuEntry>()
    MenuType.values().forEach {
        commandMenus[it] = MenuEntry(it.name)
    }
    return commandMenus
}

@OptIn(ExperimentalComposeUiApi::class)
internal fun getWorkbenchCommands(): List<Command> {
    val commands = mutableListOf<Command>()
    commands.addAll(
        listOf(
            Command(text = "Save All",
                paths = mutableListOf("${MenuType.MenuBar.name}.File"),
                action = { WorkbenchController.saveAll() },
                shortcut = KeyShortcut(Key.S, ctrl = true, alt = true),
            ),
            Command(text = "Horizontal",
                paths = mutableListOf("${MenuType.MenuBar.name}.View.Split TabSpace"),
                action = { WorkbenchController.changeSplitViewMode(SplitViewMode.HORIZONTAL) },
                shortcut = KeyShortcut(Key.H , ctrl = true, shift = true)
            ),
            Command(text = "Vertical",
                paths = mutableListOf("${MenuType.MenuBar.name}.View.Split TabSpace"),
                action = { WorkbenchController.changeSplitViewMode(SplitViewMode.VERTICAL) },
                shortcut = KeyShortcut(Key.V , ctrl = true, shift = true)
            ),
            Command(text = "Unsplit",
                paths = mutableListOf("${MenuType.MenuBar.name}.View.Split TabSpace"),
                action = { WorkbenchController.changeSplitViewMode(SplitViewMode.UNSPLIT) },
                shortcut = KeyShortcut(Key.U , ctrl = true, shift = true)
            ),
        )
    )
    return commands
}
