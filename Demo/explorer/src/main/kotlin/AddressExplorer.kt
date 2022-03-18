// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        AddressExplorerUi(AddressExplorerModel()){}
    }
}

@Composable
fun AddressExplorerUi(model: AddressExplorerModel, onClick: (Address) -> Unit) {
    with(model) {
        MaterialTheme {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                items(addressList) {
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(1.dp)
                            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(4.dp))
                            .clickable(
                                onClick = { onClick(it) }
                            )
                            .fillMaxWidth()
                    ) {
                        NameTag(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun NameTag(address: Address) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column {
            Row {
                Text(text = "First Name: ${address.firstName}, Last Name: ${address.lastName}")
            }
        }
    }
}

/**
 * Explorer Specific Model.
 */
class AddressExplorerModel {
    val addressList by mutableStateOf<MutableList<Address>>(mutableStateListOf())

    init {
        addressList.addAll(listOf(
            Address("Homer", "Simpson", "Evergreen Terrace", 742, "Springfiels", "USA"),
            Address("Marge", "Simpson", "Evergreen Terrace", 742, "Springfiels", "USA"),
            Address("Bart", "Simpson", "Evergreen Terrace", 742, "Springfiels", "USA"),
            Address("Lisa", "Simpson", "Evergreen Terrace", 742, "Springfiels", "USA"),
            Address("Maggie", "Simpson", "Evergreen Terrace", 742, "Springfiels", "USA"),
            Address("Harry", "Potter", "Privet Drive", 4, "Little Whinging", "GB"),
            Address("Albus", "Dumbledore", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address("Minerva", "McGonagall", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address("Hagrid", "Rubeus", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address("Severus", "Snape", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address("Sybill ", "Trelawney", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address("Neville", "Longbottom", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address("Hermione", "Granger", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address("Sirius", "Black", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address("Remus", "Lupin", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address("Cedric", "Diggory", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address("Ron", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address("Arthur", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address("Molly", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address("Bill", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address("Charlie", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address("Percy", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address("Fred", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address("George", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address("Ginny", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
        ))
    }
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
