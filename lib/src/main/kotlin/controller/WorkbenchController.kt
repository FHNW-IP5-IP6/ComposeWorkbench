package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import model.data.MQClient
import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.SplitViewMode
import model.data.enums.isUnsplit
import model.state.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState

@OptIn(ExperimentalSplitPaneApi::class)
@Suppress("UNCHECKED_CAST")
internal class WorkbenchController(appTitle: String) {

    private val model: WorkbenchStaticState = WorkbenchStaticState(appTitle)
    val commandController = WorkbenchCommandController(model, this)
    val mqController = WorkbenchMQDispatcher(model, this)
    val dragController = WorkbenchDragController(this)

    var informationState by mutableStateOf(getDefaultWorkbenchDisplayInformation())
        private set

    //Information and model accessor functions
    fun getAppTitle(): String {
        return model.appTitle
    }

    fun getMainWindow(): WorkbenchWindowState {
        return model.mainWindow
    }

    fun getScrollToIndex(tabRowKey: TabRowKey) = getIndex(informationState.tabRowState[tabRowKey]?.selected, tabRowKey)

    fun getSelectedModule(tabRowKey: TabRowKey) = informationState.tabRowState[tabRowKey]?.selected

    fun getPreviewTitle(tabRowKey: TabRowKey) = informationState.tabRowState[tabRowKey]?.preview

    fun getIndex(module: WorkbenchModuleState<*>?, tabRowKey: TabRowKey): Int {
        val index = getModulesFiltered(tabRowKey).indexOfFirst { it.id == module?.id }
        return index.coerceAtLeast(0)
    }

    fun isUnsaved(state: WorkbenchModuleState<*>): Boolean {
        informationState.unsavedEditors.forEach { entry ->
            if (state.module.modelType == entry.key) {
                if (entry.value.contains(state.dataId ?: state.id)) {
                    return true
                }
            }
        }
        return false
    }

    fun getModulesFiltered(key: TabRowKey): List<WorkbenchModuleState<*>> {
        return informationState.modules.filter {
                    key.displayType == it.displayType
                    && (ModuleType.BOTH == key.moduleType || key.moduleType == it.module.moduleType)
                    && it.window == key.windowState
        }.reversed()
    }

    fun hasModules(tabRowKey: TabRowKey): Boolean{
        return informationState.tabRowState[tabRowKey]?.preview != null || getModulesFiltered(tabRowKey).isNotEmpty()
    }

    //Information state update
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
                        it.onSave()
                    }
                }
            }
        }
        refreshSaveState(informationState.unsavedEditors.toMutableMap())
    }

    fun verifySplitViewMde(tab1: TabRowKey, tab2: TabRowKey) {
        val modulesTab1 = getModulesFiltered(tab1)
        val modulesTab2 = getModulesFiltered(tab2)
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
            val selected = informationState.tabRowState[TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, model.mainWindow)]?.selected
            if(selected != null) {
                reselect(selected)
                selected.displayType = DisplayType.TAB2
                val newInformationState = updateSelection(informationState, TabRowKey(selected), selected)
                informationState = newInformationState.copy(splitViewMode = splitViewMode, currentTabSpace = DisplayType.TAB2)
            }
        } else {
            var newInformationState = informationState
            val selectedTab1 = informationState.tabRowState[TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, model.mainWindow)]?.selected
            if(selectedTab1 == null) {
                val selectedTab2 = informationState.tabRowState[TabRowKey(DisplayType.TAB2, ModuleType.EDITOR, model.mainWindow)]?.selected
                newInformationState = updateSelection(newInformationState, TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, model.mainWindow), selectedTab2)
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
        dragController.removeReverseDropTarget(tabRowKey)
    }

    fun reselect(moduleState: WorkbenchModuleState<*>){
        val tabRowKey = TabRowKey(moduleState)
        val modules = getModulesFiltered(tabRowKey)
        if(modules.size <= 1 && modules.contains(moduleState)) {
            val newInformationState = hideDrawer(informationState,tabRowKey.displayType)
            informationState = updateSelection(newInformationState, tabRowKey, null)
        } else {
            val selected = informationState.tabRowState[tabRowKey]?.selected
            if(selected != null && selected == moduleState){
                val index = getIndex(moduleState, tabRowKey)
                informationState = when(index) {
                    0 -> updateSelection(informationState, tabRowKey, modules[1])
                    else -> updateSelection(informationState, tabRowKey, modules[index - 1])
                }
            }
        }
    }

    fun moduleToWindow(moduleState: WorkbenchModuleState<*>): WorkbenchWindowState {
        val window = WorkbenchWindowState(
            windowState = WindowState(position = dragController.getWindowPosition()),
            hasFocus = true,
            windowHeaderOffset = 0.dp)

        //TODO: ModuleState should be immutable
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
            tabRowStates[tabRowKey] = WorkbenchTabRowState(tabRowKey = tabRowKey, selected = null, preview = title)
        }else {
            tabRowStates[tabRowKey] = informationState.tabRowState[tabRowKey]!!.copy(preview = title)
        }
        informationState = informationState.copy(tabRowState = tabRowStates)
    }

    private fun updateSelection(newInformationState: WorkbenchInformationState, tabRowKey: TabRowKey, moduleState: WorkbenchModuleState<*>?): WorkbenchInformationState {
        val tabRowStates = newInformationState.tabRowState.toMutableMap()
        if (informationState.tabRowState[tabRowKey] == null) {
            tabRowStates[tabRowKey] = WorkbenchTabRowState(tabRowKey = tabRowKey, selected = moduleState, preview = null)
        } else {
            tabRowStates[tabRowKey] = informationState.tabRowState[tabRowKey]!!.copy(selected = moduleState)
        }
        return newInformationState.copy(tabRowState = tabRowStates)
    }

    private fun hideDrawer(newInformationState: WorkbenchInformationState, displayType: DisplayType): WorkbenchInformationState {
        return when(displayType) {
            DisplayType.LEFT -> newInformationState.copy(leftSplitState = SplitPaneState(moveEnabled = false, initialPositionPercentage = 0f))
            DisplayType.BOTTOM -> newInformationState.copy(bottomSplitState = SplitPaneState(moveEnabled = false, initialPositionPercentage = 1f))
            else -> newInformationState
        }
    }

    private fun showDrawer(newInformationState: WorkbenchInformationState, displayType: DisplayType): WorkbenchInformationState {
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
        moduleState.selected()
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
        val windows = informationState.windows.toMutableList()
        moduleState.updateModule(module)
        //copy to ensure correct recompose
        informationState = informationState.copy(modules = modules, windows = windows)
    }

    fun moduleStateSelectorPressed(tabRowKey: TabRowKey, moduleState: WorkbenchModuleState<*>?) {
        informationState = if (tabRowKey.displayType.deselectable && informationState.tabRowState[tabRowKey]?.selected == moduleState) {
            val newInformationState = updateSelection(informationState, tabRowKey, null)
            hideDrawer(newInformationState, tabRowKey.displayType)
        } else {
            val newInformationState = updateSelection(informationState, tabRowKey, moduleState)
            moduleState?.selected()
            showDrawer(newInformationState, tabRowKey.displayType)
        }

    }

    fun <C>requestEditorState(modelType: String, dataId: Int) : WorkbenchModuleState<*>{
        val existingModule = informationState.modules.find {it.dataId == dataId && it.module.modelType==modelType}
        if (existingModule != null) {
            moduleStateSelectorPressed(TabRowKey(existingModule), existingModule)
            return existingModule
        }
        val editors = getRegisteredEditors<C>(modelType)
        val editor = editors[0]
        val mqtt =  MQClient
        val moduleState = WorkbenchModuleState(
            id = getNextKey(),
            window = model.mainWindow,
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
        val explorer = getRegisteredExplorer<C>(modelType)
        val moduleState = WorkbenchModuleState(
            id = id,
            controller = explorerController,
            window = model.mainWindow,
            module = explorer,
            close = { removeModuleState(it) },
            displayType = displayType
        )
        // TODO: implement messaging initialization in WorkbenchModuleState
        explorer.init?.invoke(explorerController, MQClient)
        addModuleState(moduleState)
        return moduleState
    }

    fun createExplorerFromDefault (id: Int) {
        val defaultState = if(model.registeredDefaultExplorers[id] != null) model.registeredDefaultExplorers[id]!!as WorkbenchDefaultState<Any> else return
        val explorer = model.registeredExplorers[defaultState.type] as WorkbenchModule<Any>
        val existingModule = informationState.modules.find {it.controller == defaultState.controller}
        if (existingModule != null) {
            moduleStateSelectorPressed(TabRowKey(existingModule), existingModule)
            return
        }
        val moduleState = WorkbenchModuleState(id = id, controller = defaultState.controller, module = explorer, window = model.mainWindow, close = { removeModuleState(it) }, displayType = DisplayType.LEFT)
        addModuleState(moduleState)
    }

    //Registry specific stuff
    fun getNextKey():Int = model.uniqueKey++

    fun registerEditor(moduleType: String, editor: WorkbenchModule<*>){
        when (val editors = model.registeredEditors[moduleType]) {
            null -> model.registeredEditors[moduleType] = mutableListOf(editor)
            else -> {
                editors += editor
                model.registeredEditors[moduleType] = editors
            }
        }
    }

    fun registerExplorer(moduleType: String, explorer: WorkbenchModule<*>){
        model.registeredExplorers[moduleType] = explorer
    }

    fun <C>addDefaultExplorer(key: String, id: Int, explorerModel: C){
        val explorer = getRegisteredExplorer<C>(key)
        model.registeredDefaultExplorers[id] = WorkbenchDefaultState(explorer.modelType, explorerModel, explorer.title)
    }

    fun <C>getRegisteredExplorer(key: String): WorkbenchModule<C> {
        val explorer = model.registeredExplorers[key]
            ?: throw IllegalStateException("Could not find registered Explorer of type $key")
        return explorer as WorkbenchModule<C>
    }

    fun <C>getRegisteredEditors(key: String): List<WorkbenchModule<C>> {
        val editors = model.registeredEditors[key]
        if (editors == null || editors.isEmpty()) {
            throw IllegalStateException("Could not find registered Editor of type $key")
        }
        return editors as MutableList<WorkbenchModule<C>>
    }

    fun getRegisteredEditors(moduleState: WorkbenchModuleState<*>?): List<WorkbenchModule<*>> {
        if (moduleState == null || moduleState.module.moduleType != ModuleType.EDITOR) return emptyList()
        return getRegisteredEditors<Any>(moduleState.module.modelType)
    }
}