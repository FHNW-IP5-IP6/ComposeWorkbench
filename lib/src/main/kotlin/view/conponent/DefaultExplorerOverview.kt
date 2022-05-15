package view.conponent

import androidx.compose.foundation.clickable
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import model.WorkbenchModel
import util.cursorForClickable

@Composable
internal fun DefaultExplorerOverview(model: WorkbenchModel) {
    for (explorer in model.registeredDefaultExplorers) {
        Card (Modifier.cursorForClickable().clickable { model.createExplorerFromDefault(explorer.key) }) {
            Text(text = "${explorer.key}")
        }
    }
}