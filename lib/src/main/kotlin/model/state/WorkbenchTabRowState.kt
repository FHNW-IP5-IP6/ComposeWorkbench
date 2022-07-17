package model.state

import model.data.TabRowKey

internal data class WorkbenchTabRowState(
    val tabRowKey: TabRowKey,
    val preview: String?,
    val selected: WorkbenchModuleState<*>?,
){

    override fun toString(): String = "$tabRowKey , preview: $preview, selected: ${selected?.id}"

}