package model.state

import model.data.enums.PopUpType

internal data class PopUpState(
    val type: PopUpType,
    val action: () -> Unit
) {
}