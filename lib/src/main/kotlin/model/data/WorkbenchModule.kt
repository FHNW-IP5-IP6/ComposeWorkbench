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
    val loader: ((Int, MQClientImpl) -> C)? = null,
    val init: ((C, MQClientImpl) -> Unit)? = null,
    val onSave: (C, MQClientImpl) -> ActionResult = { _: C, _: MQClientImpl -> success()},
    val content: @Composable (C) -> Unit,
    )
{
}