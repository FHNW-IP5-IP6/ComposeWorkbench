package model.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import model.data.ModuleType

/**
 * objects which holds information about the currently dragged module and its position
 */
internal object DragState {
    var isDragging: Boolean by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var module: WorkbenchModuleState<*>? = null
    var isWindow by mutableStateOf(false)
    var positionOnScreen by mutableStateOf(IntOffset.Zero)

    fun getModuleType(): ModuleType? {
        return module?.module?.moduleType
    }

    fun reset(){
        isDragging = false
        dragPosition = Offset.Zero
        dragOffset = Offset.Zero
        module = null
        isWindow = false
        positionOnScreen = IntOffset.Zero
    }

    fun getWindowPosition(): WindowPosition {
        return WindowPosition(x = positionOnScreen.x.dp, y = positionOnScreen.y.dp)
    }
}