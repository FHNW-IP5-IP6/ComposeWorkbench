package model

import androidx.compose.runtime.*
import model.data.WorkbenchModule
import model.state.DisplayType
import model.state.WorkbenchModuleState

internal class WorkbenchModel {

    var appTitle: String = "Compose Workbench";

    val modules = mutableStateListOf<WorkbenchModuleState<*>>()

    val registeredExplorers = mutableMapOf<String, WorkbenchModule<*>>()
    val registeredEditors = mutableMapOf<String, WorkbenchModule<*>>()

    var selectedExplorer: WorkbenchModuleState<*>? by mutableStateOf(null)
    var selectedTab: WorkbenchModuleState<*>? by mutableStateOf(null)


    fun removeTab(tab: WorkbenchModuleState<*>) {
        modules.remove(tab)
        val tabs = modules.filter { it.displayType==DisplayType.TAB}
        if (tabs.size > 0) {
            selectedTab = tabs.last()
        } else {
            selectedTab = null
        }
    }


    fun selectedTabIndex(): Int {
        if (selectedTab == null) return 0
        val index = modules.filter { it.displayType==DisplayType.TAB}.indexOf(selectedTab)
        return index.coerceAtLeast(0)
    }


    fun numberOfTabs(): Int {
        return modules.count { it.displayType == DisplayType.TAB }
    }
}