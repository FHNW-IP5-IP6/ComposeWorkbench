package controller

import androidx.compose.runtime.snapshots.SnapshotStateList
import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import model.data.SplitViewMode
import model.data.WorkbenchModule
import model.state.DragState
import model.state.WindowStateAware
import model.state.WorkbenchDefaultState
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.SplitPaneState

internal class WorkbenchController(appTitle: String) {

    private val model: WorkbenchModel = WorkbenchModel(appTitle)
    val selectionController = WorkbenchSelectionController(model)
    val commandController = WorkbenchCommandController(model, selectionController)

    fun createWindowDisplayController(windowState: WindowStateAware): WorkbenchDisplayController {
        return WorkbenchWindowDisplayController(model = model, windowState = windowState, selectionController = selectionController)
    }

    fun createModuleDisplayController(displayType: DisplayType, moduleType: ModuleType, deselectable: Boolean = false): WorkbenchDisplayController{
        return WorkbenchModuleDisplayController(model = model, displayType = displayType, moduleType = moduleType, deselectable = deselectable, selectionController = selectionController)
    }

    //Model Accessor functions
    fun getDragState(): DragState {
        return model.dragState
    }

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
    fun getMainWindow(): WindowStateAware {
        return model.mainWindow
    }

    fun getWindows(): SnapshotStateList<WindowStateAware> {
        return model.windows
    }

    fun removeWindow(window: WindowStateAware){
        model.windows.remove(window)
    }

    fun moduleToWindow(moduleState: WorkbenchModuleState<*>): WindowStateAware {
        selectionController.switchDisplayType(moduleState, DisplayType.WINDOW)
        val window =  WindowStateAware(position = model.dragState.getWindowPosition(), modules = listOf(moduleState))
        model.windows += window
        return window
    }

    //Registry specific stuff
    fun getNextKey():Int = model.uniqueKey++

    fun registerEditor(key: String, editor: WorkbenchModule<*>){
        when (val editors = model.registeredEditors[key]) {
            null -> model.registeredEditors[key] = mutableListOf(editor)
            else -> {
                editors += editor
                model.registeredEditors[key] = editors
            }
        }
    }

    fun registerExplorer(key: String, explorer: WorkbenchModule<*>){
        model.registeredExplorers[key] = explorer
    }

    fun <M>requestEditorState(key: String, dataId: Int){
        val editors = getRegisteredEditors<M>(key)
        val editor = editors[0]
        val moduleState = WorkbenchModuleState(
            id = getNextKey(),
            dataId = dataId,
            model = editor.loader!!.invoke(dataId),
            module = editor,
            close = { selectionController.removeTab(it) },
            displayType = model.currentTabSpace,
        )
        selectionController.addModuleState(moduleState)
    }

    fun  <M>requestExplorerState(id: Int, key: String, explorerModel: M, displayType: DisplayType) {
        val explorer = getRegisteredExplorer<M>(key)
        val state = WorkbenchModuleState(
            id = id,
            model = explorerModel,
            module = explorer,
            close = { selectionController.removeTab(it) },
            displayType = displayType
        )
        selectionController.addModuleState(state)
    }

    fun <M>addDefaultExplorer(key: String, id: Int, explorerModel: M){
        val explorer = getRegisteredExplorer<M>(key)
        model.registeredDefaultExplorers[id] = WorkbenchDefaultState(explorer.modelType, explorerModel, explorer.title)
    }

    fun createExplorerFromDefault (id: Int) {
        val defaultState = if(model.registeredDefaultExplorers[id] != null) model.registeredDefaultExplorers[id]!!as WorkbenchDefaultState<Any> else return
        val explorer = model.registeredExplorers[defaultState.type] as WorkbenchModule<Any>
        val state = WorkbenchModuleState(id = id, model = defaultState.model, module = explorer, close = { selectionController.removeTab(it) }, displayType = DisplayType.LEFT)
        selectionController.addModuleState(state)
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

}