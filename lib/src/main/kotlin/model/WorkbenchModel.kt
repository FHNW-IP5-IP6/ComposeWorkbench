package model

import WorkbenchEditorType
import androidx.compose.runtime.*
import model.data.WorkbenchEditor
import model.data.WorkbenchExplorer
import model.state.WorkbenchWindowState

internal class WorkbenchModel {

    val windows by mutableStateOf<MutableList<WorkbenchWindowState>>(mutableStateListOf())

    val explorers by mutableStateOf<MutableList<WorkbenchExplorer>>(mutableStateListOf())
    val editors by mutableStateOf<MutableMap<WorkbenchEditorType, WorkbenchEditor<out Any, out Any>>>(
        mutableStateMapOf()
    )

    var selectedExplorer: WorkbenchExplorer? by mutableStateOf(null)
}