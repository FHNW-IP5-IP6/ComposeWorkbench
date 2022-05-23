package model.state

import androidx.compose.runtime.Composable
import model.data.WorkbenchModule

internal class WorkbenchModuleState <M> (
    val id: Int,
    val title: (M) -> String,
    val model: M,
    val module: WorkbenchModule<M>,
    val close: (WorkbenchModuleState<*>) -> Unit = {},
    var displayType: DisplayType,
    val onClose: (M) -> Unit = {},
    val onSave: (M) -> Unit = {},
    )
{
    constructor(state: WorkbenchModuleState<M>, close: (WorkbenchModuleState<*>) -> Unit = {}, displayType: DisplayType) : this(
        state.id,
        state.title,
        state.model,
        state.module,
        close,
        displayType,
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