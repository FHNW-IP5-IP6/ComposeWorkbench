package model.state

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import model.data.TabRowKey

/**
 * Immutable state which holds information about the currently dragged module and its position
 */
internal data class WorkbenchDragState(
    val isDragging: Boolean,
    val module: WorkbenchModuleState<*>?,
    val positionOnScreen: DpOffset,
    val dropTargets: List<DropTarget>
){
}

internal data class DropTarget(
    val isReverse: Boolean,
    val tabRowKey: TabRowKey,
    val bounds: Rect
) {
    override fun toString() = "$tabRowKey, isRevers $isReverse, bounds $bounds"
}