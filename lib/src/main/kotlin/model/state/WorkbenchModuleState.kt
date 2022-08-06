package model.state

import ActionResult
import ExplorerLocation
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
    var displayType: DisplayType,
){

    init {
        MQClientImpl.publishCreated(module.modelType, dataId ?: id)
        module.init?.invoke(controller, MQClientImpl)
    }

    fun updateModule(module: WorkbenchModule<*>): WorkbenchModuleState<*>{
        module as WorkbenchModule<C>
        controller = module.loader!!.invoke(dataId!!, MQClientImpl)
        return WorkbenchModuleState(
            id = this.id,
            dataId = this.dataId,
            controller = controller,
            module = module,
            window = this.window,
            displayType = this.displayType,
        )
    }

    fun updateLocation(window: WorkbenchWindowState, displayType: DisplayType): WorkbenchModuleState<*>{
        return WorkbenchModuleState(
            id = this.id,
            dataId = this.dataId,
            controller = this.controller,
            module = this.module,
            window = window,
            displayType = displayType,
        )
    }

    fun selected() {
        MQClientImpl.publishSelected(module.modelType, dataId ?: id)
    }

    fun getTitle() : String {
        return module.title(controller)
    }

    fun onSave(): ActionResult = module.onSave(controller, MQClientImpl)

    fun onClose(): ActionResult {
        return  module.onClose(controller, MQClientImpl)
    }

    @Composable
    fun content() = module.content(controller)

}

internal class WorkbenchDefaultState <C> (
    val type: String,
    val controller: C,
    val title: (C) -> String,
    val location: ExplorerLocation,
    val shown: Boolean,
    val listed: Boolean,
){
    fun getTitle() : String {
        return title(controller)
    }
}