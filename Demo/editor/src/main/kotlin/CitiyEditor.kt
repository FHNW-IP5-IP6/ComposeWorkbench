
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        CityEditorUi(findById(2661374).toCityState())
    }
}

@Composable
fun CityEditorUi(model: CityState) {
    with(model) {
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(5.dp)
        ) {
            MaterialTheme {
                val stateVertical = rememberScrollState(0)
                val stateHorizontal = rememberScrollState(0)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(stateVertical)
                        .padding(end = 12.dp, bottom = 12.dp)
                        .horizontalScroll(stateHorizontal)
                ) {
                    Column {
                        Row {
                            EditableField(label = "Name",
                                onValueChange = { name = it },
                                getValue = { name })
                            EditableField(label = "Country",
                                onValueChange = { countryCode = it },
                                getValue = { countryCode })
                        }
                        Row {
                            EditableField(label = "Population",
                                onValueChange = { population = if (it.toIntOrNull() == null) 0 else it.toInt() },
                                getValue = { population.toString() })
                            EditableField(label = "Elevation",
                                onValueChange = { elevation = if (it.toDoubleOrNull() == null) 0.0 else it.toDouble() },
                                getValue = { elevation.toString() })
                        }
                        Row {
                            EditableField(label = "Timezone",
                                onValueChange = { timeZone = it },
                                getValue = { timeZone })
                        }
                        Row {
                            EditableField(label = "Longitude",
                                onValueChange = { longitude = if (it.toDoubleOrNull() == null) 0.0 else it.toDouble() },
                                getValue = { longitude.toString() })
                            EditableField(label = "Latitude",
                                onValueChange = { latitude = if (it.toDoubleOrNull() == null) 0.0 else it.toDouble() },
                                getValue = { latitude.toString() })
                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(stateVertical)
                )
                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(end = 12.dp),
                    adapter = rememberScrollbarAdapter(stateHorizontal)
                )
            }
        }
    }
}

@Composable
internal fun EditableField(label:String ,getValue: () -> String, onValueChange: (String) -> Unit){
    TextField(
        modifier = Modifier.padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
        label = { Text(text = label) },
        value = getValue.invoke(),
        onValueChange =  onValueChange
    )
}