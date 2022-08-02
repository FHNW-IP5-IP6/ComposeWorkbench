package controller

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import model.data.TabRowKey
import model.state.WorkbenchModuleState

internal sealed class DragAndDropAction (
    val name: String): Action {

    class Reset                                                               : DragAndDropAction("Reset")
    class SetDragging(val isDragging: Boolean)                                : DragAndDropAction("Set dragging")
    class SetModuleState(val moduleState: WorkbenchModuleState<*>?)           : DragAndDropAction("Set module state")
    class SetPosition(val positionOnScreen: DpOffset)                         : DragAndDropAction("Set position")
    class AddReverseDropTarget(val tabRowKey: TabRowKey,val bounds: Rect)     : DragAndDropAction("Add reverse drop target")
    class AddDropTarget(val tabRowKey: TabRowKey,val bounds: Rect)            : DragAndDropAction("Add drop target")
    class RemoveReverseDropTarget(val tabRowKey: TabRowKey)                   : DragAndDropAction("Remove reverse drop target")
}