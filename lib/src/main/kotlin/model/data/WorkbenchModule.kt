package model.data

import ActionResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import model.data.enums.ModuleType
import success
import util.WorkbenchDefaultIcon

internal class WorkbenchModule<C>(
    val moduleType: ModuleType,
    val modelType: String,
    val title: (C) -> String,
    val icon: ImageVector = WorkbenchDefaultIcon,
    val loader: ((Int, MQClient) -> C)? = null,
    val init: ((C, MQClient) -> Unit)? = null,
    val onClose: (C, MQClient) -> ActionResult = { _: C, _: MQClient -> success()},
    val onSave: (C, MQClient) -> ActionResult = { _: C, _: MQClient -> success()},
    val content: @Composable (C) -> Unit,
    )
{
}