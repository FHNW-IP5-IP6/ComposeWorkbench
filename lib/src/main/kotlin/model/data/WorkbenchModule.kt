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
    val loader: ((Int, MQClient) -> M)? = null,
    val onClose: (M, MQClient) -> Unit = { _: M, _: MQClient -> },
    val onSave: (M, MQClient) -> Boolean = { _: M, _: MQClient -> true},
    val content: @Composable (M, MQClient) -> Unit,
    )
{
}