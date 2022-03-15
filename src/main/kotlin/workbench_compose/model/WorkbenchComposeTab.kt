package workbench_compose.model

import androidx.compose.runtime.Composable

class WorkbenchComposeTab(val writer: String, var content: @Composable () -> Unit)  {
    public var onClose: () -> Unit = {}
}