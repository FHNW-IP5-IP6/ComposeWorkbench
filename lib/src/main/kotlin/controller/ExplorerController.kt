package controller

import model.WorkbenchModel
import model.data.WorkbenchExplorer

internal class ExplorerController(val model: WorkbenchModel) {

    fun getIndex(explorer: WorkbenchExplorer?): Int {
        val index = model.explorers.indexOf(explorer)
        return index.coerceAtLeast(0)
    }

    fun explorerSelectorPressed(explorer: WorkbenchExplorer) {
        if(model.selectedExplorer == explorer){
            model.selectedExplorer = null
        }else{
            model.selectedExplorer = explorer
        }
    }

    fun isExplorerSelected(explorer: WorkbenchExplorer): Boolean {
        return model.selectedExplorer != null && model.selectedExplorer == explorer
    }
}