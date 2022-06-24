
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

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
}

fun getCityState(id: Int) = findById(id).toCityState()
fun getCityLocationState(id: Int) = findById(id).toCityLocationState()

internal fun findById(id: Int): City{
    return transaction(DbSettings.citiesDb) {
        addLogger(StdOutSqlLogger)
        City.findById(id)!!
    }
}

internal fun City.toCityState() : CityState {
    return CityState(this)
}

internal fun City.toCityLocationState(): CityLocationState {
    return CityLocationState(this)
}

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

class CityLocationState(private val city: City) {
    val id = city.id
    var name by mutableStateOf(city.name)
    var latitude by mutableStateOf(city.latitude)
    var longitude by mutableStateOf(city.longitude)

    fun persist() {
        transaction(DbSettings.citiesDb) {
            addLogger(StdOutSqlLogger)
            city.name = name
            city.longitude = longitude
            city.latitude = latitude
        }
    }
}
