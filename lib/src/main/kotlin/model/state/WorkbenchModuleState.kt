package model.state

import MQ_INTERNAL_TOPIC_PATH_EDITOR
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
        client.publish(MQ_INTERNAL_TOPIC_PATH_EDITOR, "created")
    }

    fun updateModule(module: WorkbenchModule<*>){
        module as WorkbenchModule<M>
        //this.module.onClose.invoke(this.model)
        this.module = module
        model = module.loader!!.invoke(dataId!!)
    }

    fun onClose() {
        module.onClose(model, client)
        close(this)
        client.publish(MQ_INTERNAL_TOPIC_PATH_EDITOR, "closed")
    }

    fun getTitle() : String {
        return module.title(model)
    }

    fun onSave() = module.onSave(model, client)

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