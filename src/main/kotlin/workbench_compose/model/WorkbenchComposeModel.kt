package workbench_compose.model

import androidx.compose.material.Text
import androidx.compose.runtime.*

class WorkbenchComposeModel {
    val version:String = "0.0"
    var tabIndex by mutableStateOf(0)

    var tabs by mutableStateOf<MutableList<WorkbenchComposeTab>>(
        mutableStateListOf(
            WorkbenchComposeTab("Module 1", {Text("Hello from Module 1")}),
            // WorkbenchComposeTab("Module 2", {Text("Hello from Module 2")}),
            // WorkbenchComposeTab("Module 3", {Text("Hello from Module 3")}),
        )
    )


    fun addTab(tab: WorkbenchComposeTab) {
        tabs.add(tab)
        tabIndex = tabs.size-1
    }

    fun removeTab(tab: WorkbenchComposeTab) {
        tab.onClose.invoke()
        tabs.remove(tab)
    }

}