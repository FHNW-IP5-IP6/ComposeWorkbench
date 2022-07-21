package realestateeditor.controller

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.ui.graphics.vector.ImageVector
import allpurpose.controller.Action

sealed class RealEstateAction(
    override val name: String,
    override val icon: ImageVector? = null,
    override val enabled: Boolean,
    override val undoable: Boolean
) : Action {

    class Close : RealEstateAction("Close", null, true, false)
    class New   : RealEstateAction("New", null, true, false)

    class Save(enabled: Boolean = true) : RealEstateAction("Save", Icons.Filled.Save, enabled, false)
    class Delete() : RealEstateAction("Delete", Icons.Filled.Delete, true, false)

    class UpdateStreet(val newValue: String)             : RealEstateAction("Update Street", null, true, true)
    class UpdateStreetNumber(val newValue: String)       : RealEstateAction("Update Street Number", null, true, true)
    class UpdateZipCode(val newValue: String)            : RealEstateAction("Update Zip-Code", null, true, true)
    class UpdateCity(val newValue: String)               : RealEstateAction("Update City", null, true, true)
    class UpdateYearOfConstruction(val newValue: String) : RealEstateAction("Update Year of Construction", null, true, true)
    class UpdateMarketValue(val newValue: String)        : RealEstateAction("Update Market Value", null, true, true)
    class UpdateDescription(val newValue: String)        : RealEstateAction("Update Description", null, true, true)

    class Undo(enabled: Boolean = true)                  : RealEstateAction("Undo", Icons.Filled.Undo, enabled, true)
    class Redo(enabled: Boolean = true)                  : RealEstateAction("Redo", Icons.Filled.Redo, enabled, true)


}
