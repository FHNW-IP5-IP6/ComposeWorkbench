package model.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class WorkbenchPreviewState {
    var previewTitle: String? by mutableStateOf(null)

    fun hasPreview(): Boolean{
        return previewTitle != null
    }
}