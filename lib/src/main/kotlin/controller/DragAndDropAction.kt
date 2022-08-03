package controller

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import model.data.TabRowKey
import model.state.WorkbenchModuleState

internal sealed class DragAndDropAction (
    val name: String): Action {

    class Reset                                                                             : DragAndDropAction("Reset")
    class StartDragging(val moduleState: WorkbenchModuleState<*>)                           : DragAndDropAction("Set dragging")
    class SetPosition(val positionOnScreen: DpOffset)                                       : DragAndDropAction("Set position")
    class AddDropTarget(val tabRowKey: TabRowKey,val bounds: Rect, val isReverse: Boolean)  : DragAndDropAction("Add drop target")
}