package controller

import ExplorerLocation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
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

    var uniqueKey: AtomicInteger = AtomicInteger(0)
    var informationState by mutableStateOf(getDefaultWorkbenchDisplayInformation())
        private set
    var dragState by mutableStateOf(getDefaultWorkbenchDragState())
        private set

    fun getNextKey(): Int = uniqueKey.getAndIncrement()

    fun executeAction(action: Action) {
        when (action) {
            is WorkbenchAction -> executeAction(action)
            is DragAndDropAction -> executeAction(action)
        }
    }

    private fun executeAction(workbenchAction: WorkbenchAction) {
        val newState = when (workbenchAction) {
            is WorkbenchAction.DispatchCommands -> informationState.dispatchCommands()
            is WorkbenchActionSync.AddCommand -> informationState.addCommand(workbenchAction.command)
            is WorkbenchActionSync.AddDefaultExplorer -> informationState.addDefaultExplorer(
                workbenchAction.id,
                workbenchAction.state
            )
            is WorkbenchAction.AddModuleState -> informationState.addModuleState(workbenchAction.moduleState)
            is WorkbenchAction.AddUnsavedModule -> informationState.addUnsavedModule(
                workbenchAction.type,
                workbenchAction.dataId
            )
            is WorkbenchAction.ChangeSplitViewMode -> informationState.changeSplitViewMode(workbenchAction.splitViewMode)
            is WorkbenchAction.CloseModuleState -> informationState.close(workbenchAction.moduleState)
            is WorkbenchAction.CreateExplorerFromDefault -> informationState.createExplorerFromDefault(
                workbenchAction.id
            )
            is WorkbenchAction.HideDrawer -> informationState.hideDrawer(workbenchAction.displayType)
            is WorkbenchAction.InitExplorers -> informationState.initExplorers()
            is WorkbenchAction.ModuleToWindow -> informationState.moduleToWindow(workbenchAction.moduleState)
            is WorkbenchActionSync.RegisterEditor -> informationState.registerEditor(
                workbenchAction.moduleType,
                workbenchAction.editor
            )
            is WorkbenchActionSync.RegisterExplorer -> informationState.registerExplorer(
                workbenchAction.moduleType,
                workbenchAction.explorer
            )
            is WorkbenchAction.RemoveModuleState -> informationState.removeModuleState(workbenchAction.moduleState)
            is WorkbenchAction.ClosePopUp -> informationState.closePopUp(workbenchAction.popUpState)
            is WorkbenchAction.RemoveSavedModule -> informationState.removeUnsavedModule(
                workbenchAction.type,
                workbenchAction.dataId
            )
            is WorkbenchAction.RemoveWindow -> informationState.removeWindow(workbenchAction.tabRowKey)
            is WorkbenchActionSync.RequestEditorState -> informationState.requestEditorState(
                workbenchAction.type,
                workbenchAction.dataId
            )
            is WorkbenchAction.ReselectModuleState -> informationState.reselect(workbenchAction.moduleState)
            is WorkbenchAction.SaveAll -> informationState.saveAll()
            is WorkbenchAction.CloseAll -> informationState.closeAllTabs(workbenchAction.windowState)
            is WorkbenchAction.SaveChanges -> informationState.save(workbenchAction.moduleState)
            is WorkbenchAction.SaveAndClose -> informationState.saveAndClose(workbenchAction.moduleState, workbenchAction.popUpState)
            is WorkbenchAction.DiscardChanges -> informationState.discardChanges(workbenchAction.moduleState, workbenchAction.popUpState)
            is WorkbenchAction.SetAppTitle -> informationState.setAppTitle(workbenchAction.appTitle)
            is WorkbenchAction.OpenPopUp -> informationState.openPopUp(
                workbenchAction.popUpType,
                workbenchAction.moduleState,
                workbenchAction.module,
                workbenchAction.message
            )
            is WorkbenchAction.ShowDrawer -> informationState.showDrawer(workbenchAction.displayType)
            is WorkbenchAction.TabSelectorPressed -> informationState.moduleStateSelectorPressed(
                workbenchAction.tabRowKey,
                workbenchAction.moduleState
            )
            is WorkbenchAction.UpdateCurrentTabSpace -> informationState.updateCurrentTabSpace(workbenchAction.displayType)
            is WorkbenchAction.UpdateEditor -> informationState.updateEditor(
                workbenchAction.moduleState,
                workbenchAction.module
            )
            is WorkbenchAction.UpdateSelection ->informationState.updateSelection(
                workbenchAction.tabRowKey,
                workbenchAction.moduleState
            )
            is WorkbenchAction.VerifySplitViewMode -> informationState.verifySplitViewMode(
                workbenchAction.tabRowKey1,
                workbenchAction.tabRowKey2
            )
            is WorkbenchActionSync.RequestExplorerState -> informationState.requestExplorerState(workbenchAction.moduleState)
            is WorkbenchAction.DropDraggedModule -> informationState.dropDraggedModule()
            is WorkbenchAction.SetWindowOffset -> setWindowOffset(workbenchAction.window, workbenchAction.offset)
        }
        informationState = newState
    }

    private fun executeAction(action: DragAndDropAction) {
        val newState = when (action) {
            is DragAndDropAction.AddDropTarget -> addDropTarget(action.tabRowKey, action.bounds, action.isReverse)
            is DragAndDropAction.Reset -> reset()
            is DragAndDropAction.StartDragging -> startDragging(action.moduleState)
            is DragAndDropAction.SetPosition -> setPosition(action.positionOnScreen)
        }
        dragState = newState
    }

    private fun WorkbenchInformationState.dispatchCommands(): WorkbenchInformationState {
        var m: MenuEntry = commandsMenus[MenuType.MenuBar]!!
        for (c in commands) {
            for (path in c.paths) {
                val pathSplit = path.split(".")
                if (pathSplit.size > 4 || pathSplit.isEmpty()) return this
                for (i in pathSplit.indices) {
                    if (i == 0 && commandsMenus[MenuType.valueOf(pathSplit[0])] == null) break
                    m = if (i == 0) {
                        commandsMenus[MenuType.valueOf(pathSplit[0])]!!
                    } else {
                        m.getMenu(pathSplit[i])
                    }
                }
                m.children.add(c)
                m.children.sortBy { it.index }
            }
        }
        return this
    }

    private fun WorkbenchInformationState.saveAndClose(moduleState: WorkbenchModuleState<*>, popUpState: PopUpState): WorkbenchInformationState {
        val result = save(moduleState).closePopUp(popUpState)
        return if (popUpState.type==PopUpType.ON_EDITOR_SWITCH) result.updateEditor(moduleState, popUpState.module) else result.close(moduleState)
    }

    private fun WorkbenchInformationState.discardChanges(moduleState: WorkbenchModuleState<*>, popUpState: PopUpState): WorkbenchInformationState {
        val result = removeUnsavedModule(moduleState.module.modelType, moduleState.dataId ?: moduleState.id).closePopUp(popUpState)
        return if (popUpState.type==PopUpType.ON_EDITOR_SWITCH) result.updateEditor(moduleState, popUpState.module) else result.close(moduleState)
    }

    //TODO: window state should be immutable -> remove from module state and tabrowkey
    private  fun setWindowOffset(windowState: WorkbenchWindowState, offset: Dp): WorkbenchInformationState {
        windowState.windowHeaderOffset = offset
        return informationState
    }

    private fun WorkbenchInformationState.dropDraggedModule(): WorkbenchInformationState {
        var result = this
        if (dragState.module != null) {
            val module = dragState.module as WorkbenchModuleState<*>
            val reverseDropTarget = dragState.getCurrentReverseDopTarget()
            if (reverseDropTarget == null) {
                result = result.reselect(module)
                result = result.moduleToWindow(module)
            } else {
                val dropTarget = dragState.getCurrentDopTarget(reverseDropTarget.tabRowKey.windowState)
                if (dropTarget != null && isValidDropTarget(dropTarget, module)) {
                    result = result.dropModule(dropTarget, module)
                }
            }
        }
        dragState = dragState.copy(isDragging = false, module = null, dropTargets = listOf())
        return result.copy(preview = WorkbenchPreviewState(tabRowKey = null, title = ""))
    }

    private fun WorkbenchInformationState.dropModule(
        dropTarget: DropTarget,
        moduleState: WorkbenchModuleState<*>
    ): WorkbenchInformationState {
        val result = removeModuleState(moduleState)
        val newState = moduleState.updateLocation(dropTarget.tabRowKey.windowState, dropTarget.tabRowKey.displayType)
        return result.addModuleState(newState)
    }

    private fun addDropTarget(tabRowKey: TabRowKey, bounds: Rect, isReverse: Boolean): WorkbenchDragState {
        val same = dragState.dropTargets.filter { it.tabRowKey == tabRowKey && it.isReverse == isReverse }
            .find { it.bounds == bounds }
        return if (same != null) {
            dragState
        }else {
            //check if window still exists - can happen due to channeling
            if(tabRowKey.windowState != informationState.mainWindow && !informationState.windows.contains(tabRowKey.windowState)) return dragState
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

    private fun WorkbenchInformationState.addCommand(command: Command): WorkbenchInformationState {
        val commands = commands.toMutableList()
        commands.add(command)
        return copy(commands = commands)
    }

    private fun WorkbenchInformationState.openPopUp(
        popUpType: PopUpType,
        moduleState: WorkbenchModuleState<*>,
        module: WorkbenchModule<*>,
        message: String
    ): WorkbenchInformationState {
        val popups = popUpList.toMutableSet()
        popups.add(PopUpState(popUpType, moduleState, module, message))
        return copy(popUpList = popups)
    }

    private fun WorkbenchInformationState.closePopUp(popUpState: PopUpState): WorkbenchInformationState {
        val popups = popUpList.toMutableSet()
        popups.remove(popUpState)
        return copy(popUpList = popups)
    }

    private fun WorkbenchInformationState.updateCurrentTabSpace(currentTabSpace: DisplayType): WorkbenchInformationState {
        return copy(currentTabSpace = currentTabSpace)
    }

    private fun WorkbenchInformationState.refreshSaveState(unsavedEditors: MutableMap<String, MutableSet<Int>>): WorkbenchInformationState {
        unsavedEditors.forEach {
            if (it.value.isEmpty())
                unsavedEditors.remove(it.key)
        }
        return copy(unsavedEditors = unsavedEditors)
    }

    private fun WorkbenchInformationState.addUnsavedModule(type: String, dataId: Int): WorkbenchInformationState {
        val unsaved = unsavedEditors.toMutableMap()
        if (!unsaved.containsKey(type)) {
            unsaved[type] = mutableSetOf()
        }
        unsaved[type]!!.add(dataId)
        return copy(unsavedEditors = unsaved)
    }

    private fun WorkbenchInformationState.removeUnsavedModule(type: String, dataId: Int): WorkbenchInformationState {
        val unsaved = unsavedEditors.toMutableMap()
        unsaved[type]?.remove(dataId)
        MQClientImpl.publishSaved(type, dataId)
        return refreshSaveState(unsaved)
    }

    private fun WorkbenchInformationState.closeAllTabs(windowState: WorkbenchWindowState): WorkbenchInformationState {
        var state = this
        modules
            .filter { it.window == windowState }
            .forEach {
                state = state.close(it)
            }
        return state.refreshSaveState(unsavedEditors.toMutableMap())
    }

    private fun WorkbenchInformationState.saveAll(): WorkbenchInformationState {
        var state = this
        modules.forEach {
            unsavedEditors.forEach { entry ->
                if (it.module.modelType == entry.key) {
                    if (entry.value.contains(it.dataId ?: it.id)) {
                        state = state.save(it)
                    }
                }
            }
        }
        return state.refreshSaveState(unsavedEditors.toMutableMap())
    }


    private fun WorkbenchInformationState.close(moduleState: WorkbenchModuleState<*>): WorkbenchInformationState {
        if (isUnsaved(moduleState)) {
            return openPopUp(PopUpType.ON_CLOSE, moduleState, moduleState.module, "")
        }
        MQClientImpl.publishClosed(moduleState.module.modelType, moduleState.dataId ?: moduleState.id)
        return removeModuleState(moduleState)
    }

    private fun WorkbenchInformationState.save(moduleState: WorkbenchModuleState<*>): WorkbenchInformationState {
        return if (moduleState.onSave().successful) {
            removeUnsavedModule(moduleState.module.modelType, moduleState.dataId ?: moduleState.id)
        } else {
            openPopUp(PopUpType.SAVE_FAILED, moduleState, moduleState.module, "Failed to save.")
        }
    }

    private fun WorkbenchInformationState.verifySplitViewMode(tab1: TabRowKey, tab2: TabRowKey): WorkbenchInformationState {
        val modulesTab1 = getModulesFiltered(tab1)
        val modulesTab2 = getModulesFiltered(tab2)
        return if (modulesTab1.isNotEmpty() && modulesTab2.isNotEmpty()) this
        else changeSplitViewMode(SplitViewMode.UNSPLIT)
    }

    private fun WorkbenchInformationState.changeSplitViewMode(splitViewMode: SplitViewMode): WorkbenchInformationState {
        if (splitViewMode == this.splitViewMode) {
            return this
        }
        if (!splitViewMode.isUnsplit() && !this.splitViewMode.isUnsplit()) {
            return copy(splitViewMode = splitViewMode)
        }
        val tab1 = TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, mainWindow)
        val tab2 = TabRowKey(DisplayType.TAB2, ModuleType.EDITOR, mainWindow)
        if (this.splitViewMode.isUnsplit()) {
            val selected = tabRowState[tab1]?.selected
            if (selected != null) {
                var result = reselect(selected)
                selected.displayType = DisplayType.TAB2
                result = result.updateSelection(TabRowKey(selected), selected)
                return result.copy(splitViewMode = splitViewMode, currentTabSpace = DisplayType.TAB2)
            }
        } else {
            var newInformationState = this
            if (tabRowState[tab1]?.selected == null) {
                newInformationState = newInformationState.updateSelection(tab1, tabRowState[tab2]?.selected)
            }
            newInformationState = newInformationState.updateSelection(tab2, null)
            val modules = newInformationState.modules.toMutableList()
            modules.forEach { if (DisplayType.TAB2 == it.displayType) it.displayType = DisplayType.TAB1 }
            val currentTabSpace = DisplayType.TAB1
            return newInformationState.copy(
                splitViewMode = splitViewMode,
                currentTabSpace = currentTabSpace,
                modules = modules
            )
        }
        dragState = dragState.copy(dropTargets = listOf())
        return this
    }

    private fun WorkbenchInformationState.removeWindow(tabRowKey: TabRowKey): WorkbenchInformationState {
        val windows = windows.toMutableList()
        windows -= tabRowKey.windowState
        dragState = dragState.copy(dropTargets = listOf())
        return copy(windows = windows)
    }

    private fun WorkbenchInformationState.reselect(
        moduleState: WorkbenchModuleState<*>
    ): WorkbenchInformationState {
        val tabRowKey = TabRowKey(moduleState)
        val modules = getModulesFiltered(tabRowKey)
        return if (modules.isEmpty() || modules.size <= 1 && modules.contains(moduleState)) {
            val result = hideDrawer(tabRowKey.displayType)
            result.updateSelection(tabRowKey, null)
        } else {
            val selected = tabRowState[tabRowKey]?.selected
            if (selected != null && selected == moduleState) {
                when (val index = getIndex(moduleState.id, tabRowKey)) {
                    0 -> updateSelection(tabRowKey, modules[1])
                    else -> updateSelection(tabRowKey, modules[index - 1])
                }
            } else {
                this
            }
        }
    }

    private fun WorkbenchInformationState.moduleToWindow(
        moduleState: WorkbenchModuleState<*>
    ): WorkbenchInformationState {
        val window = WorkbenchWindowState(
            windowState = WindowState(position = dragState.getWindowPosition()),
            windowHeaderOffset = 0.dp
        )
        var result = removeModuleState(moduleState)
        val newState = moduleState.updateLocation(window, DisplayType.WINDOW)
        result = result.addModuleState(newState)
        val windows = result.windows.toMutableList()
        windows += window
        return result.copy(windows = windows)
    }

    private fun WorkbenchInformationState.updateSelection(
        tabRowKey: TabRowKey,
        moduleState: WorkbenchModuleState<*>?
    ): WorkbenchInformationState {
        val tabRowStates = tabRowState.toMutableMap()
        if (tabRowState[tabRowKey] == null) {
            tabRowStates[tabRowKey] =
                WorkbenchTabRowState(tabRowKey = tabRowKey, selected = moduleState)
        } else {
            tabRowStates[tabRowKey] = tabRowState[tabRowKey]!!.copy(selected = moduleState)
        }
        if(moduleState != null){
            MQClientImpl.publishSelected(moduleState.module.modelType, moduleState.dataId ?: moduleState.id)
        }
        return copy(tabRowState = tabRowStates)
    }

    private fun WorkbenchInformationState.hideDrawer(
        displayType: DisplayType
    ): WorkbenchInformationState {
        return when (displayType) {
            DisplayType.LEFT -> copy(
                leftSplitState = SplitPaneState(
                    moveEnabled = false,
                    initialPositionPercentage = 0f
                )
            )
            DisplayType.BOTTOM -> copy(
                bottomSplitState = SplitPaneState(
                    moveEnabled = false,
                    initialPositionPercentage = 1f
                )
            )
            else -> this
        }
    }

    private fun WorkbenchInformationState.showDrawer(
        displayType: DisplayType
    ): WorkbenchInformationState {
        return when (displayType) {
            DisplayType.LEFT -> copy(
                leftSplitState = SplitPaneState(
                    moveEnabled = true,
                    initialPositionPercentage = 0.25f
                )
            )
            DisplayType.BOTTOM -> copy(
                bottomSplitState = SplitPaneState(
                    moveEnabled = true,
                    initialPositionPercentage = 0.7f
                )
            )
            else -> this
        }
    }

    private fun WorkbenchInformationState.addModuleState(
        moduleState: WorkbenchModuleState<*>
    ): WorkbenchInformationState {
        val modules = modules.toMutableList()
        modules += moduleState
        var result = updateSelection(TabRowKey(moduleState), moduleState)
        result = result.showDrawer(moduleState.displayType)
        return result.copy(modules = modules)
    }

    private fun WorkbenchInformationState.removeModuleState(
        moduleState: WorkbenchModuleState<*>
    ): WorkbenchInformationState {
        var result = reselect(moduleState)
        val modules = result.modules.toMutableList()
        modules -= moduleState
        result = result.copy(modules = modules)
        //remove window if empty
        if (moduleState.window != result.mainWindow && result.getModulesFiltered(TabRowKey(moduleState)).isEmpty()) {
            result = result.removeWindow(TabRowKey(moduleState))
        }
        return result
    }

    private fun WorkbenchInformationState.updateEditor(
        moduleState: WorkbenchModuleState<*>,
        module: WorkbenchModule<*>
    ): WorkbenchInformationState {
        if (isUnsaved(moduleState)) {
            return openPopUp(PopUpType.ON_EDITOR_SWITCH, moduleState, module,  "")
        }
        val modules = modules.toMutableList()
        val index = modules.indexOf(moduleState).coerceAtLeast(0)
        modules.remove(moduleState)
        val newModule = moduleState.updateModule(module)
        modules.add(index, newModule)
        val result = updateSelection(TabRowKey(newModule),newModule)
        return result.copy(modules = modules)
    }

    private fun WorkbenchInformationState.moduleStateSelectorPressed(
        tabRowKey: TabRowKey,
        moduleState: WorkbenchModuleState<*>?
    ): WorkbenchInformationState {
        return if (tabRowKey.displayType.deselectable && tabRowState[tabRowKey]?.selected == moduleState) {
            val newInformationState = updateSelection(tabRowKey, null)
            newInformationState.hideDrawer(tabRowKey.displayType)
        } else {
            val newInformationState = updateSelection(tabRowKey, moduleState)
            newInformationState.showDrawer(tabRowKey.displayType)
        }
    }

    private fun WorkbenchInformationState.requestEditorState(modelType: String, dataId: Int): WorkbenchInformationState {
        val existingModule = modules.find { it.dataId == dataId && it.module.modelType == modelType }
        if (existingModule != null) {
            return moduleStateSelectorPressed(TabRowKey(existingModule), existingModule)
        }
        val editors = getRegisteredEditors<Any>(modelType)
        val editor = editors[0]
        val mqtt = MQClientImpl
        val moduleState = WorkbenchModuleState(
            id = getNextKey(),
            window = mainWindow,
            dataId = dataId,
            controller = editor.loader!!.invoke(dataId, mqtt),
            module = editor,
            displayType = currentTabSpace,
        )
        return addModuleState(moduleState)
    }

    private fun WorkbenchInformationState.requestExplorerState(moduleState: WorkbenchModuleState<*>): WorkbenchInformationState {
        val existingModule =
            modules.find { it.controller == moduleState.controller && it.module.modelType == moduleState.module.modelType }
        if (existingModule != null) {
            return moduleStateSelectorPressed(TabRowKey(existingModule), existingModule)
        }
        return addModuleState(moduleState)
    }

    private fun WorkbenchInformationState.initExplorers(): WorkbenchInformationState {
        var newInformationState = this
        registeredDefaultExplorers.forEach { (t, u) ->
            if (u.shown) {
                newInformationState = newInformationState.createExplorerFromDefault(t)
            }
        }
        val registeredDefaultExplorers = newInformationState.registeredDefaultExplorers.toMutableMap()
        val toRemove = registeredDefaultExplorers.filter { !it.value.listed }.keys
        toRemove.map { registeredDefaultExplorers.remove(it) }
        return newInformationState.copy(registeredDefaultExplorers = registeredDefaultExplorers)
    }

    private fun WorkbenchInformationState.createExplorerFromDefault(
        id: Int
    ): WorkbenchInformationState {
        val defaultState =
            if (registeredDefaultExplorers[id] != null) registeredDefaultExplorers[id]!! as WorkbenchDefaultState<Any> else return this
        val explorer = registeredExplorers[defaultState.type] as WorkbenchModule<Any>
        val existingModule = modules.find { it.controller == defaultState.controller }
        if (existingModule != null) {
            return moduleStateSelectorPressed(TabRowKey(existingModule), existingModule)
        }
        val moduleState = WorkbenchModuleState(
            id = id,
            controller = defaultState.controller,
            module = explorer,
            window = mainWindow,
            displayType = if (defaultState.location == ExplorerLocation.LEFT) DisplayType.LEFT else DisplayType.BOTTOM,
        )
        return addModuleState(moduleState)
    }

    //Registry specific
    private fun WorkbenchInformationState.registerEditor(moduleType: String, editor: WorkbenchModule<*>): WorkbenchInformationState {
        val registeredEditors = registeredEditors.toMutableMap()
        when (val editors = registeredEditors[moduleType]) {
            null -> registeredEditors[moduleType] = mutableListOf(editor)
            else -> {
                editors += editor
                registeredEditors[moduleType] = editors
            }
        }
        return copy(registeredEditors = registeredEditors)
    }

    private fun WorkbenchInformationState.registerExplorer(moduleType: String, explorer: WorkbenchModule<*>): WorkbenchInformationState {
        val registeredExplorers = registeredExplorers.toMutableMap()
        registeredExplorers[moduleType] = explorer
        return copy(registeredExplorers = registeredExplorers)
    }

    private fun WorkbenchInformationState.addDefaultExplorer(id: Int, state: WorkbenchDefaultState<*>): WorkbenchInformationState {
        val registeredDefaultExplorers = registeredDefaultExplorers.toMutableMap()
        registeredDefaultExplorers[id] = state
        return copy(registeredDefaultExplorers = registeredDefaultExplorers)
    }

    private fun WorkbenchInformationState.setAppTitle(appTitle: String): WorkbenchInformationState {
        return copy(appTitle = appTitle)
    }
}