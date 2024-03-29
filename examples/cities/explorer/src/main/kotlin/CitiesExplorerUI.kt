import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
fun CitiesExplorerUi(data: CitiesState, onClick: (id: Int) -> Unit) {
    MaterialTheme {
        Column {
            Row(modifier = Modifier.padding(5.dp).background(Color.LightGray)) {
                TableCell(text = "City", weight = 0.4f)
                TableCell(text = "Country Code", weight = 0.3f)
                TableCell(text = "Population", weight = 0.3f)
            }
            LazyColumn(modifier = Modifier.padding(5.dp)) {
                items(data.cities) {
                    Row(modifier = Modifier.clickable { onClick(it.id) }) {
                        TableCell(text = it.name, weight = 0.4f)
                        TableCell(text = it.countryCode, weight = 0.3f)
                        TableCell(text = it.population.toString(), weight = 0.3f)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float
) {
    Text(text = text,
        maxLines = 1,
        modifier = Modifier
        .border(1.dp, Color.Gray)
        .weight(weight)
        .padding(4.dp)
    )
}


