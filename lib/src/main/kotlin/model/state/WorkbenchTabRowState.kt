package model.state

import model.data.TabRowKey

internal data class WorkbenchTabRowState(
    val tabRowKey: TabRowKey,
    val selected: WorkbenchModuleState<*>?,
    val popUpState: PopUpState?
){

    override fun toString(): String = "$tabRowKey, selected: ${selected?.id}"

}