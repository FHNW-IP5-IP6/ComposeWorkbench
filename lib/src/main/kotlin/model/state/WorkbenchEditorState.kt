package model.state

import WorkbenchEditorType
import androidx.compose.runtime.Composable

internal class WorkbenchEditorState<T, M>(
    val title: String,
    val type: WorkbenchEditorType,
    val initModel: (T) -> M,
    val onClose: (M) -> Unit,
    val content: @Composable (M) -> Unit,
) {
}