package model.state

import androidx.compose.runtime.Composable
import model.data.MQClient
import model.data.WorkbenchModule
import model.data.enums.DisplayType

internal class WorkbenchModuleState <M> (
    val id: Int,
    val dataId: Int? = null,
    var model: M,
    var module: WorkbenchModule<M>,
    var window: WorkbenchWindowState,
    var close: (WorkbenchModuleState<*>) -> Unit = {},
    var displayType: DisplayType,
    var isPreview: Boolean = false
){
    private var client: MQClient = MQClient(module.modelType, dataId ?: id)

    init {
        client.publishCreated()
    }

    fun updateModule(module: WorkbenchModule<*>){
        module as WorkbenchModule<M>
        this.module = module
        model = module.loader!!.invoke(dataId!!)
    }

    fun onClose() {
        close(this)
        module.onClose(model, client)
        client.publishClosed()
    }

    fun getTitle() : String {
        return module.title(model)
    }

    fun onSave(): Boolean = module.onSave(model, client)

    @Composable
    fun content() = module.content(model, client)
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