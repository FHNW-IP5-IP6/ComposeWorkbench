package realestateexplorer.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import realestateexplorer.data.ExplorerData

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExplorerUI(realEstates: List<ExplorerData>) {

    if (realEstates.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("noch nix in DB")
        }

    } else {
        Column(Modifier.fillMaxSize()) {
            LazyColumn {
                items(realEstates) {
                    ListItem(
                        text = { Text("${it.street} ${it.streetNumber}") },
                        overlineText = { Text("${it.zipCode}, ${it.city}") },
                        //  modifier = Modifier.clickable(onClick = { trigger(ApplicationAction.Open(it.id)) })
                    )
                    Divider()
                }
            }
        }
    }

}