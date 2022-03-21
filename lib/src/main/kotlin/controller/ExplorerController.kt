package controller

import model.WorkbenchModel
import model.state.WorkbenchExplorerState

internal class ExplorerController(val model: WorkbenchModel) {

    fun getIndex(explorer: WorkbenchExplorerState?): Int {
        val index = model.explorers.indexOf(explorer)
        return index.coerceAtLeast(0)
    }
}