package controller

import androidx.compose.runtime.snapshots.SnapshotStateList
import model.WorkbenchModel
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.SplitViewMode
import model.state.WorkbenchDefaultState
import model.state.WorkbenchModuleState
import model.state.WorkbenchWindowState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState

@OptIn(ExperimentalSplitPaneApi::class)
@Suppress("UNCHECKED_CAST")
internal class WorkbenchController(appTitle: String) {

    val model: WorkbenchModel = WorkbenchModel(appTitle)
    val commandController = WorkbenchCommandController(model, this)
    val displayControllers: MutableMap<DisplayControllerKey, WorkbenchDisplayController> = mutableMapOf()
    val mqController = WorkbenchMQDispatcher(model, commandController)
    val dragController = WorkbenchDragController()

    fun getDisplayController(displayType: DisplayType, moduleType: ModuleType, deselectable: Boolean = false): WorkbenchDisplayController{
        val key = DisplayControllerKey(displayType, moduleType, model.mainWindow)
        if (!displayControllers.containsKey(key)) {
            displayControllers[key] = WorkbenchDisplayController(model = model, displayType = displayType, moduleType = moduleType, windowState = model.mainWindow, deselectable = deselectable){
                handleEmptyTabRow(it)
            }
        }
        return displayControllers[key]!!
    }

    fun getDisplayController(windowState: WorkbenchWindowState): WorkbenchDisplayController {
        val key = DisplayControllerKey(DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState)
        if (!displayControllers.containsKey(key)) {
            displayControllers[key] =  WorkbenchDisplayController(model = model, windowState = windowState, displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, deselectable = false){
                handleEmptyTabRow(it)
            }
        }
        return displayControllers[key]!!
    }

    //Model Accessor functions
    fun getAppTitle(): String {
        return model.appTitle
    }

    fun getBottomSplitState(): SplitPaneState {
        return model.bottomSplitState
    }

    fun getLeftSplitState(): SplitPaneState {
        return model.leftSplitState
    }

    fun getSplitViewMode(): SplitViewMode {
        return model.splitViewMode
    }

    //Window specific stuff
    fun getMainWindow(): WorkbenchWindowState {
        return model.mainWindow
    }

    fun getWindows(): SnapshotStateList<WorkbenchWindowState> {
        return model.windows
    }

    fun removeWindow(window: WorkbenchWindowState){
        model.windows.remove(window)
        dragController.removeReverseDropTarget(window)
        displayControllers.remove(DisplayControllerKey(DisplayType.WINDOW, ModuleType.BOTH, window))
    }

    fun moduleToWindow(moduleState: WorkbenchModuleState<*>): WorkbenchWindowState {
        val window = WorkbenchWindowState(position = dragController.getWindowPosition())
        val displayController = getDisplayController(window)
        displayController.addModuleState(moduleState)
        model.windows += window
        reselectEditorSpace()
        return window
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

    fun <M>requestEditorState(moduleType: String, dataId: Int): WorkbenchModuleState<M> {
        val editors = getRegisteredEditors<M>(moduleType)
        val editor = editors[0]
        val moduleState = WorkbenchModuleState(
            id = getNextKey(),
            window = model.mainWindow,
            dataId = dataId,
            model = editor.loader!!.invoke(dataId),
            module = editor,
            close = { displayControllers[DisplayControllerKey(it)]?.removeModuleState(it) },
            displayType = model.currentTabSpace,
        )
        addModule(moduleState)
        return moduleState
    }

    fun  <M>requestExplorerState(id: Int, moduleType: String, explorerModel: M, displayType: DisplayType): WorkbenchModuleState<M> {
        val explorer = getRegisteredExplorer<M>(moduleType)
        val moduleState = WorkbenchModuleState(
            id = id,
            model = explorerModel,
            window = model.mainWindow,
            module = explorer,
            close = { displayControllers[DisplayControllerKey(it)]?.removeModuleState(it) },
            displayType = displayType
        )
        addModule(moduleState)
        return moduleState
    }

    fun <M>isUnsaved(state: WorkbenchModuleState<M>): Boolean {
        model.unsavedEditors.forEach { entry ->
            if (state.module.modelType == entry.key) {
                if (entry.value.contains(state.dataId ?: state.id)) {
                    return true
                }
            }
        }
        return false;
    }

    private fun <M>onModuleClose(moduleState: WorkbenchModuleState<M>) {
        if(isUnsaved(moduleState)) {
            // return on cancel response
            // save on save response
            // close without saving on discard response
        } else {
            moduleState.onClose()
        }
        // remove module if accepted
    }

    private fun addModule(moduleState: WorkbenchModuleState<*>){
        val displayController = getDisplayController(moduleState.displayType, moduleState.module.moduleType, true)
        displayController.addModuleState(moduleState)
    }

    fun <M>addDefaultExplorer(key: String, id: Int, explorerModel: M){
        val explorer = getRegisteredExplorer<M>(key)
        model.registeredDefaultExplorers[id] = WorkbenchDefaultState(explorer.modelType, explorerModel, explorer.title)
    }

    fun createExplorerFromDefault (id: Int) {
        val defaultState = if(model.registeredDefaultExplorers[id] != null) model.registeredDefaultExplorers[id]!!as WorkbenchDefaultState<Any> else return
        val explorer = model.registeredExplorers[defaultState.type] as WorkbenchModule<Any>
        val moduleState = WorkbenchModuleState(id = id, model = defaultState.model, module = explorer, window = model.mainWindow, close = { displayControllers[DisplayControllerKey(it)]?.removeModuleState(it) }, displayType = DisplayType.LEFT)
        addModule(moduleState)
    }

    fun <M>getRegisteredExplorer(key: String): WorkbenchModule<M> {
        val explorer = model.registeredExplorers[key]
            ?: throw IllegalStateException("Could not find registered Explorer of type $key")
        return explorer as WorkbenchModule<M>
    }

    fun <M>getRegisteredEditors(key: String): MutableList<WorkbenchModule<M>> {
        val editors = model.registeredEditors[key]
        if (editors == null || editors.isEmpty()) {
            throw IllegalStateException("Could not find registered Editor of type $key")
        }
        return editors as MutableList<WorkbenchModule<M>>
    }

    //Handle empty tab rows
    public fun handleEmptyTabRow(displayController: WorkbenchDisplayController){
        when (displayController.displayType) {
            DisplayType.TAB1 -> reselectEditorSpace()
            DisplayType.TAB2 -> reselectEditorSpace()
            DisplayType.WINDOW -> {
                removeWindow(displayController.windowState)
                removeDisplayController(displayController)
            }
            else -> removeDisplayController(displayController)
        }
    }

    private fun removeDisplayController(displayController: WorkbenchDisplayController){
        dragController.removeDropTarget(displayController)
        displayControllers.remove(DisplayControllerKey(displayController))
    }

    //Editor split view specific
    private fun reselectEditorSpace() {
        if (model.splitViewMode == SplitViewMode.UNSPLIT) return
        val tab1 = getDisplayController(DisplayType.TAB1, ModuleType.EDITOR)
        val tab2 = getDisplayController(DisplayType.TAB2, ModuleType.EDITOR)
        if (tab1.getModulesFiltered().isEmpty() || tab2.getModulesFiltered().isEmpty()){
            unsplit(tab1, tab2)
        }
    }

    private fun unsplit(tab1: WorkbenchDisplayController, tab2: WorkbenchDisplayController){
        model.splitViewMode = SplitViewMode.UNSPLIT
        if (tab2.getModulesFiltered().isEmpty()){
            //Tab 2 is empty so we remove it
            model.splitViewMode = SplitViewMode.UNSPLIT
            model.currentTabSpace = DisplayType.TAB1
            removeDisplayController(tab2)
        } else {
            val selected = tab2.getSelectedModule()
            tab2.getModulesFiltered().forEach {  it.displayType = DisplayType.TAB1 }
            if (selected != null) {
                tab1.setSelectedModule(selected)
            }
            removeDisplayController(tab2)
            model.currentTabSpace = DisplayType.TAB1
        }
    }

    private fun updateSplitMode(splitViewMode: SplitViewMode, tab1: WorkbenchDisplayController, tab2: WorkbenchDisplayController){
        model.splitViewMode = splitViewMode

        if (tab1.getModulesFiltered().isEmpty() && tab2.getModulesFiltered().size==1
            || tab2.getModulesFiltered().size==1 && tab1.getModulesFiltered().isEmpty()) return

        val m1 = tab1.getSelectedModule()
        val m2 = tab2.getSelectedModule()
        if (m1 != null && m2 == null) {
            tab1.removeModuleState(m1) //change selection of tab1
            tab2.addModuleState(m1)
        }
        if (m1 == null && m2 != null) {
            tab2.removeModuleState(m2)
            tab1.addModuleState(m2)
        }
    }

    fun changeSplitViewMode(splitViewMode: SplitViewMode){
        val tab1 = getDisplayController(DisplayType.TAB1, ModuleType.EDITOR)
        val tab2 = getDisplayController(DisplayType.TAB2, ModuleType.EDITOR)
        when (splitViewMode) {
            SplitViewMode.UNSPLIT -> unsplit(tab1, tab2)
            else -> updateSplitMode(splitViewMode, tab1, tab2)
        }
    }
}

internal class DisplayControllerKey(
    val displayType: DisplayType,
    val moduleType: ModuleType,
    val windowState: WorkbenchWindowState
){
    constructor(moduleState: WorkbenchModuleState<*>) :
            this(moduleState.displayType,
                if(DisplayType.WINDOW == moduleState.displayType) ModuleType.BOTH else moduleState.module.moduleType,
                moduleState.window)

    constructor(displayController: WorkbenchDisplayController) :
            this(displayController.displayType,
                displayController.moduleType,
                displayController.windowState)

    override fun equals(other: Any?): Boolean = (other is DisplayControllerKey)
            && displayType == other.displayType
            && moduleType == other.moduleType
            && windowState == other.windowState

    override fun hashCode(): Int {
        var result = displayType.hashCode()
        result = 31 * result + moduleType.hashCode()
        result = 31 * result + windowState.hashCode()
        return result
    }

}