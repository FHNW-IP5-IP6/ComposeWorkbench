
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        CityEditorUi(getCity(2))
    }
}

fun getCity(id: Int): CityState {
    val city = transaction(DbSettings.citiesDb) {
        City.findById(id)!!
    }
    return city.toState()
}

@Composable
fun CityEditorUi(model: CityState) {
    with(model) {
        MaterialTheme {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
        }
    }
}

/**
 * Editor Specific DB Objects.
 */
object Cities: IntIdTable() {
    val name = text("NAME")
    val countryCode = text("COUNTRY_CODE")
    val population = integer("POPULATION")
    val elevation = double("ELEVATION").nullable()
    val timezone = text("TIMEZONE")
    val latitude = double("LATITUDE")
    val longitude = double("LONGITUDE")
}

class City(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<City>(Cities)

    var name by Cities.name
    var countryCode by Cities.countryCode
    var population by Cities.population
    var elevation: Double? by Cities.elevation
    var timeZone by Cities.timezone
    var latitude by Cities.latitude
    var longitude by Cities.longitude

    fun toState() : CityState {
        return CityState(this)
    }
}

//TODO: so f***ing verbose
class CityState(private val city: City) {
    val id = city.id
    var name by mutableStateOf(city.name)
    var countryCode by mutableStateOf(city.countryCode)
    var population by mutableStateOf(city.population)
    var elevation by mutableStateOf(city.elevation)
    var timeZone by mutableStateOf(city.timeZone)
    var latitude by mutableStateOf(city.latitude)
    var longitude by mutableStateOf(city.longitude)

    fun persist() {
        transaction(DbSettings.citiesDb) {
            addLogger(StdOutSqlLogger)
            city.name = name
            city.countryCode = countryCode
            city.population = population
            city.elevation = elevation
            city.timeZone = timeZone
            city.longitude = longitude
            city.latitude = latitude
        }
    }
}

