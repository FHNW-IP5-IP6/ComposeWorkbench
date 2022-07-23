package realestateexplorer.view

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import allpurpose.view.ActionIcon
import realestateexplorer.controller.ExplorerAction
import realestateexplorer.data.ExplorerData


@Composable
fun ExplorerUI(realEstates: List<ExplorerData>, trigger: (action: ExplorerAction) -> Unit, onClick: (id: Int) -> Unit) {
    MaterialTheme(colors = lightColors(primary = Color(0xFFEBE8DF),
          primaryVariant = Color(0xFF6A6A6A),
               secondary = Color(0xFFFBBA00),
        secondaryVariant = Color(0xFFA6572),
              background = Color(0xFFFFFFFC),
               onPrimary = Color(0xFF343434)),
                 content = { Scaffold(floatingActionButton = { FAB(trigger) },
                                                   content = { Body(realEstates, onClick, it) })
                           })
}

@Composable
fun FAB(trigger: (action: ExplorerAction) -> Unit) {
    FloatingActionButton(onClick = {},
        content = { ActionIcon(trigger, ExplorerAction.New()) })
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Body(realEstates: List<ExplorerData>, onClick: (id: Int) -> Unit, paddingValues: PaddingValues) {

    if (realEstates.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("noch nix in DB")
        }

    } else {
        val scrollBarWidth = 15.dp
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()){
            val lazyListState = rememberLazyListState()

            LazyColumn(state = lazyListState,
                    modifier = Modifier.align(Alignment.TopStart)
                                       .padding(end = scrollBarWidth)) {
                items(items = realEstates,
                        key = { it.id }) {
                    ListItem( text = { Text("${it.street} ${it.streetNumber}") },
                      overlineText = { Text("${it.zipCode}, ${it.city}") },
                     secondaryText = { Text(it.type) },
                          modifier = Modifier.clickable(onClick = { onClick(it.id) })
                    )
                    Divider()
                }
            }

            VerticalScrollbar(modifier = Modifier.align(Alignment.CenterEnd)
                                                 .width(scrollBarWidth)
                                                 .padding(start = 3.dp, end = 3.dp, top = 3.dp, bottom =  3.dp),
                               adapter = rememberScrollbarAdapter(scrollState = lazyListState)
            )
        }
    }
}
