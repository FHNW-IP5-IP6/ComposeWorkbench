package view.component


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import controller.WorkbenchController
import model.data.enums.MenuType
import model.data.enums.ModuleType
import util.selectedButtonColors

@Composable
internal fun WorkbenchAppBar(controller: WorkbenchController) {

    TopAppBar(content = {
        Row(
            modifier = Modifier.fillMaxSize().padding(0.dp, 0.dp, 20.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkbenchMenu(controller.commandController.getMenuEntry(MenuType.MenuAppBar), Icons.Default.Menu)
            Text(
                controller.getAppTitle(),
                fontSize = MaterialTheme.typography.h4.fontSize,
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
            )
            Button(
                onClick = { controller.commandController.saveAll() },
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
                colors = ButtonDefaults.selectedButtonColors(true),
                enabled = controller.model.unsavedState
            ) {
                Text("Save All")
            }
        }
    })
}
