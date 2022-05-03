package model.data

import androidx.compose.runtime.Composable

internal class WorkbenchModule<M>(
    val moduleType: ModuleType,
    val modelType: String,
    val loader: ((Int) -> M)? = null,
    val content: @Composable (M) -> Unit,
    )
{
}