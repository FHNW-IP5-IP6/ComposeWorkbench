package controller

import ActionResult
import ExplorerLocation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
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
internal object WorkbenchController {

    var uniqueKey: AtomicInteger = AtomicInteger(0)
    var informationState by mutableStateOf(getDefaultWorkbenchDisplayInformation(), policy = neverEqualPolicy())
        private set

    fun getNextKey():Int = uniqueKey.getAndIncrement()

    //Information state update
    fun setPopUp( tabRowKey: TabRowKey, type: PopUpType, message: String = "", action: () -> Unit){
        val tabRowStates = informationState.tabRowState.toMutableMap()
        if (informationState.tabRowState[tabRowKey] != null) {
            tabRowStates[tabRowKey] = informationState.tabRowState[tabRowKey]!!.copy(popUpState = PopUpState(type, action, message))
        }
        informationState =  informationState.copy(tabRowState = tabRowStates)
    }

    fun removePopUp(tabRowKey: TabRowKey){
        val tabRowStates = informationState.tabRowState.toMutableMap()
        if (informationState.tabRowState[tabRowKey] != null) {
            tabRowStates[tabRowKey] = informationState.tabRowState[tabRowKey]!!.copy(popUpState = null)
        }
        informationState =  informationState.copy(tabRowState = tabRowStates)
    }

    fun updateCurrentTabSpace(currentTabSpace: DisplayType) {
        informationState = informationState.copy(currentTabSpace = currentTabSpace)
    }

    private fun refreshSaveState(unsavedEditors: MutableMap<String, MutableSet<Int>>) {
        // remove type key if set is empty
        unsavedEditors.forEach{
            if (it.value.isEmpty())
                unsavedEditors.remove(it.key)
        }
        informationState = informationState.copy(unsavedEditors = unsavedEditors)
    }

    fun addUnsavedModule(type: String, dataId: Int) {
        val unsaved = informationState.unsavedEditors.toMutableMap()
        if (!unsaved.containsKey(type)) {
            unsaved[type] = mutableSetOf()
        }
        unsaved[type]!!.add(dataId)
        informationState = informationState.copy(unsavedEditors = unsaved)
    }

    fun removeSavedModule(type: String, dataId: Int) {
        val unsaved = informationState.unsavedEditors.toMutableMap()
        unsaved[type]?.remove(dataId)
        refreshSaveState(unsaved)
    }

    fun saveAll() {
        informationState.modules.forEach {
            informationState.unsavedEditors.forEach { entry ->
                if (it.module.modelType == entry.key) {
                    if (entry.value.contains(it.dataId ?: it.id)) {
                        save(it) {}
                    }
                }
            }
        }
        refreshSaveState(informationState.unsavedEditors.toMutableMap())
    }

    fun close(moduleState: WorkbenchModuleState<*>){
        val onSuccess = {
            MQClientImpl.publishClosed(moduleState.module.modelType, moduleState.dataId ?: moduleState.id)
            removePopUp(TabRowKey(moduleState))}
        executeAction({ moduleState.onClose() }, onSuccess){
            setPopUp(TabRowKey(moduleState), PopUpType.CLOSE_FAILED, it.message, action = {})
        }
    }

    fun save(moduleState: WorkbenchModuleState<*>, action: () -> Unit){
        val onSuccess = {
            MQClientImpl.publishSaved(moduleState.module.modelType, moduleState.dataId ?: moduleState.id)
            action.invoke()
            removePopUp(TabRowKey(moduleState))
        }
        executeAction({ moduleState.onSave() }, onSuccess) { setPopUp(TabRowKey(moduleState), PopUpType.SAVE_FAILED, it.message, action = {}) }
    }

    private fun executeAction(action: () -> ActionResult, onSuccess: () -> Unit, onFailure: (ActionResult) -> Unit) {
        val actionResult = action.invoke()
        if (actionResult.successful) {
            onSuccess.invoke()
        } else {
            onFailure.invoke(actionResult)
        }
    }

    fun verifySplitViewMde(tab1: TabRowKey, tab2: TabRowKey) {
        val modulesTab1 = informationState.getModulesFiltered(tab1)
        val modulesTab2 = informationState.getModulesFiltered(tab2)
        if(modulesTab1.isNotEmpty() && modulesTab2.isNotEmpty()) return
        else changeSplitViewMode(SplitViewMode.UNSPLIT)
    }

    fun changeSplitViewMode(splitViewMode: SplitViewMode){
        if (splitViewMode == informationState.splitViewMode) {
            return
        }
        if (!splitViewMode.isUnsplit() && !informationState.splitViewMode.isUnsplit()){
            informationState = informationState.copy(splitViewMode = splitViewMode)
            return
        }
        if(informationState.splitViewMode.isUnsplit()) {
            val selected = informationState.tabRowState[TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, informationState.mainWindow)]?.selected
            if(selected != null) {
                reselect(selected)
                selected.displayType = DisplayType.TAB2
                val newInformationState = updateSelection(informationState, TabRowKey(selected), selected)
                informationState = newInformationState.copy(splitViewMode = splitViewMode, currentTabSpace = DisplayType.TAB2)
            }
        } else {
            var newInformationState = informationState
            val selectedTab1 = informationState.tabRowState[TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, informationState.mainWindow)]?.selected
            if(selectedTab1 == null) {
                val selectedTab2 = informationState.tabRowState[TabRowKey(DisplayType.TAB2, ModuleType.EDITOR, informationState.mainWindow)]?.selected
                newInformationState = updateSelection(newInformationState, TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, informationState.mainWindow), selectedTab2)
            }
            val modules = newInformationState.modules.toMutableList()
            modules.forEach { if (DisplayType.TAB2 == it.displayType) it.displayType = DisplayType.TAB1}
            val currentTabSpace = DisplayType.TAB1
            informationState = newInformationState.copy(splitViewMode = splitViewMode, currentTabSpace = currentTabSpace, modules = modules)
        }
    }

    fun removeWindow(tabRowKey: TabRowKey){
        val windows = informationState.windows.toMutableList()
        windows -= tabRowKey.windowState
        informationState = informationState.copy(windows = windows)
        WorkbenchDragController.removeReverseDropTarget(tabRowKey)
    }

    fun reselect(moduleState: WorkbenchModuleState<*>){
        val tabRowKey = TabRowKey(moduleState)
        val modules = informationState.getModulesFiltered(tabRowKey)
        if(modules.size <= 1 && modules.contains(moduleState)) {
            val newInformationState = hideDrawer(informationState,tabRowKey.displayType)
            informationState = updateSelection(newInformationState, tabRowKey, null)
        } else {
            val selected = informationState.tabRowState[tabRowKey]?.selected
            if(selected != null && selected == moduleState){
                val index = informationState.getIndex(moduleState.id, tabRowKey)
                informationState = when(index) {
                    0 -> updateSelection(informationState, tabRowKey, modules[1])
                    else -> updateSelection(informationState, tabRowKey, modules[index - 1])
                }
            }
        }
    }

    fun moduleToWindow(moduleState: WorkbenchModuleState<*>): WorkbenchWindowState {
        val window = WorkbenchWindowState(
            windowState = WindowState(position = WorkbenchDragController.dragState.getWindowPosition()),
            hasFocus = true,
            windowHeaderOffset = 0.dp)

        moduleState.displayType = DisplayType.WINDOW
        moduleState.window = window

        val windows = informationState.windows.toMutableList()
        windows += window
        val newInformationState = updateSelection(informationState, TabRowKey(moduleState), moduleState)
        informationState = newInformationState.copy(windows = windows)
        return window
    }

    fun updatePreviewTitle(tabRowKey: TabRowKey, title: String?){
        val tabRowStates = informationState.tabRowState.toMutableMap()
        if(informationState.tabRowState[tabRowKey] == null){
            tabRowStates[tabRowKey] = WorkbenchTabRowState(tabRowKey = tabRowKey, selected = null, preview = title, popUpState = null)
        }else {
            tabRowStates[tabRowKey] = informationState.tabRowState[tabRowKey]!!.copy(preview = title)
        }
        informationState = informationState.copy(tabRowState = tabRowStates)
    }

    private fun updateSelection(newInformationState: WorkbenchInformationState, tabRowKey: TabRowKey, moduleState: WorkbenchModuleState<*>?): WorkbenchInformationState {
        val tabRowStates = newInformationState.tabRowState.toMutableMap()
        if (informationState.tabRowState[tabRowKey] == null) {
            tabRowStates[tabRowKey] = WorkbenchTabRowState(tabRowKey = tabRowKey, selected = moduleState, preview = null, popUpState = null)
        } else {
            tabRowStates[tabRowKey] = informationState.tabRowState[tabRowKey]!!.copy(selected = moduleState)
        }
        moduleState?.selected()
        return newInformationState.copy(tabRowState = tabRowStates)
    }

    fun hideDrawer(newInformationState: WorkbenchInformationState, displayType: DisplayType): WorkbenchInformationState {
        return when(displayType) {
            DisplayType.LEFT -> newInformationState.copy(leftSplitState = SplitPaneState(moveEnabled = false, initialPositionPercentage = 0f))
            DisplayType.BOTTOM -> newInformationState.copy(bottomSplitState = SplitPaneState(moveEnabled = false, initialPositionPercentage = 1f))
            else -> newInformationState
        }
    }

    fun showDrawer(newInformationState: WorkbenchInformationState, displayType: DisplayType): WorkbenchInformationState {
        return when(displayType) {
            DisplayType.LEFT -> newInformationState.copy(leftSplitState = SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.25f))
            DisplayType.BOTTOM -> newInformationState.copy(bottomSplitState = SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.7f))
            else -> newInformationState
        }
    }

    fun addModuleState(moduleState: WorkbenchModuleState<*>){
        val modules = informationState.modules.toMutableList()
        modules += moduleState
        var newInformationState = updateSelection(informationState, TabRowKey(moduleState), moduleState)
        newInformationState = showDrawer(newInformationState, moduleState.displayType)
        informationState = newInformationState.copy(modules = modules)
    }

    fun removeModuleState(moduleState: WorkbenchModuleState<*>){
        reselect(moduleState)
        val modules = informationState.modules.toMutableList()
        modules -= moduleState
        informationState = informationState.copy(modules = modules)
    }

    fun dropModule(dropTarget: DropTarget, moduleState: WorkbenchModuleState<*>){
        val moduleStates = informationState.modules.toMutableList()
        //remove and add module state to ensure it's at the beginning of the tab row
        moduleStates -= moduleState
        moduleState.window = dropTarget.tabRowKey.windowState
        moduleState.displayType = dropTarget.tabRowKey.displayType
        moduleStates += moduleState
        var newInformationState = informationState.copy(modules = moduleStates)
        newInformationState = showDrawer(newInformationState, moduleState.displayType)
        informationState = updateSelection(newInformationState, TabRowKey(moduleState), moduleState)
    }

    fun updateModule(moduleState: WorkbenchModuleState<*>, module: WorkbenchModule<*>){
        val modules = informationState.modules.toMutableList()
        moduleState.updateModule(module)
        informationState = informationState.copy(modules = modules)
    }

    fun moduleStateSelectorPressed(tabRowKey: TabRowKey, moduleState: WorkbenchModuleState<*>?) {
        informationState = if (tabRowKey.displayType.deselectable && informationState.tabRowState[tabRowKey]?.selected == moduleState) {
            val newInformationState = updateSelection(informationState, tabRowKey, null)
            hideDrawer(newInformationState, tabRowKey.displayType)
        } else {
            val newInformationState = updateSelection(informationState, tabRowKey, moduleState)
            showDrawer(newInformationState, tabRowKey.displayType)
        }
    }

    fun <C>requestEditorState(modelType: String, dataId: Int) : WorkbenchModuleState<*>{
        val existingModule = informationState.modules.find {it.dataId == dataId && it.module.modelType==modelType}
        if (existingModule != null) {
            moduleStateSelectorPressed(TabRowKey(existingModule), existingModule)
            return existingModule
        }
        val editors = informationState.getRegisteredEditors<C>(modelType)
        val editor = editors[0]
        val mqtt =  MQClientImpl
        val moduleState = WorkbenchModuleState(
            id = getNextKey(),
            window = informationState.mainWindow,
            dataId = dataId,
            controller = editor.loader!!.invoke(dataId, mqtt),
            module = editor,
            close = { removeModuleState(it) },
            displayType = informationState.currentTabSpace,
        )
        addModuleState(moduleState)
        return moduleState
    }

    fun  <C>requestExplorerState(id: Int, modelType: String, explorerController: C, displayType: DisplayType): WorkbenchModuleState<*> {
        val existingModule = informationState.modules.find {it.controller == explorerController && it.module.modelType==modelType}
        if (existingModule != null) {
            moduleStateSelectorPressed(TabRowKey(existingModule), existingModule)
            return existingModule
        }
        val explorer = informationState.getRegisteredExplorer<C>(modelType)
        val moduleState = WorkbenchModuleState(
            id = id,
            controller = explorerController,
            window = informationState.mainWindow,
            module = explorer,
            close = { removeModuleState(it) },
            displayType = displayType
        )
        // TODO: implement messaging initialization in WorkbenchModuleState
        explorer.init?.invoke(explorerController, MQClientImpl)
        addModuleState(moduleState)
        return moduleState
    }

    fun initExplorers() {
        informationState.registeredDefaultExplorers.forEach { (t, u) ->
            if (u.shown) {
                createExplorerFromDefault (t)
            }
        }
        val registeredDefaultExplorers = informationState.registeredDefaultExplorers.toMutableMap()
        val toRemove = registeredDefaultExplorers.filter { !it.value.listed }.keys
        toRemove.map { registeredDefaultExplorers.remove(it) }
        informationState = informationState.copy(registeredDefaultExplorers =  registeredDefaultExplorers)
    }

    fun createExplorerFromDefault (id: Int) {
        val defaultState = if(informationState.registeredDefaultExplorers[id] != null) informationState.registeredDefaultExplorers[id]!!as WorkbenchDefaultState<Any> else return
        val explorer = informationState.registeredExplorers[defaultState.type] as WorkbenchModule<Any>
        val existingModule = informationState.modules.find {it.controller == defaultState.controller}
        if (existingModule != null) {
            moduleStateSelectorPressed(TabRowKey(existingModule), existingModule)
            return
        }
        val moduleState = WorkbenchModuleState(
            id = id,
            controller = defaultState.controller,
            module = explorer,
            window = informationState.mainWindow,
            close = { removeModuleState(it) },
            displayType = if (defaultState.location == ExplorerLocation.LEFT) DisplayType.LEFT else DisplayType.BOTTOM,
        )
        addModuleState(moduleState)
    }

    //Registry specific
    fun registerEditor(moduleType: String, editor: WorkbenchModule<*>){
        val registeredEditors = informationState.registeredEditors.toMutableMap()
        when (val editors = registeredEditors[moduleType]) {
            null -> registeredEditors[moduleType] = mutableListOf(editor)
            else -> {
                editors += editor
                registeredEditors[moduleType] = editors
            }
        }
        informationState = informationState.copy(registeredEditors = registeredEditors)
    }

    fun registerExplorer(moduleType: String, explorer: WorkbenchModule<*>){
        val registeredExplorers = informationState.registeredExplorers.toMutableMap()
        registeredExplorers[moduleType] = explorer
        informationState = informationState.copy(registeredExplorers = registeredExplorers)
    }

    fun <C>addDefaultExplorer(key: String, id: Int, explorerModel: C, location: ExplorerLocation, shown: Boolean, listed: Boolean){
        val registeredDefaultExplorers = informationState.registeredDefaultExplorers.toMutableMap()
        val explorer = informationState.getRegisteredExplorer<C>(key)
        registeredDefaultExplorers[id] = WorkbenchDefaultState(explorer.modelType, explorerModel, explorer.title, location, shown, listed)
        informationState = informationState.copy(registeredDefaultExplorers = registeredDefaultExplorers)
    }

    fun addCommand(command: Command) {
        val commands = informationState.commands.toMutableList()
        commands.add(command)
        informationState = informationState.copy(commands = commands)
    }

    fun setAppTitle(appTitle: String){
        informationState = informationState.copy(appTitle = appTitle)
    }

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

    // used for testing
    internal fun resetInformationState(){
        informationState = getDefaultWorkbenchDisplayInformation()
    }
}