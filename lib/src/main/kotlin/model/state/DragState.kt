package model.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import model.data.ModuleType

/**
 * Class which holds information about the currently dragged module and its position
 */
internal class DragState {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var module: WorkbenchModuleState<*>? = null
    var isWindow by mutableStateOf(false)
    var positionOnScreen by mutableStateOf(IntOffset.Zero)

    fun getModuleType(): ModuleType? {
        return module?.module?.moduleType
    }
}