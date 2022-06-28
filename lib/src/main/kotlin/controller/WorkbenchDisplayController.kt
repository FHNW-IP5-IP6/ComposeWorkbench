package controller

import ICON_ROW_HEIGHT
import TAB_ROW_HEIGHT
import TAB_ROW_WIDTH
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import model.state.DragState
import model.state.PreviewState
import model.state.WindowStateAware
import model.state.WorkbenchModuleState

internal interface WorkbenchDisplayController {

    val displayType: DisplayType
    val moduleType: ModuleType
    val model: WorkbenchModel
    val selectionController: WorkbenchSelectionController
    val previewState: PreviewState

    fun getSelectedModule(): WorkbenchModuleState<*>?

    fun getModulesFiltered(): List<WorkbenchModuleState<*>>

    fun moduleSelectorPressed(module: WorkbenchModuleState<*>?)

    fun isModuleSelected(module: WorkbenchModuleState<*>?): Boolean

    fun removeModuleState(module: WorkbenchModuleState<*>)

    fun getWindow(): WindowStateAware

    fun onModuleDraggedOut(module: WorkbenchModuleState<*>)

    fun onModuleDroppedIn(module: WorkbenchModuleState<*>)

    fun getIndex(module: WorkbenchModuleState<*>?): Int {
        val index = getModulesFiltered().indexOfFirst { it.id == module?.id }
        return index.coerceAtLeast(0)
    }

    fun getDragState(): DragState = model.dragState

    fun getScrollToIndex() = getIndex(getSelectedModule())

    fun getTabRowMinDimension(): Pair<Dp, Dp> {
        if(getModulesFiltered().isNotEmpty() || previewState.hasPreview()) {
            return Pair(TAB_ROW_WIDTH.dp, TAB_ROW_HEIGHT.dp)
        }
        return Pair(4.dp, 4.dp)
    }

    fun getContentDimension(maxSize: DpSize): DpSize {
        return when(moduleType) {
            ModuleType.EXPLORER -> maxSize
            ModuleType.EDITOR -> DpSize(maxSize.width, maxSize.height - ICON_ROW_HEIGHT)
        }
    }

    fun containsModule(module: WorkbenchModuleState<*>): Boolean{
        return getModulesFiltered().contains(module)
    }
}
