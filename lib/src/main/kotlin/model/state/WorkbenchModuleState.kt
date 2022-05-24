package model.state

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import model.data.WorkbenchModule

internal class WorkbenchModuleState <M> (
    val id: Int,
    val title: (M) -> String,
    val model: M,
    val module: WorkbenchModule<M>,
    val close: (WorkbenchModuleState<*>) -> Unit = {},
    var displayType: DisplayType,
    val position: IntOffset? = null,
    val onClose: (M) -> Unit = {},
    val onSave: (M) -> Unit = {},
    )
{
    constructor(state: WorkbenchModuleState<M>, close: (WorkbenchModuleState<*>) -> Unit = {}, displayType: DisplayType, position: IntOffset? = null) : this(
        state.id,
        state.title,
        state.model,
        state.module,
        close,
        displayType,
        position,
        state.onClose,
        state.onSave
    )

    fun onClose() {
        onClose(model)
        close(this)
    }

    fun getTitle() : String {
        return title(model)
    }

    fun getWindowState(): WindowState {
        if(position == null) return WindowState()
        return WindowState(position = WindowPosition(position.x.dp, position.y.dp))
    }

    fun onSave() = onSave(model)

    @Composable
    fun content() = module.content(model)

}

internal class WorkbenchDefaultState <M> (
    val type: String,
    val model: M,
    val title: (M) -> String
){
    fun getTitle() : String {
        return title(model)
    }
}