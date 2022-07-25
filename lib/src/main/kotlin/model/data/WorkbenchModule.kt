package model.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import model.data.enums.ModuleType
import util.WorkbenchDefaultIcon

internal class WorkbenchModule<C>(
    val moduleType: ModuleType,
    val modelType: String,
    val title: (C) -> String,
    val icon: ImageVector = WorkbenchDefaultIcon,
    val loader: ((Int, MQClient) -> C)? = null,
    val init: ((C, MQClient) -> Unit)? = null,
    val onClose: (C, MQClient) -> Unit = { _: C, _: MQClient -> },
    val onSave: (C, MQClient) -> Boolean = { _: C, _: MQClient -> true},
    val content: @Composable (C) -> Unit,
    )
{
}