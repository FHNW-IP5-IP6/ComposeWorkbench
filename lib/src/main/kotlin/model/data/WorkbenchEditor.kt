package model.data

import WorkbenchEditorType
import androidx.compose.runtime.Composable

internal class WorkbenchEditor<T, M>(
    val title: String,
    val type: WorkbenchEditorType,
    val initModel: (T) -> M,
    val onClose: (M) -> Unit,
    val content: @Composable (M) -> Unit,
) {
}