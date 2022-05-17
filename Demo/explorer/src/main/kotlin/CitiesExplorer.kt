
import Cities.countryCode
import Cities.population
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
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
        Column (modifier = Modifier.fillMaxWidth()) {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
                items(data.state) {
                    Card(modifier = Modifier
                        .clickable { onClick(it.id.value) }
                        .fillMaxWidth()
                        .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(4.dp))){
                        Text(text = "Name: ${it.name}, Country: ${it.countryCode}, Population: ${it.population}")
                    }
                }
            }
        }
    }
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
