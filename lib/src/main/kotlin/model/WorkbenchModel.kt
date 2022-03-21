package model

import WorkbenchEditorType
import androidx.compose.runtime.*
import model.state.WorkbenchEditorState
import model.state.WorkbenchExplorerState
import model.state.WorkbenchWindowState

internal class WorkbenchModel {

    val windows by mutableStateOf<MutableList<WorkbenchWindowState>>(mutableStateListOf())

    val explorers by mutableStateOf<MutableList<WorkbenchExplorerState>>(mutableStateListOf())
    val editors by mutableStateOf<MutableMap<WorkbenchEditorType, WorkbenchEditorState<out Any, out Any>>>(
        mutableStateMapOf()
    )

    var selectedExplorer: WorkbenchExplorerState? by mutableStateOf(null)
}