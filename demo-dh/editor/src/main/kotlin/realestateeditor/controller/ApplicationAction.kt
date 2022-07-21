package realestateeditor.controller

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.ui.graphics.vector.ImageVector
import allpurpose.controller.Action

sealed class ApplicationAction(
    override val name: String,
    override val icon: ImageVector? = null,
    override val enabled: Boolean,
    override val undoable: Boolean) : Action {

    class Open(val id: Int)  : ApplicationAction("Open",  Icons.Filled.OpenInNew,        true, false)
    class Close(val id: Int) : ApplicationAction("Close", Icons.Filled.Close,            true, false)
    class New                : ApplicationAction("New",   Icons.Filled.AddCircleOutline, true, false)
    class OpenDialog         : ApplicationAction("Open ...",   null, true, false)
    class CloseDialog        : ApplicationAction("Close Dialog",   null, true, false)
    class Exit               : ApplicationAction("Quit", null, true, false)

}
