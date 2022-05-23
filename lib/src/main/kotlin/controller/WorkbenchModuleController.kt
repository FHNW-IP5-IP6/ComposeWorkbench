package controller

import androidx.compose.runtime.MutableState
import model.WorkbenchModel
import model.data.ModuleType
import model.state.DisplayType
import model.state.WorkbenchModuleState

internal class WorkbenchModuleController(val model: WorkbenchModel, val displayType: DisplayType, val moduleType: ModuleType, val deselectable: Boolean = false) {

    fun getSelectedModule(): MutableState<WorkbenchModuleState<*>?> {
        return model.getSelectedModule(displayType, moduleType)
    }

    fun getModulesFiltered(): List<WorkbenchModuleState<*>> {
        return model.modules.filter { it.displayType== displayType && it.module.moduleType== moduleType }
    }

    fun getIndex(explorer: WorkbenchModuleState<*>?): Int {
        val index = getModulesFiltered().indexOf(explorer)
        return index.coerceAtLeast(0)
    }

    fun moduleSelectorPressed(module: WorkbenchModuleState<*>?) {
        if(deselectable && getSelectedModule().value == module){
            model.setSelectedModuleNull(displayType, moduleType)
        }else{
            model.setSelectedModule(module!!)
        }
    }

    fun isModuleSelected(module: WorkbenchModuleState<*>?): Boolean {
        return getSelectedModule().value != null && getSelectedModule().value == module
    }

    fun removeModuleState(module: WorkbenchModuleState<*>){
        model.modules -= module
    }

    fun <M> convertToWindow(module: WorkbenchModuleState<M>) {
        val window = WorkbenchModuleState(module, this::removeModuleState, DisplayType.WINDOW)
        model.removeTab(module)
        model.addState(window)
    }

    internal fun updateDisplayType(module: WorkbenchModuleState<*>, displayType: DisplayType){
        if(displayType != module.displayType){
            model.removeTab(module)
            module.displayType = displayType
            model.addState(module)
        }else if(!isModuleSelected(module)) {
            moduleSelectorPressed(module)
        }
    }
}