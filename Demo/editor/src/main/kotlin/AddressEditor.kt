// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        AddressEditorUi( AddressEditorModel(Address("","","",0, "","")))
    }
}

@Composable
fun AddressEditorUi(model: AddressEditorModel){
    with(model) {
        MaterialTheme {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column {
                    Row {
                        AddressValueEditField(label = "Name",
                            onValueChange = { firstName = it },
                            getValue = { firstName })
                        AddressValueEditField(label = "Last Name",
                            onValueChange = { lastName = it },
                            getValue = { lastName })
                    }
                    Row {
                        AddressValueEditField(label = "Street",
                            onValueChange = { street = it },
                            getValue = { street })
                        AddressValueEditField(label = "Street Nr.",
                            onValueChange = { streetNr = if (it.toIntOrNull() == null)  0 else it.toInt()},
                            getValue = { streetNr.toString() })
                    }
                    Row {
                        AddressValueEditField(label = "City",
                            onValueChange = { city  = it },
                            getValue = { city })
                        AddressValueEditField(label = "Country",
                            onValueChange = { country = it },
                            getValue = { country })
                    }
                }
            }
        }
    }
}

@Composable
internal fun AddressValueEditField(label:String ,getValue: () -> String, onValueChange: (String) -> Unit){
    TextField(
        modifier = Modifier.padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
        label = { Text(text = label) },
        value = getValue.invoke(),
        onValueChange =  onValueChange
    )
}

/**
 * Editor Specific Model.
 */
class AddressEditorModel(address: Address) {
    var firstName by mutableStateOf(address.firstName)
    var lastName by mutableStateOf(address.lastName)
    var street by mutableStateOf(address.street)
    var streetNr by mutableStateOf(address.streetNr)
    var city by mutableStateOf(address.city)
    var country by mutableStateOf(address.country)
}

class Address(
    var firstName: String,
    var lastName: String,
    var street: String,
    var streetNr: Int?,
    var city: String,
    var country: String
) {
}