package model.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import model.data.enums.ModuleType
import util.WorkbenchDefaultIcon

internal class WorkbenchModule<M>(
    val moduleType: ModuleType,
    val modelType: String,
    val title: (M) -> String,
    val icon: ImageVector = WorkbenchDefaultIcon,
    val loader: ((Int) -> M)? = null,
    val onClose: (M) -> Unit = {},
    val onSave: (M) -> Unit = {},
    val content: @Composable (M) -> Unit,
    )
{
}