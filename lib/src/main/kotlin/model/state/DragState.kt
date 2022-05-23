package model.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import model.data.ModuleType

/**
 * Class which holds information about the currently dragged module and its position
 */
internal class DragState {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var module: WorkbenchModuleState<*>? = null

    fun getModuleType(): ModuleType? {
        return module?.module?.moduleType
    }
}