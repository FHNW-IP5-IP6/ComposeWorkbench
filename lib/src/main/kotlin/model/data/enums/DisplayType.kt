package model.data.enums

import ExplorerLocation

internal enum class DisplayType(val orientation: Float, val deselectable: Boolean) {
    TAB1(0F, false), TAB2(0F, false), WINDOW(0F, false), LEFT(-90F, true), BOTTOM(0F, true)
}

internal fun toDisplayType(location: ExplorerLocation) : DisplayType {
    return when(location) {
        ExplorerLocation.LEFT -> DisplayType.LEFT
        ExplorerLocation.BOTTOM -> DisplayType.BOTTOM
    }
}

