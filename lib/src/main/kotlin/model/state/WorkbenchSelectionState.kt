package model.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class WorkbenchSelectionState {
    var selectedModuleState: WorkbenchModuleState<*>? by mutableStateOf(null)
}