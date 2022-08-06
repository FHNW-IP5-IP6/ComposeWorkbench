package model.state

import model.data.TabRowKey

internal data class WorkbenchTabRowState(
    val tabRowKey: TabRowKey,
    val selected: WorkbenchModuleState<*>?,
){

    override fun toString(): String = "$tabRowKey, selected: ${selected?.id}"

}