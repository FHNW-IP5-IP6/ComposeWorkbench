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
    for (id in model.registeredDefaultExplorers.keys) {

        Card (Modifier.cursorForClickable().clickable { model.createExplorerFromDefault(id) }) {
            Text(text = model.registeredDefaultExplorers[id]!!.getTitle())
        }
    }
}