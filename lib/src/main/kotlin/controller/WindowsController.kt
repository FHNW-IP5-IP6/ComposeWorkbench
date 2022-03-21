package controller

import model.WorkbenchModel
import model.state.WorkbenchWindowState

internal class WindowsController(val model: WorkbenchModel) {

    fun remove(window: WorkbenchWindowState){
        model.windows.remove(window)
    }
}