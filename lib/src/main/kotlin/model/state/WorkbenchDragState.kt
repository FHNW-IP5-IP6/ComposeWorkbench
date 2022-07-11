package model.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import controller.WorkbenchDisplayController

/**
 * objects which holds information about the currently dragged module and its position
 */
internal class WorkbenchDragState {
    var isDragging: Boolean by mutableStateOf(false)
    var module: WorkbenchModuleState<*>? = null
    var positionOnScreen by mutableStateOf(DpOffset.Zero)
    var reverseDropTargets = mutableStateListOf<ReverseDropTarget>()
    var dropTargets = mutableStateListOf<DropTarget>()
}

internal class DropTarget(
    val displayController: WorkbenchDisplayController,
    val bounds: Rect
) {
}

internal class ReverseDropTarget(
    val windowState: WorkbenchWindowState,
    val bounds: Rect
) {
}