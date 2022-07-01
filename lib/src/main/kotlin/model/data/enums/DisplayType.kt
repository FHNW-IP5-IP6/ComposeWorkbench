package model.data.enums

import ExplorerLocation

internal enum class DisplayType(val orientation: Float) {
    TAB1(0F), TAB2(0F), WINDOW(0F), LEFT(-90F), BOTTOM(0F)
}

internal fun toDisplayType(location: ExplorerLocation) : DisplayType {
    return when(location) {
        ExplorerLocation.LEFT -> DisplayType.LEFT
        ExplorerLocation.BOTTOM -> DisplayType.BOTTOM
    }
}

