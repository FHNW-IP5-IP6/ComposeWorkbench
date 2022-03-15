package workbench_compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import workbench_compose.model.WorkbenchComposeModel
import workbench_compose.model.WorkbenchComposeTab

import workbench_compose.view.*

class WorkbenchCompose {
    val name: String = "foo"

    private var model: WorkbenchComposeModel = WorkbenchComposeModel()

    fun addTab(title:String, content: @Composable () -> Unit) : WorkbenchComposeTab {
        val tab = WorkbenchComposeTab(title, content)
        model.addTab(tab)
        return tab
    }

    fun closeTab(tab: WorkbenchComposeTab) {
        model.removeTab(tab)
    }

    fun run() = application {
        Window(onCloseRequest = ::exitApplication) {
            WorkbenchComposeUI(model)
        }
    }
}