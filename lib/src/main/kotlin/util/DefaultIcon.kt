package util

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

internal val WorkbenchDefaultIcon: ImageVector
    get() {
        if (_home != null) {
            return _home!!
        }
        _home = materialIcon(name = "WorkbenchDefaultIcon") {
            materialPath {
            }
        }
        return _home!!
    }

private var _home: ImageVector? = null
