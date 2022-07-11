package controller

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.WindowPosition
import model.data.enums.ModuleType
import model.state.*
import util.toOffset

internal class WorkbenchDragController {

    private val dragState = WorkbenchDragState()

    fun getModuleType(): ModuleType? {
        return dragState.module?.module?.moduleType
    }

    fun getModuleState(): WorkbenchModuleState<*>? {
        return dragState.module
    }

    fun isDragging(): Boolean {
        return dragState.isDragging
    }

    fun getPosition(): DpOffset{
        return dragState.positionOnScreen
    }

    fun addReverseDropTarget(windowState: WorkbenchWindowState, bounds: Rect){
        dragState.reverseDropTargets.removeIf { it.windowState == windowState }
        dragState.reverseDropTargets.add(ReverseDropTarget(windowState, bounds))
    }

    fun addDropTarget(displayController: WorkbenchDisplayController, bounds: Rect){
        dragState.dropTargets.removeIf { it.displayController == displayController }
        dragState.dropTargets.add(DropTarget(displayController, bounds))
    }

    fun removeReverseDropTarget(windowState: WorkbenchWindowState){
        dragState.dropTargets.removeIf { it.displayController.windowState == windowState }
        dragState.reverseDropTargets.removeIf { it.windowState == windowState }
    }

    fun removeDropTarget(displayController: WorkbenchDisplayController){
        dragState.dropTargets.removeIf { it.displayController == displayController }
    }

    fun isCurrentDropTarget(displayController: WorkbenchDisplayController): Boolean {
        val reverseDropTarget = getCurrentReverseDopTarget() ?: return false //If there is no reverse target active the drop target is outside any window
        val dropTarget = getCurrentDopTarget(reverseDropTarget.windowState)
        return  dropTarget != null && dropTarget.displayController == displayController && isValidDropTarget(dropTarget)
    }

    fun isValidDropTarget(dropTarget: DropTarget): Boolean {
        return dragState.module != null && dropTarget.displayController.acceptsModuleOfType(getModuleType()!!) && !dropTarget.displayController.containsModule(dragState.module!!)
    }

    fun getCurrentReverseDopTarget(): ReverseDropTarget? {
        val targets = dragState.reverseDropTargets.filter { it.bounds.contains(dragState.positionOnScreen.toOffset()) }
        return when (targets.isEmpty()){
            true -> null
            false -> {
                //Prioritize the window witch is currently focused
                if (targets.find { it.windowState.hasFocus } == null) targets.first() else targets.find { it.windowState.hasFocus }!!
            }
        }
    }

    fun getCurrentDopTarget(windowState: WorkbenchWindowState): DropTarget? {
        return dragState.dropTargets.find { it.bounds.contains(dragState.positionOnScreen.toOffset()) && it.displayController.windowState == windowState }
    }

    fun reset(){
        dragState.isDragging = false
        dragState.module = null
        dragState.positionOnScreen = DpOffset.Zero
    }

    fun setDragging(isDragging: Boolean) {
        dragState.isDragging = isDragging
    }

    fun setModuleState(moduleState: WorkbenchModuleState<*>? ){
        dragState.module = moduleState
    }

    fun setPosition(positionOnScreen: DpOffset) {
        dragState.positionOnScreen = positionOnScreen
    }

    fun toOffset(windowState: WorkbenchWindowState): Offset {
        val x = dragState.positionOnScreen.x - windowState.windowState.position.x
        val y = dragState.positionOnScreen.y - windowState.windowState.position.y
        return Offset(x.value,y.value)
    }

    fun getWindowPosition(): WindowPosition {
        return WindowPosition(x = dragState.positionOnScreen.x, y = dragState.positionOnScreen.y)
    }
}