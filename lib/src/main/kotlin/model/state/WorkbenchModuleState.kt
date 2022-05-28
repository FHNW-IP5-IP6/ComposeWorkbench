package model.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import model.data.DisplayType
import model.data.WorkbenchModule

internal open class WorkbenchModuleState <M> (
    val id: Int,
    val title: (M) -> String,
    val model: M,
    val module: WorkbenchModule<M>,
    var close: (WorkbenchModuleState<*>) -> Unit = {},
    var displayType: DisplayType,
    var isPreview: Boolean = false,
    val onClose: (M) -> Unit = {},
    val onSave: (M) -> Unit = {},
    )
{

    /**
     * creates a preview module from the given state
     */
    constructor(
        id: Int,
        state: WorkbenchModuleState<M>,
        displayType: DisplayType)
            : this(id, state.title, state.model, state.module, {}, displayType, true, {}, {})

    fun toWindow(): WorkbenchModuleState<M> {
        this.displayType = DisplayType.WINDOW
        return this
    }

    fun onClose() {
        onClose(model)
        close(this)
    }

    fun getTitle() : String {
        return title(model)
    }

    fun onSave() = onSave(model)

    @Composable
    fun content() = module.content(model)
}

internal class WindowStateAware(
    val moduleState: WorkbenchModuleState<*>,
    position: WindowPosition = WindowPosition.PlatformDefault) {

    val windowState by mutableStateOf(WindowState(position = position))

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