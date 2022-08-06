package model.state

import model.data.WorkbenchModule
import model.data.enums.PopUpType

internal data class PopUpState(
    val type: PopUpType,
    val moduleState: WorkbenchModuleState<*>,
    val module: WorkbenchModule<*>,
    val message: String
)