package view.component


import COMMAND_IDENTIFIER_MENU_COLLAPSIBLE
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.WorkbenchModel
import model.data.ModuleType
import util.selectedButtonColors

@Composable
internal fun WorkbenchAppBar(model: WorkbenchModel) {

    TopAppBar(content = {
        Row(
            modifier = Modifier.fillMaxSize().padding(0.dp, 0.dp, 20.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //MenuEntry(true, model.commandsMenuBar)
            WBMenu(model.commandsMenus[COMMAND_IDENTIFIER_MENU_COLLAPSIBLE]!!, Icons.Default.Menu)
            Text(
                model.appTitle,
                fontSize = MaterialTheme.typography.h4.fontSize,
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
            )
            Button(
                onClick = { model.saveAll(ModuleType.EDITOR) },
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
                colors = ButtonDefaults.selectedButtonColors(true)
            ) {
                Text("Save All")
            }
        }
    })
}
