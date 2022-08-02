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
import controller.Action
import controller.WorkbenchAction
import model.data.enums.MenuType
import model.state.WorkbenchInformationState
import util.selectedButtonColors

@Composable
internal fun WorkbenchAppBar(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit
) {

    TopAppBar(content = {
        Row(
            modifier = Modifier.fillMaxSize().padding(0.dp, 0.dp, 20.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkbenchMenu(informationState.getMenuEntry(MenuType.MenuAppBar), Icons.Default.Menu, onActionRequired)
            Text(
                informationState.appTitle,
                fontSize = MaterialTheme.typography.h4.fontSize,
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
            )
            Button(
                onClick = { onActionRequired.invoke(WorkbenchAction.SaveAll()) },
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
                colors = ButtonDefaults.selectedButtonColors(true),
                enabled = informationState.unsavedEditors.isNotEmpty()
            ) {
                Text("Save All")
            }
        }
    })
}
