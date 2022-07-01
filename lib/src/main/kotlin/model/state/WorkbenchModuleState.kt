package model.state

import androidx.compose.runtime.Composable
import model.data.MQClient
import model.data.WorkbenchModule
import model.data.enums.DisplayType

internal class WorkbenchModuleState <M> (
    val id: Int,
    private val dataId: Int? = null,
    var model: M,
    var module: WorkbenchModule<M>,
    var window: WorkbenchWindowState,
    var close: (WorkbenchModuleState<*>) -> Unit = {},
    var displayType: DisplayType,
    var isPreview: Boolean = false
){
    private var client: MQClient = MQClient(id.toString())

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

internal class WorkbenchDefaultState <M> (
    val type: String,
    val model: M,
    val title: (M) -> String
){
    fun getTitle() : String {
        return title(model)
    }
}