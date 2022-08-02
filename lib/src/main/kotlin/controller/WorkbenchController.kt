package controller

import ActionResult
import ExplorerLocation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import model.data.*
import model.data.enums.*
import model.state.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalSplitPaneApi::class)
@Suppress("UNCHECKED_CAST")
internal class WorkbenchController {

    var uniqueKey: AtomicInteger = AtomicInteger(0)
    var informationState by mutableStateOf(getDefaultWorkbenchDisplayInformation(), policy = neverEqualPolicy())
        private set

    var dragState by mutableStateOf(getDefaultWorkbenchDragState(), policy = neverEqualPolicy())
        private set

    //TODO: is this the right place for this?
    fun dispatchCommands() {
        var m: MenuEntry = informationState.commandsMenus[MenuType.MenuBar]!!
        for (c in informationState.commands) {
            for (path in c.paths) {
                val pathSplit = path.split(".")
                if (pathSplit.size > 4 || pathSplit.isEmpty()) return
                for (i in pathSplit.indices) {
                    if (i == 0 && informationState.commandsMenus[MenuType.valueOf(pathSplit[0])] == null) break
                    m = if (i == 0) {
                        informationState.commandsMenus[MenuType.valueOf(pathSplit[0])]!!
                    } else {
                        m.getMenu(pathSplit[i])
                    }
                }
                m.children.add(c)
                m.children.sortBy { it.index }
            }
        }
    }

    fun getNextKey(): Int = uniqueKey.getAndIncrement()

    fun triggerAction(action: Action) {
        when (action) {
            is WorkbenchAction -> triggerAction(action)
            is DragAndDropAction -> triggerAction(action)
        }
    }

    private fun triggerAction(workbenchAction: WorkbenchAction) {
        synchronized(informationState) {
            val newState = when (workbenchAction) {
                is WorkbenchAction.AddCommand -> addCommand(workbenchAction.command)
                is WorkbenchAction.AddDefaultExplorer -> addDefaultExplorer(
                    workbenchAction.key,
                    workbenchAction.id,
                    workbenchAction.state
                )
                is WorkbenchAction.AddModuleState -> addModuleState(informationState, workbenchAction.moduleState)
                is WorkbenchAction.AddUnsavedModule -> addUnsavedModule(workbenchAction.type, workbenchAction.dataId)
                is WorkbenchAction.ChangeSplitViewMode -> changeSplitViewMode(workbenchAction.splitViewMode)
                is WorkbenchAction.CloseModuleState -> close(workbenchAction.moduleState)
                is WorkbenchAction.CreateExplorerFromDefault -> createExplorerFromDefault(
                    informationState,
                    workbenchAction.id
                )
                is WorkbenchAction.DropModuleState -> dropModule(
                    informationState,
                    workbenchAction.dropTarget,
                    workbenchAction.moduleState
                )
                is WorkbenchAction.HideDrawer -> hideDrawer(informationState, workbenchAction.displayType)
                is WorkbenchAction.InitExplorers -> initExplorers()
                is WorkbenchAction.ModuleToWindow -> moduleToWindow(informationState, workbenchAction.moduleState)
                is WorkbenchAction.RegisterEditor -> registerEditor(workbenchAction.moduleType, workbenchAction.editor)
                is WorkbenchAction.RegisterExplorer -> registerExplorer(
                    workbenchAction.moduleType,
                    workbenchAction.explorer
                )
                is WorkbenchAction.RemoveModuleState -> removeModuleState(workbenchAction.moduleState)
                is WorkbenchAction.RemovePopUp -> removePopUp(workbenchAction.tabRowKey)
                is WorkbenchAction.RemoveSavedModule -> removeSavedModule(workbenchAction.type, workbenchAction.dataId)
                is WorkbenchAction.RemoveWindow -> removeWindow(workbenchAction.tabRowKey)
                is WorkbenchAction.RequestEditorState -> requestEditorState(
                    workbenchAction.type,
                    workbenchAction.dataId
                )
                is WorkbenchAction.ReselectModuleState -> reselect(workbenchAction.moduleState)
                is WorkbenchAction.SaveAll -> saveAll()
                is WorkbenchAction.SaveModuleState -> save(workbenchAction.moduleState, workbenchAction.action)
                is WorkbenchAction.SetAppTitle -> setAppTitle(workbenchAction.appTitle)
                is WorkbenchAction.SetPopUp -> setPopUp(workbenchAction.tabRowKey, workbenchAction.popUpState)
                is WorkbenchAction.ShowDrawer -> showDrawer(informationState, workbenchAction.displayType)
                is WorkbenchAction.TabSelectorPressed -> moduleStateSelectorPressed(
                    workbenchAction.tabRowKey,
                    workbenchAction.moduleState
                )
                is WorkbenchAction.UpdateCurrentTabSpace -> updateCurrentTabSpace(workbenchAction.displayType)
                is WorkbenchAction.UpdateModuleState -> updateModule(
                    workbenchAction.moduleState,
                    workbenchAction.module
                )
                is WorkbenchAction.UpdatePreviewTitle -> updatePreviewTitle(
                    workbenchAction.tabRowKey,
                    workbenchAction.title
                )
                is WorkbenchAction.UpdateSelection -> updateSelection(
                    informationState,
                    workbenchAction.tabRowKey,
                    workbenchAction.moduleState
                )
                is WorkbenchAction.VerifySplitViewMode -> verifySplitViewMode(
                    workbenchAction.tabRowKey1,
                    workbenchAction.tabRowKey2
                )
                is WorkbenchAction.RequestExplorerState -> requestExplorerState(workbenchAction.moduleState)
                is WorkbenchAction.DropDraggedModule -> dropDraggedModule()
            }
            informationState = newState
        }
    }

    /**
     * IMPORTANT!! some actions from the triggerAction(Action) function will call this function
     * - To prevent deadlocks Drag and Drop Actions MUST NEVER trigger any information state actions
     */
    private fun triggerAction(action: DragAndDropAction) {
        synchronized(informationState) {
            val newState = when (action) {
                is DragAndDropAction.SetModuleState -> setModuleState(action.moduleState)
                is DragAndDropAction.AddDropTarget -> addDropTarget(action.tabRowKey, action.bounds)
                is DragAndDropAction.AddReverseDropTarget -> addReverseDropTarget(action.tabRowKey, action.bounds)
                is DragAndDropAction.RemoveReverseDropTarget -> removeReverseDropTarget(action.tabRowKey)
                is DragAndDropAction.Reset -> reset()
                is DragAndDropAction.SetDragging -> setDragging(action.isDragging)
                is DragAndDropAction.SetPosition -> setPosition(action.positionOnScreen)
            }
            dragState = newState
        }
    }

    private fun dropDraggedModule(): WorkbenchInformationState {
        var newInformationState = informationState
        if (dragState.module != null) {
            val module = dragState.module as WorkbenchModuleState<*>
            val reverseDropTarget = dragState.getCurrentReverseDopTarget()
            if (reverseDropTarget == null) {
                newInformationState = reselect(module)
                newInformationState = moduleToWindow(newInformationState, module)
            } else {
                val dropTarget = dragState.getCurrentDopTarget(reverseDropTarget.tabRowKey.windowState)
                if (dropTarget != null && dragState.isValidDropTarget(dropTarget.tabRowKey, informationState)) {
                    newInformationState = reselect(module)
                    newInformationState = dropModule(newInformationState, dropTarget, module)
                }
            }
        }
        triggerAction(DragAndDropAction.Reset())
        return newInformationState
    }

    private fun addReverseDropTarget(tabRowKey: TabRowKey, bounds: Rect): WorkbenchDragState {
        val dropTargets = dragState.dropTargets.toMutableList()
        dropTargets.removeIf { it.isReverse && it.tabRowKey == tabRowKey }
        dropTargets += DropTarget(isReverse = true, bounds = bounds, tabRowKey = tabRowKey)
        return dragState.copy(dropTargets = dropTargets)
    }

    private fun addDropTarget(tabRowKey: TabRowKey, bounds: Rect): WorkbenchDragState {
        val dropTargets = dragState.dropTargets.toMutableList()
        dropTargets.removeIf { !it.isReverse && it.tabRowKey == tabRowKey }
        dropTargets += DropTarget(isReverse = false, bounds = bounds, tabRowKey = tabRowKey)
        return dragState.copy(dropTargets = dropTargets)
    }

    private fun removeReverseDropTarget(tabRowKey: TabRowKey): WorkbenchDragState {
        val dropTargets = dragState.dropTargets.toMutableList()
        dropTargets.removeIf { it.tabRowKey.windowState == tabRowKey.windowState }
        return dragState.copy(dropTargets = dropTargets)
    }

    private fun reset(): WorkbenchDragState {
        return dragState.copy(isDragging = false, module = null, positionOnScreen = DpOffset.Zero)
    }

    private fun setDragging(isDragging: Boolean): WorkbenchDragState {
        return dragState.copy(isDragging = isDragging)
    }

    private fun setModuleState(moduleState: WorkbenchModuleState<*>?): WorkbenchDragState {
        return dragState.copy(module = moduleState)
    }

    private fun setPosition(positionOnScreen: DpOffset): WorkbenchDragState {
        return dragState.copy(positionOnScreen = positionOnScreen)
    }

    private fun addCommand(command: Command): WorkbenchInformationState {
        val commands = informationState.commands.toMutableList()
        commands.add(command)
        return informationState.copy(commands = commands)
    }

    private fun setPopUp(
        tabRowKey: TabRowKey,
        popUpState: PopUpState
    ): WorkbenchInformationState {
        val tabRowStates = informationState.tabRowState.toMutableMap()
        if (informationState.tabRowState[tabRowKey] != null) {
            tabRowStates[tabRowKey] =
                informationState.tabRowState[tabRowKey]!!.copy(popUpState = popUpState)
        }
        return informationState.copy(tabRowState = tabRowStates)
    }

    private fun removePopUp(tabRowKey: TabRowKey): WorkbenchInformationState {
        val tabRowStates = informationState.tabRowState.toMutableMap()
        if (informationState.tabRowState[tabRowKey] != null) {
            tabRowStates[tabRowKey] = informationState.tabRowState[tabRowKey]!!.copy(popUpState = null)
        }
        return informationState.copy(tabRowState = tabRowStates)
    }

    private fun updateCurrentTabSpace(currentTabSpace: DisplayType): WorkbenchInformationState {
        return informationState.copy(currentTabSpace = currentTabSpace)
    }

    private fun refreshSaveState(unsavedEditors: MutableMap<String, MutableSet<Int>>): WorkbenchInformationState {
        // remove type key if set is empty
        unsavedEditors.forEach {
            if (it.value.isEmpty())
                unsavedEditors.remove(it.key)
        }
        return informationState.copy(unsavedEditors = unsavedEditors)
    }

    private fun addUnsavedModule(type: String, dataId: Int): WorkbenchInformationState {
        val unsaved = informationState.unsavedEditors.toMutableMap()
        if (!unsaved.containsKey(type)) {
            unsaved[type] = mutableSetOf()
        }
        unsaved[type]!!.add(dataId)
        return informationState.copy(unsavedEditors = unsaved)
    }

    private fun removeSavedModule(type: String, dataId: Int): WorkbenchInformationState {
        val unsaved = informationState.unsavedEditors.toMutableMap()
        unsaved[type]?.remove(dataId)
        return refreshSaveState(unsaved)
    }

    private fun saveAll(): WorkbenchInformationState {
        informationState.modules.forEach {
            informationState.unsavedEditors.forEach { entry ->
                if (it.module.modelType == entry.key) {
                    if (entry.value.contains(it.dataId ?: it.id)) {
                        save(it) {}
                    }
                }
            }
        }
        return refreshSaveState(informationState.unsavedEditors.toMutableMap())
    }

    private fun close(moduleState: WorkbenchModuleState<*>): WorkbenchInformationState {
        val onSuccess = {
            MQClientImpl.publishClosed(moduleState.module.modelType, moduleState.dataId ?: moduleState.id)
            removePopUp(TabRowKey(moduleState))
        }
        return executeAction({ moduleState.onClose() }, onSuccess) {
            setPopUp(TabRowKey(moduleState), PopUpState(PopUpType.CLOSE_FAILED, it.message) {})
        }
    }

    private fun save(moduleState: WorkbenchModuleState<*>, action: () -> Unit): WorkbenchInformationState {
        val onSuccess = {
            MQClientImpl.publishSaved(moduleState.module.modelType, moduleState.dataId ?: moduleState.id)
            action.invoke()
            removePopUp(TabRowKey(moduleState))
        }
        return executeAction({ moduleState.onSave() }, onSuccess) {
            setPopUp(
                TabRowKey(moduleState),
                PopUpState(PopUpType.SAVE_FAILED, it.message) {}
            )
        }
    }

    private fun executeAction(
        action: () -> ActionResult,
        onSuccess: () -> WorkbenchInformationState,
        onFailure: (ActionResult) -> WorkbenchInformationState
    ): WorkbenchInformationState {
        val actionResult = action.invoke()
        return if (actionResult.successful) {
            onSuccess.invoke()
        } else {
            onFailure.invoke(actionResult)
        }
    }

    private fun verifySplitViewMode(tab1: TabRowKey, tab2: TabRowKey): WorkbenchInformationState {
        val modulesTab1 = informationState.getModulesFiltered(tab1)
        val modulesTab2 = informationState.getModulesFiltered(tab2)
        return if (modulesTab1.isNotEmpty() && modulesTab2.isNotEmpty()) informationState
        else changeSplitViewMode(SplitViewMode.UNSPLIT)
    }

    private fun changeSplitViewMode(splitViewMode: SplitViewMode): WorkbenchInformationState {
        if (splitViewMode == informationState.splitViewMode) {
            return informationState
        }
        if (!splitViewMode.isUnsplit() && !informationState.splitViewMode.isUnsplit()) {
            return informationState.copy(splitViewMode = splitViewMode)
        }
        if (informationState.splitViewMode.isUnsplit()) {
            val selected = informationState.tabRowState[TabRowKey(
                DisplayType.TAB1,
                ModuleType.EDITOR,
                informationState.mainWindow
            )]?.selected
            if (selected != null) {
                reselect(selected)
                selected.displayType = DisplayType.TAB2
                val newInformationState = updateSelection(informationState, TabRowKey(selected), selected)
                return newInformationState.copy(splitViewMode = splitViewMode, currentTabSpace = DisplayType.TAB2)
            }
        } else {
            var newInformationState = informationState
            val selectedTab1 = informationState.tabRowState[TabRowKey(
                DisplayType.TAB1,
                ModuleType.EDITOR,
                informationState.mainWindow
            )]?.selected
            if (selectedTab1 == null) {
                val selectedTab2 = informationState.tabRowState[TabRowKey(
                    DisplayType.TAB2,
                    ModuleType.EDITOR,
                    informationState.mainWindow
                )]?.selected
                newInformationState = updateSelection(
                    newInformationState,
                    TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, informationState.mainWindow),
                    selectedTab2
                )
            }
            val modules = newInformationState.modules.toMutableList()
            modules.forEach { if (DisplayType.TAB2 == it.displayType) it.displayType = DisplayType.TAB1 }
            val currentTabSpace = DisplayType.TAB1
            return newInformationState.copy(
                splitViewMode = splitViewMode,
                currentTabSpace = currentTabSpace,
                modules = modules
            )
        }
        return informationState
    }

    private fun removeWindow(tabRowKey: TabRowKey): WorkbenchInformationState {
        val windows = informationState.windows.toMutableList()
        windows -= tabRowKey.windowState
        triggerAction(DragAndDropAction.RemoveReverseDropTarget(tabRowKey))
        return informationState.copy(windows = windows)
    }

    private fun reselect(moduleState: WorkbenchModuleState<*>): WorkbenchInformationState {
        val tabRowKey = TabRowKey(moduleState)
        val modules = informationState.getModulesFiltered(tabRowKey)
        return if (modules.size <= 1 && modules.contains(moduleState)) {
            val newInformationState = hideDrawer(informationState, tabRowKey.displayType)
            updateSelection(newInformationState, tabRowKey, null)
        } else {
            val selected = informationState.tabRowState[tabRowKey]?.selected
            if (selected != null && selected == moduleState) {
                when (val index = informationState.getIndex(moduleState.id, tabRowKey)) {
                    0 -> updateSelection(informationState, tabRowKey, modules[1])
                    else -> updateSelection(informationState, tabRowKey, modules[index - 1])
                }
            } else {
                informationState
            }
        }
    }

    private fun moduleToWindow(newInformationState: WorkbenchInformationState ,moduleState: WorkbenchModuleState<*>): WorkbenchInformationState {
        val window = WorkbenchWindowState(
            windowState = WindowState(position = dragState.getWindowPosition()),
            hasFocus = true,
            windowHeaderOffset = 0.dp
        )

        moduleState.displayType = DisplayType.WINDOW
        moduleState.window = window

        val windows = newInformationState.windows.toMutableList()
        windows += window
        val result = updateSelection(newInformationState, TabRowKey(moduleState), moduleState)
        return result.copy(windows = windows)
    }

    private fun updatePreviewTitle(tabRowKey: TabRowKey, title: String?): WorkbenchInformationState {
        val tabRowStates = informationState.tabRowState.toMutableMap()
        if (informationState.tabRowState[tabRowKey] == null) {
            tabRowStates[tabRowKey] =
                WorkbenchTabRowState(tabRowKey = tabRowKey, selected = null, preview = title, popUpState = null)
        } else {
            tabRowStates[tabRowKey] = informationState.tabRowState[tabRowKey]!!.copy(preview = title)
        }
        return informationState.copy(tabRowState = tabRowStates)
    }

    private fun updateSelection(
        newInformationState: WorkbenchInformationState,
        tabRowKey: TabRowKey,
        moduleState: WorkbenchModuleState<*>?
    ): WorkbenchInformationState {
        val tabRowStates = newInformationState.tabRowState.toMutableMap()
        if (informationState.tabRowState[tabRowKey] == null) {
            tabRowStates[tabRowKey] =
                WorkbenchTabRowState(tabRowKey = tabRowKey, selected = moduleState, preview = null, popUpState = null)
        } else {
            tabRowStates[tabRowKey] = informationState.tabRowState[tabRowKey]!!.copy(selected = moduleState)
        }
        moduleState?.selected()
        return newInformationState.copy(tabRowState = tabRowStates)
    }

    private fun hideDrawer(
        newInformationState: WorkbenchInformationState,
        displayType: DisplayType
    ): WorkbenchInformationState {
        return when (displayType) {
            DisplayType.LEFT -> newInformationState.copy(
                leftSplitState = SplitPaneState(
                    moveEnabled = false,
                    initialPositionPercentage = 0f
                )
            )
            DisplayType.BOTTOM -> newInformationState.copy(
                bottomSplitState = SplitPaneState(
                    moveEnabled = false,
                    initialPositionPercentage = 1f
                )
            )
            else -> newInformationState
        }
    }

    private fun showDrawer(
        newInformationState: WorkbenchInformationState,
        displayType: DisplayType
    ): WorkbenchInformationState {
        return when (displayType) {
            DisplayType.LEFT -> newInformationState.copy(
                leftSplitState = SplitPaneState(
                    moveEnabled = true,
                    initialPositionPercentage = 0.25f
                )
            )
            DisplayType.BOTTOM -> newInformationState.copy(
                bottomSplitState = SplitPaneState(
                    moveEnabled = true,
                    initialPositionPercentage = 0.7f
                )
            )
            else -> newInformationState
        }
    }

    private fun addModuleState(
        newInformationState: WorkbenchInformationState,
        moduleState: WorkbenchModuleState<*>
    ): WorkbenchInformationState {
        val modules = newInformationState.modules.toMutableList()
        modules += moduleState
        var result = updateSelection(newInformationState, TabRowKey(moduleState), moduleState)
        result = showDrawer(result, moduleState.displayType)
        return result.copy(modules = modules)
    }

    private fun removeModuleState(moduleState: WorkbenchModuleState<*>): WorkbenchInformationState {
        reselect(moduleState)
        val modules = informationState.modules.toMutableList()
        modules -= moduleState
        return informationState.copy(modules = modules)
    }

    private fun dropModule(newInformationState: WorkbenchInformationState, dropTarget: DropTarget, moduleState: WorkbenchModuleState<*>): WorkbenchInformationState {
        val moduleStates = newInformationState.modules.toMutableList()
        //remove and add module state to ensure it's at the beginning of the tab row
        moduleStates -= moduleState
        moduleState.window = dropTarget.tabRowKey.windowState
        moduleState.displayType = dropTarget.tabRowKey.displayType
        moduleStates += moduleState
        var result = newInformationState.copy(modules = moduleStates)
        result = showDrawer(result, moduleState.displayType)
        return updateSelection(result, TabRowKey(moduleState), moduleState)
    }

    private fun updateModule(
        moduleState: WorkbenchModuleState<*>,
        module: WorkbenchModule<*>
    ): WorkbenchInformationState {
        val modules = informationState.modules.toMutableList()
        moduleState.updateModule(module)
        return informationState.copy(modules = modules)
    }

    private fun moduleStateSelectorPressed(
        tabRowKey: TabRowKey,
        moduleState: WorkbenchModuleState<*>?
    ): WorkbenchInformationState {
        return if (tabRowKey.displayType.deselectable && informationState.tabRowState[tabRowKey]?.selected == moduleState) {
            val newInformationState = updateSelection(informationState, tabRowKey, null)
            hideDrawer(newInformationState, tabRowKey.displayType)
        } else {
            val newInformationState = updateSelection(informationState, tabRowKey, moduleState)
            showDrawer(newInformationState, tabRowKey.displayType)
        }
    }

    private fun requestEditorState(modelType: String, dataId: Int): WorkbenchInformationState {
        val existingModule = informationState.modules.find { it.dataId == dataId && it.module.modelType == modelType }
        if (existingModule != null) {
            return moduleStateSelectorPressed(TabRowKey(existingModule), existingModule)
        }
        val editors = informationState.getRegisteredEditors<Any>(modelType)
        val editor = editors[0]
        val mqtt = MQClientImpl
        val moduleState = WorkbenchModuleState(
            id = getNextKey(),
            window = informationState.mainWindow,
            dataId = dataId,
            controller = editor.loader!!.invoke(dataId, mqtt),
            module = editor,
            close = { removeModuleState(it) },
            displayType = informationState.currentTabSpace,
        )
        return addModuleState(informationState, moduleState)
    }

    private fun requestExplorerState(moduleState: WorkbenchModuleState<*>): WorkbenchInformationState {
        val existingModule =
            informationState.modules.find { it.controller == moduleState.controller && it.module.modelType == moduleState.module.modelType }
        if (existingModule != null) {
            return moduleStateSelectorPressed(TabRowKey(existingModule), existingModule)
        }
        fun <C> init() {
            val state = moduleState as WorkbenchModuleState<C>
            val module = moduleState.module
            module.init?.invoke(state.controller, MQClientImpl)
        }
        init<Any>()
        return addModuleState(informationState, moduleState)
    }

    private fun initExplorers(): WorkbenchInformationState {
        var newInformationState = informationState
        informationState.registeredDefaultExplorers.forEach { (t, u) ->
            if (u.shown) {
                newInformationState = createExplorerFromDefault(newInformationState, t)
            }
        }
        //TODO: invoke module init for all created
        val registeredDefaultExplorers = newInformationState.registeredDefaultExplorers.toMutableMap()
        val toRemove = registeredDefaultExplorers.filter { !it.value.listed }.keys
        toRemove.map { registeredDefaultExplorers.remove(it) }
        return newInformationState.copy(registeredDefaultExplorers = registeredDefaultExplorers)
    }

    private fun createExplorerFromDefault(
        newInformationState: WorkbenchInformationState,
        id: Int
    ): WorkbenchInformationState {
        val defaultState =
            if (newInformationState.registeredDefaultExplorers[id] != null) newInformationState.registeredDefaultExplorers[id]!! as WorkbenchDefaultState<Any> else return newInformationState
        val explorer = newInformationState.registeredExplorers[defaultState.type] as WorkbenchModule<Any>
        val existingModule = newInformationState.modules.find { it.controller == defaultState.controller }
        if (existingModule != null) {
            return moduleStateSelectorPressed(TabRowKey(existingModule), existingModule)
        }
        val moduleState = WorkbenchModuleState(
            id = id,
            controller = defaultState.controller,
            module = explorer,
            window = newInformationState.mainWindow,
            close = { removeModuleState(it) },
            displayType = if (defaultState.location == ExplorerLocation.LEFT) DisplayType.LEFT else DisplayType.BOTTOM,
        )
        return addModuleState(newInformationState, moduleState)
    }

    //Registry specific
    private fun registerEditor(moduleType: String, editor: WorkbenchModule<*>): WorkbenchInformationState {
        val registeredEditors = informationState.registeredEditors.toMutableMap()
        when (val editors = registeredEditors[moduleType]) {
            null -> registeredEditors[moduleType] = mutableListOf(editor)
            else -> {
                editors += editor
                registeredEditors[moduleType] = editors
            }
        }
        return informationState.copy(registeredEditors = registeredEditors)
    }

    private fun registerExplorer(moduleType: String, explorer: WorkbenchModule<*>): WorkbenchInformationState {
        val registeredExplorers = informationState.registeredExplorers.toMutableMap()
        registeredExplorers[moduleType] = explorer
        return informationState.copy(registeredExplorers = registeredExplorers)
    }

    private fun addDefaultExplorer(key: String, id: Int, state: WorkbenchDefaultState<*>): WorkbenchInformationState {
        val registeredDefaultExplorers = informationState.registeredDefaultExplorers.toMutableMap()
        registeredDefaultExplorers[id] = state
        return informationState.copy(registeredDefaultExplorers = registeredDefaultExplorers)
    }

    private fun setAppTitle(appTitle: String): WorkbenchInformationState {
        return informationState.copy(appTitle = appTitle)
    }
}