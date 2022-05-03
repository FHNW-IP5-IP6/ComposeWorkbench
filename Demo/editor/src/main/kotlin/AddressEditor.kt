
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        AddressEditorUi( AddressEditorModel(Address(-1,"","","",0, "","")))
    }
}

@Composable
fun AddressEditorUi(model: AddressEditorModel){
    with(model) {
        MaterialTheme {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column {
                    Row {
                        EditableField(label = "Name",
                            onValueChange = { firstName = it },
                            getValue = { firstName })
                        EditableField(label = "Last Name",
                            onValueChange = { lastName = it },
                            getValue = { lastName })
                    }
                    Row {
                        EditableField(label = "Street",
                            onValueChange = { street = it },
                            getValue = { street })
                        EditableField(label = "Street Nr.",
                            onValueChange = { streetNr = if (it.toIntOrNull() == null)  0 else it.toInt()},
                            getValue = { streetNr.toString() })
                    }
                    Row {
                        EditableField(label = "City",
                            onValueChange = { city  = it },
                            getValue = { city })
                        EditableField(label = "Country",
                            onValueChange = { country = it },
                            getValue = { country })
                    }
                }
            }
        }
    }
}

/**
 * Editor Specific Model.
 */
class AddressEditorModel(address: Address) {
    val id = address.id
    var firstName by mutableStateOf(address.firstName)
    var lastName by mutableStateOf(address.lastName)
    var street by mutableStateOf(address.street)
    var streetNr by mutableStateOf(address.streetNr)
    var city by mutableStateOf(address.city)
    var country by mutableStateOf(address.country)
}

class Address(
    val id: Int,
    var firstName: String,
    var lastName: String,
    var street: String,
    var streetNr: Int?,
    var city: String,
    var country: String
) {
}
