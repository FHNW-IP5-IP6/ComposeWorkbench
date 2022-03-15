package workbench_compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import workbench_compose.model.WorkbenchComposeModel

@Composable
fun tabs(model: WorkbenchComposeModel) {
    Column {
        if (model.tabs.size > 0) {
            if (model.tabIndex >= model.tabs.size) model.tabIndex=model.tabs.size-1
            TabRow(selectedTabIndex = model.tabIndex) {
            model.tabs.forEachIndexed { index, tab ->
                Tab(selected = model.tabIndex == index,
                    onClick = { model.tabIndex = index },
                    text = {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)
                        {
                            Text(
                                text = tab.writer,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { model.removeTab(tab) }) {
                                Icon(Icons.Filled.Close, "close")
                            }
                        }
                    }
                )
            }
        }
        model.tabs[model.tabIndex].content.invoke()
        } else {
            Text("No Tabs to display.")
        }
    }
}

@Composable
fun WorkbenchComposeUI(model: WorkbenchComposeModel) {
    MaterialTheme {
        tabs(model)
    }
}