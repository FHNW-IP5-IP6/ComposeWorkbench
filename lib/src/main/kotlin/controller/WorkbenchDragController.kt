package controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.WindowPosition
import model.data.TabRowKey
import model.data.enums.ModuleType
import model.state.DropTarget
import model.state.WorkbenchDragState
import model.state.WorkbenchModuleState
import model.state.WorkbenchWindowState
import util.toOffset

internal class WorkbenchDragController(private val controller: WorkbenchController) {

    var dragState by mutableStateOf(WorkbenchDragState(isDragging = false, positionOnScreen = DpOffset.Zero, module = null, dropTargets = listOf()))
        private set

    //Model accessor functions
    fun getModuleType() = dragState.module?.module?.moduleType

    fun isCurrentDropTarget(tabRowKey: TabRowKey): Boolean {
        val reverseDropTarget = getCurrentReverseDopTarget() ?: return false //If there is no reverse target active the drop target is outside any window
        val dropTarget = getCurrentDopTarget(reverseDropTarget.tabRowKey.windowState)
        return  dropTarget != null && dropTarget.tabRowKey == tabRowKey
    }

    fun getCurrentReverseDopTarget(): DropTarget? {
        val targets = dragState.dropTargets.filter { it.isReverse && it.bounds.contains(dragState.positionOnScreen.toOffset()) }
        return when (targets.isEmpty()){
            true -> null
            false -> {
                //Prioritize the window witch is currently focused
                if (targets.find { it.tabRowKey.windowState.hasFocus } == null) targets.first() else targets.find { it.tabRowKey.windowState.hasFocus }!!
            }
        }
    }

    fun getCurrentDopTarget(windowState: WorkbenchWindowState): DropTarget? {
        return dragState.dropTargets.find { !it.isReverse && it.bounds.contains(dragState.positionOnScreen.toOffset()) && it.tabRowKey.windowState == windowState }
    }

    fun toOffset(windowState: WorkbenchWindowState): Offset {
        val x = dragState.positionOnScreen.x - windowState.windowState.position.x
        val y = dragState.positionOnScreen.y - windowState.windowState.position.y
        return Offset(x.value,y.value)
    }

    fun getWindowPosition(): WindowPosition {
        return WindowPosition(x = dragState.positionOnScreen.x, y = dragState.positionOnScreen.y)
    }

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

    fun dropDraggedModule() {
        if(dragState.module != null){
            val reverseDropTarget = getCurrentReverseDopTarget()
            if(reverseDropTarget == null){
                controller.reselect(dragState.module!!)
                controller.moduleToWindow(dragState.module!!)
            }else {
                val dropTarget = getCurrentDopTarget(reverseDropTarget.tabRowKey.windowState)
                if(dropTarget != null && isValidDropTarget(dropTarget.tabRowKey)){
                    controller.reselect(dragState.module!!)
                    controller.dropModule(dropTarget, dragState.module!!)
                }
            }
        }
        reset()
    }

    fun isValidDropTarget(tabRowKey: TabRowKey): Boolean {
        if (getModuleType() == null) return false
        return (ModuleType.BOTH == getModuleType() || ModuleType.BOTH == tabRowKey.moduleType || getModuleType() == tabRowKey.moduleType) && !controller.getModulesFiltered(tabRowKey).contains(dragState.module!!)
    }

}