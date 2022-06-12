package view.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import model.WorkbenchModel
import util.cursorForClickable

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DefaultExplorerOverview(model: WorkbenchModel) {

    LazyVerticalGrid(
        cells = GridCells.Fixed(2),
        contentPadding = PaddingValues(30.dp)
    ) {
        model.registeredDefaultExplorers.keys.forEach{ id ->
            item {
                OverviewCard(
                    model.registeredDefaultExplorers[id]!!.getTitle()
                ) { model.createExplorerFromDefault(id) }
            }
        }
    }

}

@Composable
internal fun OverviewCard (title: String, onClick: ()->Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .cursorForClickable()
            .clickable { onClick.invoke() },
        elevation = 5.dp,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(text = title,
            Modifier.padding(20.dp, 40.dp, 20.dp, 40.dp),
            textAlign = TextAlign.Center,
        )
    }
}
