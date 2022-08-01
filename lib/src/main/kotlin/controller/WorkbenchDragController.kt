package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import controller.WorkbenchController.dropModule
import controller.WorkbenchController.informationState
import controller.WorkbenchController.moduleToWindow
import controller.WorkbenchController.reselect
import model.data.TabRowKey
import model.data.enums.ModuleType
import model.state.DropTarget
import model.state.WorkbenchDragState
import model.state.WorkbenchInformationState
import model.state.WorkbenchModuleState

internal object WorkbenchDragController {

    var dragState by mutableStateOf(WorkbenchDragState(isDragging = false, positionOnScreen = DpOffset.Zero, module = null, dropTargets = listOf()))
        private set

    //Model state update
    fun addReverseDropTarget(tabRowKey: TabRowKey, bounds: Rect){
        val dropTargets = dragState.dropTargets.toMutableList()
        dropTargets.removeIf { it.isReverse && it.tabRowKey == tabRowKey }
        dropTargets += DropTarget(isReverse = true, bounds = bounds, tabRowKey = tabRowKey)
        dragState = dragState.copy(dropTargets = dropTargets)
    }

    fun addDropTarget(tabRowKey: TabRowKey, bounds: Rect){
        val dropTargets = dragState.dropTargets.toMutableList()
        dropTargets.removeIf { !it.isReverse && it.tabRowKey == tabRowKey }
        dropTargets += DropTarget(isReverse = false, bounds = bounds, tabRowKey = tabRowKey)
        dragState = dragState.copy(dropTargets = dropTargets)
    }

    fun removeReverseDropTarget(tabRowKey: TabRowKey){
        val dropTargets = dragState.dropTargets.toMutableList()
        dropTargets.removeIf { it.tabRowKey.windowState == tabRowKey.windowState }
        dragState = dragState.copy(dropTargets = dropTargets)
    }

    fun reset(){
        dragState = dragState.copy(isDragging = false, module = null, positionOnScreen = DpOffset.Zero)
    }

    fun setDragging(isDragging: Boolean) {
        dragState = dragState.copy(isDragging = isDragging)
    }

    fun setModuleState(moduleState: WorkbenchModuleState<*>? ){
        dragState = dragState.copy(module = moduleState)
    }

    fun setPosition(positionOnScreen: DpOffset) {
        dragState = dragState.copy(positionOnScreen = positionOnScreen)
    }

    fun dropDraggedModule(informationState: WorkbenchInformationState) {
        if(dragState.module != null){
            val reverseDropTarget = dragState.getCurrentReverseDopTarget()
            if(reverseDropTarget == null){
                reselect(dragState.module!!)
                moduleToWindow(dragState.module!!)
            }else {
                val dropTarget = dragState.getCurrentDopTarget(reverseDropTarget.tabRowKey.windowState)
                if(dropTarget != null && isValidDropTarget(dropTarget.tabRowKey)){
                    reselect(dragState.module!!)
                    dropModule(dropTarget, dragState.module!!)
                }
            }
        }
        reset()
    }

    fun isValidDropTarget(tabRowKey: TabRowKey): Boolean {
        val moduleType = dragState.module?.module?.moduleType ?: return false
        return (ModuleType.BOTH == moduleType || ModuleType.BOTH == tabRowKey.moduleType || moduleType == tabRowKey.moduleType) && !informationState.getModulesFiltered(tabRowKey).contains(dragState.module!!)
    }

    // used for testing
    internal fun resetDragState(){
        dragState = WorkbenchDragState(isDragging = false, positionOnScreen = DpOffset.Zero, module = null, dropTargets = listOf())
    }
}