package model.data

import androidx.compose.runtime.Composable

internal class WorkbenchModule<M>(
    val moduleType: ModuleType,
    val modelType: String,
    val content: @Composable (M) -> Unit,
    )
{
}