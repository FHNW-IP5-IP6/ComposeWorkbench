package controller

import model.WorkbenchModel
import model.state.DisplayType
import model.state.WorkbenchModuleState

internal class WorkbenchController(val model: WorkbenchModel) {

    fun <M> convertToWindow(module: WorkbenchModuleState<M>) {
        val window = WorkbenchModuleState(
            module.title,
            module.model,
            module.module,
            this::removeModuleState,
            DisplayType.WINDOW,
            module.onClose
        )
        model.modules -= module
        model.modules += window
    }

    fun removeModuleState(module: WorkbenchModuleState<*>){
        model.modules -= module
    }
}