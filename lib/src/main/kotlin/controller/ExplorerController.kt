package controller

import model.WorkbenchModel
import model.data.ModuleType
import model.state.DisplayType
import model.state.WorkbenchModuleState

internal class ExplorerController(val model: WorkbenchModel) {

    fun getIndex(explorer: WorkbenchModuleState<*>?): Int {
        val index = model.modules.filter { it.displayType==DisplayType.RIGHT && it.module.moduleType==ModuleType.EXPLORER }.indexOf(explorer)
        return index.coerceAtLeast(0)
    }

    fun explorerSelectorPressed(explorer: WorkbenchModuleState<*>?) {
        if(model.selectedExplorer == explorer){
            model.selectedExplorer = null
        }else{
            model.selectedExplorer = explorer
        }
    }

    fun isExplorerSelected(explorer: WorkbenchModuleState<*>?): Boolean {
        return model.selectedExplorer != null && model.selectedExplorer == explorer
    }
}