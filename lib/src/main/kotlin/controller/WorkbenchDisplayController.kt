package controller

import ICON_ROW_HEIGHT
import TAB_ROW_HEIGHT
import TAB_ROW_WIDTH
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import model.WorkbenchModel
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.state.*
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState

@OptIn(ExperimentalSplitPaneApi::class)
internal class WorkbenchDisplayController(
    val model: WorkbenchModel,
    val displayType: DisplayType,
    val moduleType: ModuleType,
    val windowState: WorkbenchWindowState,
    val previewState: WorkbenchPreviewState = WorkbenchPreviewState(),
    private val selectionState: WorkbenchSelectionState = WorkbenchSelectionState(),
    private val deselectable: Boolean = false,
    val removeMe: (WorkbenchDisplayController) -> Unit
){

    init {
        if (getModulesFiltered().isEmpty()) hideDrawer(displayType)
    }

    fun acceptsModuleOfType(moduleType: ModuleType): Boolean {
        return ModuleType.BOTH == this.moduleType || this.moduleType == moduleType
    }

    fun getDragState(): WorkbenchDragState = model.workbenchDragState

    fun getScrollToIndex() = getIndex(getSelectedModule())

    fun getIndex(module: WorkbenchModuleState<*>?): Int {
        val index = getModulesFiltered().indexOfFirst { it.id == module?.id }
        return index.coerceAtLeast(0)
    }

    fun getTabRowMinDimension(): Pair<Dp, Dp> {
        if (getModulesFiltered().isNotEmpty() || previewState.hasPreview()) {
            return Pair(TAB_ROW_WIDTH.dp, TAB_ROW_HEIGHT.dp)
        }
        return Pair(4.dp, 4.dp)
    }

    fun getContentDimension(maxSize: DpSize): DpSize {
        return when (moduleType) {
            ModuleType.EXPLORER -> maxSize
            ModuleType.EDITOR -> DpSize(maxSize.width, maxSize.height - ICON_ROW_HEIGHT)
            ModuleType.BOTH -> maxSize
        }
    }

    fun containsModule(module: WorkbenchModuleState<*>): Boolean {
        return getModulesFiltered().contains(module)
    }

    fun updateAndRefreshState(state: WorkbenchModuleState<*>, updater: (WorkbenchModuleState<*>) -> Unit) {
        model.modules.remove(state)
        updater.invoke(state)
        model.modules += state
    }

    fun getSelectedModule (): WorkbenchModuleState<*>? {
        return selectionState.selectedModuleState
    }

    fun setSelectedModule (state: WorkbenchModuleState<*>) {
        selectionState.selectedModuleState = state
        showDrawer(state.displayType)
        if (state.displayType == DisplayType.TAB1 || state.displayType == DisplayType.TAB2)
            model.currentTabSpace = state.displayType
    }

    fun setSelectedModuleNull() {
        selectionState.selectedModuleState = null
        hideDrawer(displayType)
    }

    fun getModulesFiltered(): List<WorkbenchModuleState<*>> {
        return model.modules.filter {
                    it.displayType == displayType
                    && (moduleType == ModuleType.BOTH || it.module.moduleType == moduleType)
                    && it.window == windowState
        }.reversed()
    }

    fun moduleStateSelectorPressed(moduleState: WorkbenchModuleState<*>?) {
        if (deselectable && getSelectedModule() == moduleState) {
            setSelectedModuleNull()
        } else {
            setSelectedModule(moduleState!!)
        }
    }

    fun reselectModuleState(moduleState: WorkbenchModuleState<*>) {
        val selected = getSelectedModule()
        val filteredStates = model.modules.filter { it.displayType == moduleState.displayType && it.module.moduleType == moduleState.module.moduleType }.reversed()
        if (filteredStates.size <= 1) {
            setSelectedModuleNull()
        } else if(selected == null || selected.id == moduleState.id){
            when (val idx = filteredStates.indexOfFirst { it.id == moduleState.id}.coerceAtLeast(0)) {
                0 -> {
                    setSelectedModule(filteredStates[1])
                }
                else -> {
                    setSelectedModule(filteredStates[idx - 1])
                }
            }
        }
    }

    fun removeModuleState(state: WorkbenchModuleState<*>) {
        reselectModuleState(state)
        model.modules.remove(state)
        if (getModulesFiltered().isEmpty()){
            removeMe(this)
        }
    }

    fun isModuleSelected(module: WorkbenchModuleState<*>?): Boolean {
        return getSelectedModule() != null && getSelectedModule() == module
    }

    fun onModuleDraggedOut(module: WorkbenchModuleState<*>) {
        removeModuleState(module)
    }

    fun onModuleDroppedIn(moduleState: WorkbenchModuleState<*>) {
        if (displayType != moduleState.displayType) {
            addModuleState(moduleState)
        } else if (!isModuleSelected(moduleState)) {
            moduleStateSelectorPressed(moduleState)
        }
    }

    fun addModuleState(moduleState: WorkbenchModuleState<*>) {
        if(model.modules.contains(moduleState)) moduleState.close.invoke(moduleState)
        //TODO: improve this
        if(model.modules.any{ moduleState.model == it.model && !it.isPreview }) return
        moduleState.displayType = displayType
        moduleState.window = windowState
        model.modules += moduleState
        setSelectedModule(moduleState)
    }

    private fun hideDrawer(displayType: DisplayType) {
        when(displayType) {
            DisplayType.LEFT -> model.leftSplitState = SplitPaneState(moveEnabled = false, initialPositionPercentage = 0f)
            DisplayType.BOTTOM -> model.bottomSplitState = SplitPaneState(moveEnabled = false, initialPositionPercentage = 1f)
            else -> Unit
        }
    }

    private fun showDrawer(displayType: DisplayType) {
        when(displayType) {
            DisplayType.LEFT -> model.leftSplitState = SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.25f)
            DisplayType.BOTTOM -> model.bottomSplitState = SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.7f)
            else -> Unit
        }
    }
}