package model.state

import androidx.compose.runtime.Composable
import model.data.MQClient
import model.data.WorkbenchModule
import model.data.enums.DisplayType

internal class WorkbenchModuleState <C> (
    val id: Int,
    val dataId: Int? = null,
    var controller: C,
    var module: WorkbenchModule<C>,
    var window: WorkbenchWindowState,
    var close: (WorkbenchModuleState<*>) -> Unit = {},
    var displayType: DisplayType,
    val client: MQClient = MQClient,
    var isPreview: Boolean = false
){


    init {
        client.publishCreated(module.modelType, dataId ?: id)
    }

    fun updateModule(module: WorkbenchModule<*>){
        module as WorkbenchModule<C>
        this.module = module
        controller = module.loader!!.invoke(dataId!!, client)
    }

    fun selected() {
        client.publishSelected(module.modelType, dataId ?: id)
    }

    fun onClose() {
        close(this)
        module.onClose(controller, client)
        client.publishClosed(module.modelType, dataId ?: id)
    }

    fun getTitle() : String {
        return module.title(controller)
    }

    fun onSave(): Boolean = module.onSave(controller, client)

    @Composable
    fun content() = module.content(controller)
}

internal class WorkbenchDefaultState <C> (
    val type: String,
    val controller: C,
    val title: (C) -> String
){
    fun getTitle() : String {
        return title(controller)
    }
}