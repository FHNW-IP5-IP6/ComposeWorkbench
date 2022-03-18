package model

import WorkbenchEditorType
import androidx.compose.runtime.*
import model.state.WorkbenchEditorState
import model.state.WorkbenchExplorerState
import model.state.WorkbenchWindowState

internal object WorkbenchModel {

    val windows by mutableStateOf<MutableList<WorkbenchWindowState>>(mutableStateListOf())

    val explorers by mutableStateOf<MutableList<WorkbenchExplorerState>>(mutableStateListOf())
    val editors by mutableStateOf<MutableMap<WorkbenchEditorType, WorkbenchEditorState<out Any, out Any>>>(
        mutableStateMapOf()
    )

    var selectedExplorer: WorkbenchExplorerState? by mutableStateOf(null)

    fun getIndex(explorer: WorkbenchExplorerState?): Int {
        val index = explorers.indexOf(explorer)
        return index.coerceAtLeast(0)
    }
}