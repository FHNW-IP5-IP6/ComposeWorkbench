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
import androidx.compose.runtime.*
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
                                onClick = {
                                    onClick(it)
                                }
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

    val addressList = mutableStateListOf<Address>()

    fun updateAddress(address: Address) {
        addressList[address.id] = address
    }

    init {
        addressList.addAll(listOf(
            Address(0, "Homer", "Simpson", "Evergreen Terrace", 742, "Springfiels", "USA"),
            Address(1, "Marge", "Simpson", "Evergreen Terrace", 742, "Springfiels", "USA"),
            Address(2, "Bart", "Simpson", "Evergreen Terrace", 742, "Springfiels", "USA"),
            Address(3, "Lisa", "Simpson", "Evergreen Terrace", 742, "Springfiels", "USA"),
            Address(4, "Maggie", "Simpson", "Evergreen Terrace", 742, "Springfiels", "USA"),
            Address(5, "Harry", "Potter", "Privet Drive", 4, "Little Whinging", "GB"),
            Address(6, "Albus", "Dumbledore", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address(7, "Minerva", "McGonagall", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address(8, "Hagrid", "Rubeus", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address(9, "Severus", "Snape", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address(10, "Sybill ", "Trelawney", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address(11, "Neville", "Longbottom", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address(12, "Hermione", "Granger", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address(13, "Sirius", "Black", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address(14, "Remus", "Lupin", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address(15, "Cedric", "Diggory", "Hogwarts Castle", 1, "Higlands", "Scotland"),
            Address(16, "Ron", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address(17, "Arthur", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address(18, "Molly", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address(19, "Bill", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address(20, "Charlie", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address(21, "Percy", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address(22, "Fred", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address(23,"George", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
            Address(24, "Ginny", "Weasley", "The Burrow", 1, "Ottery St. Catchpole", "Scotland"),
        ))
    }
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
