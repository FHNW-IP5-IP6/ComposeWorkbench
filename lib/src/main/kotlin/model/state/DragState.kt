package model.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.WindowPosition
import model.data.ModuleType

/**
 * objects which holds information about the currently dragged module and its position
 */
internal class DragState(mainWindow: WindowStateAware) {
    var isDragging: Boolean by mutableStateOf(false)
    var module: WorkbenchModuleState<*>? = null
    var positionOnScreen by mutableStateOf(DpOffset.Zero)
    var parentWindow: WindowStateAware by mutableStateOf(mainWindow)
    var onModuleDropped: (WorkbenchModuleState<*>) -> Unit = {}

    fun getModuleType(): ModuleType? {
        return module?.module?.moduleType
    }

    fun reset(){
        isDragging = false
        module = null
        positionOnScreen = DpOffset.Zero
        onModuleDropped = {}
    }

    fun toOffset(): Offset{
        val x = positionOnScreen.x - parentWindow.windowState.position.x
        val y = positionOnScreen.y - parentWindow.windowState.position.y
        return Offset(x.value,y.value)
    }

    fun getWindowPosition(): WindowPosition {
        return WindowPosition(x = positionOnScreen.x, y = positionOnScreen.y)
    }
}