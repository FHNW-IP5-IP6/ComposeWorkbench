package model.state

import ActionResult
import androidx.compose.runtime.Composable
import model.data.MQClientImpl
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
    var isPreview: Boolean = false
){

    init {
        MQClientImpl.publishCreated(module.modelType, dataId ?: id)
    }

    fun updateModule(module: WorkbenchModule<*>){
        module as WorkbenchModule<C>
        this.module = module
        controller = module.loader!!.invoke(dataId!!, MQClientImpl)
    }

    fun selected() {
        MQClientImpl.publishSelected(module.modelType, dataId ?: id)
    }

    fun getTitle() : String {
        return module.title(controller)
    }

    fun onSave(): ActionResult = module.onSave(controller, MQClientImpl)

    fun onClose(): ActionResult {
        val result =  module.onClose(controller, MQClientImpl)
        if(result.successful) {
            close(this)
        }
        return result
    }

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