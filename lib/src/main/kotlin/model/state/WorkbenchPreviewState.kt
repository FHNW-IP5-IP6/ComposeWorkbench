package model.state

import model.data.TabRowKey

internal data class WorkbenchPreviewState(
    val tabRowKey: TabRowKey?,
    val title: String
){
}