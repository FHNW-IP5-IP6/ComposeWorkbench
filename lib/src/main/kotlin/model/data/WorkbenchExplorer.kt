package model.data

import androidx.compose.runtime.Composable
import model.ContentHolder

internal class WorkbenchExplorer(
    val title: String,
    val content: @Composable () -> Unit,
): ContentHolder {

    @Composable
    override fun content() {
        content.invoke()
    }
}