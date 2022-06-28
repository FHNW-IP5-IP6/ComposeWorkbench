package model.state

import MAIN_WINDOW_POS_OFFSET
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import model.data.DisplayType
import model.data.MQClient
import model.data.WorkbenchModule

internal class WorkbenchModuleState <M> (
    val id: Int,
    val dataId: Int? = null,
    var model: M,
    var module: WorkbenchModule<M>,
    var close: (WorkbenchModuleState<*>) -> Unit = {},
    var displayType: DisplayType,
    var isPreview: Boolean = false
){
    private var client: MQClient = MQClient(id.toString())

    /**
     * creates a preview module from the given state
     */
    constructor(
        id: Int,
        state: WorkbenchModuleState<M>,
        displayType: DisplayType)
            : this(id, state.dataId, state.model, state.module, {}, displayType, true) {}

    init {
        client.publish("Module created.")
    }

    fun updateModule(module: WorkbenchModule<*>){
        module as WorkbenchModule<M>
        this.module.onClose.invoke(this.model)
        this.module = module
        model = module.loader!!.invoke(dataId!!)
    }

    fun onClose() {
        module.onClose(model)
        close(this)
        client.publish("Module closed.")
    }

    fun getTitle() : String {
        return module.title(model)
    }

    fun onSave() = module.onSave(model)

    @Composable
    fun content() = module.content(model)
}

internal class WindowStateAware(
    var modules: List<WorkbenchModuleState<*>>,
    position: WindowPosition = WindowPosition(MAIN_WINDOW_POS_OFFSET, MAIN_WINDOW_POS_OFFSET)
) {
    val windowState by mutableStateOf(WindowState(position = position))
    var windowHeaderOffset = 0.dp
    var selectedModule:WorkbenchModuleState<*>? by mutableStateOf(null)
    var isDropTarget by mutableStateOf(false)

    init {
        selectedModule = if(modules.isNotEmpty()) modules[0] else null
    }
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

internal class PreviewState {
    var previewTitle: String? by mutableStateOf(null)

    fun hasPreview(): Boolean{
        return previewTitle != null
    }
}