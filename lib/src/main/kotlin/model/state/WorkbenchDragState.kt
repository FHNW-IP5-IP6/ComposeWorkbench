package model.state

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import model.data.TabRowKey
import util.toOffset

/**
 * Immutable state which holds information about the currently dragged module and its position
 */
internal data class WorkbenchDragState(
    val isDragging: Boolean,
    val module: WorkbenchModuleState<*>?,
    val dropTargets: List<DropTarget>,
    val dragWindowState: WindowState,
){
    internal fun getCurrentDropTarget(): DropTarget? {
        val reverseDropTarget = getCurrentReverseDopTarget() ?: return null //If there is no reverse target active the drop target is outside any window
        return getCurrentDopTarget(reverseDropTarget.tabRowKey.windowState)
    }

    internal fun getCurrentReverseDopTarget(): DropTarget? {
        if(getPositionOnScreen().isUnspecified) return null
        val targets = dropTargets.filter { it.isReverse && it.bounds.contains(
            getPositionOnScreen().toOffset()) }
        return when (targets.isEmpty()){
            true -> null
            false -> {
                //Prioritize the window witch is currently focused
                if (targets.find { it.tabRowKey.windowState.hasFocus } == null) targets.first() else targets.find { it.tabRowKey.windowState.hasFocus }!!
            }
        }
    }

    internal fun toOffset(windowState: WorkbenchWindowState): Offset {
        val x = getPositionOnScreen().x - windowState.windowState.position.x
        val y = getPositionOnScreen().y - windowState.windowState.position.y
        return Offset(x.value,y.value)
    }

    internal fun getWindowPosition(): WindowPosition {
        if(getPositionOnScreen().isUnspecified) return WindowPosition.PlatformDefault
        return WindowPosition(dragWindowState.position.x, dragWindowState.position.y)
    }

    internal fun getCurrentDopTarget(windowState: WorkbenchWindowState): DropTarget? {
        if(getPositionOnScreen().isUnspecified) return null
        return dropTargets.find { !it.isReverse && it.bounds.contains(
            getPositionOnScreen().toOffset()) && it.tabRowKey.windowState == windowState }
    }

    internal fun getPositionOnScreen(): DpOffset {
        if(dragWindowState.position.isSpecified) {
            return DpOffset(dragWindowState.position.x, dragWindowState.position.y)
        }
        return DpOffset.Unspecified
    }
}

internal data class DropTarget(
    val isReverse: Boolean,
    val tabRowKey: TabRowKey,
    val bounds: Rect
) {
    override fun toString() = "$tabRowKey, isRevers $isReverse, bounds $bounds"
}

internal fun getDefaultWorkbenchDragState(): WorkbenchDragState {
    return WorkbenchDragState(
        isDragging = false,
        module = null,
        dropTargets = listOf(),
        dragWindowState =  WindowState(size = DpSize(350.dp, 300.dp), position = WindowPosition.PlatformDefault)
    )
}
