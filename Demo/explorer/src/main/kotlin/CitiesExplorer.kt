
import Cities.countryCode
import Cities.population
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        CitiesExplorerUi(getSwissCities()){}
    }
}

fun getSmallCities(): CitiesState {
    val dbRead = { transaction((DbSettings.citiesDb)) {
        City.find { population less 10000 and(population greater 0) }.toList()
    }}
    return CitiesState(dbRead(), dbRead)
}

fun getBigCities(): CitiesState {
    val dbRead = { transaction((DbSettings.citiesDb)) {
        City.find { population greater 10000000 }.toList()
    }}
    return CitiesState(dbRead(), dbRead)
}

fun getSwissCities(): CitiesState {
    val dbRead = { transaction(DbSettings.citiesDb) {
        City.find { countryCode eq "CH" }.toList()
    }}
    return CitiesState(dbRead(), dbRead)
}

fun getGermanCities(): CitiesState {
    val dbRead = { transaction(DbSettings.citiesDb) {
        City.find { countryCode eq "DE" }.toList()
    }}
    return CitiesState(dbRead(), dbRead)
}

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
                items(data.state) {
                    Row(modifier = Modifier.clickable { onClick(it.id.value) }) {
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

/**
 * Explorer Specific DB Objects.
 */
class CitiesState(
    private val cities: List<City>,
    private val reload: () -> List<City> ) {

    var state by mutableStateOf(cities)

    fun reload(){
        state = reload.invoke()
    }
}

object Cities: IntIdTable() {
    val name = text("NAME")
    val countryCode = text("COUNTRY_CODE")
    val population = integer("POPULATION")
}

class City(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<City>(Cities)

    var name by Cities.name
    var countryCode by Cities.countryCode
    var population by Cities.population
}
