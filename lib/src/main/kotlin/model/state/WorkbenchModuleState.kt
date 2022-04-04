package model.state

import androidx.compose.runtime.Composable
import model.data.WorkbenchModule

internal class WorkbenchModuleState <M> (
    val title: String,
    val model: M,
    val module: WorkbenchModule<M>,
    val close: (WorkbenchModuleState<*>) -> Unit = {},
    val displayType: DisplayType,
    val onClose: (M) -> Unit = {},
    val onSave: (M) -> Unit = {},
    )
{
    fun onClose() {
        onClose(model)
        close(this)
    }

    fun onSave() = onSave(model)

    @Composable
    fun content() = module.content(model)
}