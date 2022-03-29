package controller

import model.WorkbenchModel
import model.data.WorkbenchExplorer

internal class ExplorerController(val model: WorkbenchModel) {

    fun getIndex(explorer: WorkbenchExplorer?): Int {
        val index = model.explorers.indexOf(explorer)
        return index.coerceAtLeast(0)
    }
}