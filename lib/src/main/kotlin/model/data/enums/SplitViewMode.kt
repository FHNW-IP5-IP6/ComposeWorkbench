package model.data.enums

enum class SplitViewMode {
    UNSPLIT, VERTICAL, HORIZONTAL
}

internal fun SplitViewMode.isUnsplit() = this == SplitViewMode.UNSPLIT