package controller

import ActionResult
import ExplorerLocation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
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

    private val stateUpdateLock = Any()
    var uniqueKey: AtomicInteger = AtomicInteger(0)
    var informationState by mutableStateOf(getDefaultWorkbenchDisplayInformation())
        private set

    var dragState by mutableStateOf(getDefaultWorkbenchDragState())
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
            val newState = when (workbenchAction) {
                is WorkbenchActionSync.AddCommand -> addCommand(workbenchAction.command)
                is WorkbenchActionSync.AddDefaultExplorer -> addDefaultExplorer(
                    workbenchAction.id,
                    workbenchAction.state
                )
                is WorkbenchAction.AddModuleState -> addModuleState(informationState, workbenchAction.moduleState)
                is WorkbenchAction.AddUnsavedModule -> addUnsavedModule(
                    workbenchAction.type,
                    workbenchAction.dataId
                )
                is WorkbenchAction.ChangeSplitViewMode -> changeSplitViewMode(workbenchAction.splitViewMode)
                is WorkbenchAction.CloseModuleState -> close(workbenchAction.moduleState)
                is WorkbenchAction.CreateExplorerFromDefault -> createExplorerFromDefault(
                    informationState,
                    workbenchAction.id
                )
                is WorkbenchAction.HideDrawer -> hideDrawer(informationState, workbenchAction.displayType)
                is WorkbenchAction.InitExplorers -> initExplorers()
                is WorkbenchAction.ModuleToWindow -> moduleToWindow(informationState, workbenchAction.moduleState)
                is WorkbenchActionSync.RegisterEditor -> registerEditor(
                    workbenchAction.moduleType,
                    workbenchAction.editor
                )
                is WorkbenchActionSync.RegisterExplorer -> registerExplorer(
                    workbenchAction.moduleType,
                    workbenchAction.explorer
                )
                is WorkbenchAction.RemoveModuleState -> removeModuleState(
                    informationState,
                    workbenchAction.moduleState
                )
                is WorkbenchAction.RemovePopUp -> removePopUp(workbenchAction.tabRowKey)
                is WorkbenchAction.RemoveSavedModule -> removeSavedModule(
                    workbenchAction.type,
                    workbenchAction.dataId
                )
                is WorkbenchAction.RemoveWindow -> removeWindow(informationState, workbenchAction.tabRowKey)
                is WorkbenchActionSync.RequestEditorState -> requestEditorState(
                    workbenchAction.type,
                    workbenchAction.dataId
                )
                is WorkbenchAction.ReselectModuleState -> reselect(informationState, workbenchAction.moduleState)
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
                is WorkbenchAction.UpdateEditor -> updateEditor(
                    workbenchAction.moduleState,
                    workbenchAction.module
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
                is WorkbenchActionSync.RequestExplorerState -> requestExplorerState(workbenchAction.moduleState)
                is WorkbenchAction.DropDraggedModule -> dropDraggedModule()
            }
            informationState = newState

    }

    /**
     * IMPORTANT!! some actions from the triggerAction(Action) function will call this function
     * - To prevent deadlocks Drag and Drop Actions MUST NEVER trigger any information state actions
     */
    private fun triggerAction(action: DragAndDropAction) {
            val newState = when (action) {
                is DragAndDropAction.AddDropTarget -> addDropTarget(action.tabRowKey, action.bounds, action.isReverse)
                is DragAndDropAction.Reset -> reset()
                is DragAndDropAction.StartDragging -> startDragging(action.moduleState)
                is DragAndDropAction.SetPosition -> setPosition(action.positionOnScreen)
            }
            dragState = newState
    }

    //Drop target cleanup not working!!
    private fun dropDraggedModule(): WorkbenchInformationState {
        var result = informationState
        if (dragState.module != null) {
            val module = dragState.module as WorkbenchModuleState<*>
            val reverseDropTarget = dragState.getCurrentReverseDopTarget()
            if (reverseDropTarget == null) {
                result = reselect(result, module)
                result = moduleToWindow(result, module)
            } else {
                val dropTarget = dragState.getCurrentDopTarget(reverseDropTarget.tabRowKey.windowState)
                if (dropTarget != null && isValidDropTarget(dropTarget, module)) {
                    result = dropModule(result, dropTarget, module)
                }
            }
        }
        dragState = dragState.copy(isDragging = false, module = null)
        return result.copy(preview = WorkbenchPreviewState(tabRowKey = null, title = ""))
    }

    private fun dropModule(
        newInformationState: WorkbenchInformationState,
        dropTarget: DropTarget,
        moduleState: WorkbenchModuleState<*>
    ): WorkbenchInformationState {
        val result = removeModuleState(newInformationState, moduleState)
        val newState = moduleState.updateLocation(dropTarget.tabRowKey.windowState, dropTarget.tabRowKey.displayType)
        return addModuleState(result,  newState)
    }

    private fun addDropTarget(tabRowKey: TabRowKey, bounds: Rect, isReverse: Boolean): WorkbenchDragState {
        val same = dragState.dropTargets.filter { it.tabRowKey == tabRowKey && it.isReverse == isReverse }
            .find { it.bounds == bounds }
        return if (same != null) {
            dragState
        }else {
            val dropTargets = dragState.dropTargets.toMutableList()
            dropTargets.removeIf { it.isReverse == isReverse && it.tabRowKey == tabRowKey }
            dropTargets += DropTarget(isReverse = isReverse, bounds = bounds, tabRowKey = tabRowKey)
            dragState.copy(dropTargets = dropTargets)
        }
    }

    private fun reset(): WorkbenchDragState {
        informationState = informationState.copy(preview = WorkbenchPreviewState(tabRowKey = null, title = ""))
        return dragState.copy(isDragging = false, module = null)
    }

    private fun startDragging(moduleState: WorkbenchModuleState<*>): WorkbenchDragState {
        dragState.dragWindowState.position = WindowPosition.PlatformDefault
        return dragState.copy(isDragging = true, module = moduleState)
    }

    private fun setPosition(positionOnScreen: DpOffset): WorkbenchDragState {
        val currentDropTarget = dragState.getCurrentDropTarget()
        if(dragState.module != null) {
            if(currentDropTarget != null){
                if (isValidDropTarget(currentDropTarget, dragState.module!!)) {
                    setPreviewIfChanged(informationState, currentDropTarget.tabRowKey, dragState.module!!.getTitle())
                } else {
                    setPreviewIfChanged(informationState, null, "")
                }
            } else {
                 setPreviewIfChanged(informationState, null, "")
            }
        }
        dragState.dragWindowState.position = WindowPosition(positionOnScreen.x, positionOnScreen.y)
        return dragState
    }

    private fun setPreviewIfChanged(newInformationState: WorkbenchInformationState, tabRowKey: TabRowKey?, title: String) {
        if (newInformationState.preview.tabRowKey != tabRowKey || newInformationState.preview.title != title) {
            informationState = newInformationState.copy(preview = WorkbenchPreviewState(tabRowKey = tabRowKey, title = title))
        }
    }

    fun isValidDropTarget(dropTarget: DropTarget ,moduleState: WorkbenchModuleState<*>): Boolean {
        val moduleType = moduleState.module.moduleType
        return (ModuleType.BOTH == moduleType
                || ModuleType.BOTH == dropTarget.tabRowKey.moduleType
                || moduleType == dropTarget.tabRowKey.moduleType)
                && !informationState.getModulesFiltered(dropTarget.tabRowKey).contains(moduleState)
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
            val newInformationState = removePopUp(TabRowKey(moduleState))
            removeModuleState(newInformationState, moduleState)
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
                var result = reselect(informationState, selected)
                selected.displayType = DisplayType.TAB2
                result = updateSelection(result, TabRowKey(selected), selected)
                return result.copy(splitViewMode = splitViewMode, currentTabSpace = DisplayType.TAB2)
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
        cleanupDropTargets(informationState.mainWindow, DisplayType.TAB1)
        cleanupDropTargets(informationState.mainWindow, DisplayType.TAB2)
        return informationState
    }

    private fun removeWindow(newInformationState: WorkbenchInformationState, tabRowKey: TabRowKey): WorkbenchInformationState {
        //TODO: this should handle the on close and action results of each opened editor in window?
        val windows = newInformationState.windows.toMutableList()
        windows -= tabRowKey.windowState
        cleanupDropTargets(tabRowKey.windowState, tabRowKey.displayType)
        return newInformationState.copy(windows = windows)
    }

    private fun cleanupDropTargets(windowState: WorkbenchWindowState, displayType: DisplayType) {
        val dropTargets = dragState.dropTargets.toMutableList()
        dropTargets.removeIf { it.tabRowKey.windowState == windowState && displayType == displayType }
        dragState = dragState.copy(dropTargets = dropTargets)
    }

    private fun reselect(
        newInformationState: WorkbenchInformationState,
        moduleState: WorkbenchModuleState<*>
    ): WorkbenchInformationState {
        val tabRowKey = TabRowKey(moduleState)
        val modules = newInformationState.getModulesFiltered(tabRowKey)
        return if (modules.isEmpty() || modules.size <= 1 && modules.contains(moduleState)) {
            val result = hideDrawer(newInformationState, tabRowKey.displayType)
            updateSelection(result, tabRowKey, null)
        } else {
            val selected = newInformationState.tabRowState[tabRowKey]?.selected
            if (selected != null && selected == moduleState) {
                when (val index = newInformationState.getIndex(moduleState.id, tabRowKey)) {
                    0 -> updateSelection(newInformationState, tabRowKey, modules[1])
                    else -> updateSelection(newInformationState, tabRowKey, modules[index - 1])
                }
            } else {
                newInformationState
            }
        }
    }

    private fun moduleToWindow(
        newInformationState: WorkbenchInformationState,
        moduleState: WorkbenchModuleState<*>
    ): WorkbenchInformationState {
        val window = WorkbenchWindowState(
            windowState = WindowState(position = dragState.getWindowPosition()),
            hasFocus = true,
            windowHeaderOffset = 0.dp
        )
        var result = removeModuleState(newInformationState, moduleState)
        val newState = moduleState.updateLocation(window, DisplayType.WINDOW)
        result = addModuleState(result,  newState)
        val windows = result.windows.toMutableList()
        windows += window
        return result.copy(windows = windows)
    }

    private fun updateSelection(
        newInformationState: WorkbenchInformationState,
        tabRowKey: TabRowKey,
        moduleState: WorkbenchModuleState<*>?
    ): WorkbenchInformationState {
        val tabRowStates = newInformationState.tabRowState.toMutableMap()
        if (informationState.tabRowState[tabRowKey] == null) {
            tabRowStates[tabRowKey] =
                WorkbenchTabRowState(tabRowKey = tabRowKey, selected = moduleState, popUpState = null)
        } else {
            tabRowStates[tabRowKey] = informationState.tabRowState[tabRowKey]!!.copy(selected = moduleState)
        }
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

    private fun removeModuleState(
        newInformationState: WorkbenchInformationState,
        moduleState: WorkbenchModuleState<*>
    ): WorkbenchInformationState {
        var result = reselect(newInformationState, moduleState)
        val modules = result.modules.toMutableList()
        modules -= moduleState
        result = result.copy(modules = modules)
        //remove window if empty
        if (moduleState.window != result.mainWindow && result.getModulesFiltered(TabRowKey(moduleState)).isEmpty()) {
            result = removeWindow(result, TabRowKey(moduleState))
        }
        return result
    }

    private fun updateEditor(
        moduleState: WorkbenchModuleState<*>,
        module: WorkbenchModule<*>
    ): WorkbenchInformationState {
        val modules = informationState.modules.toMutableList()
        val index = modules.indexOf(moduleState).coerceAtLeast(0)
        modules.remove(moduleState)
        val newModule = moduleState.updateModule(module)
        modules.add(index, newModule)
        val result = updateSelection(informationState, TabRowKey(newModule),newModule)
        return result.copy(modules = modules)
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

    private fun addDefaultExplorer(id: Int, state: WorkbenchDefaultState<*>): WorkbenchInformationState {
        val registeredDefaultExplorers = informationState.registeredDefaultExplorers.toMutableMap()
        registeredDefaultExplorers[id] = state
        return informationState.copy(registeredDefaultExplorers = registeredDefaultExplorers)
    }

    private fun setAppTitle(appTitle: String): WorkbenchInformationState {
        return informationState.copy(appTitle = appTitle)
    }
}