package realestateexplorer.controller

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.vector.ImageVector
import allpurpose.controller.Action

sealed class ExplorerAction(
    override val name: String,
    override val icon: ImageVector? = null,
    override val enabled: Boolean,
    override val undoable: Boolean
) : Action {


    class New    : ExplorerAction("New",   Icons.Filled.Add, true, false)
    class Update(val id: Int, val field: String, val value: String) : ExplorerAction("Update",null, true, true)
}
