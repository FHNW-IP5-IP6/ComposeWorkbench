
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
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

class DataCity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DataCity>(Cities)

    var name by Cities.name
    var countryCode by Cities.countryCode
    var population by Cities.population
    var elevation: Double? by Cities.elevation
    var timeZone by Cities.timezone
    var latitude by Cities.latitude
    var longitude by Cities.longitude
}

data class CityState(
    val id: Int = -1,
    val name: String = "",
    val countryCode: String = "",
    val population: Int = 0,
    val elevation: Double?  = null,
    val timeZone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
){
    constructor(dataCity: DataCity) : this(
        dataCity.id.value,
        dataCity.name,
        dataCity.countryCode,
        dataCity.population,
        dataCity.elevation,
        dataCity.timeZone,
        dataCity.latitude,
        dataCity.longitude,
    )
}


class CityRepository {

    companion object {
        fun getCityState(id: Int) = findById(id).toCityState()

        fun persist(cityState: CityState) {
            val dataCity = findById(cityState.id)!!
            transaction(DbSettings.citiesDb) {
                dataCity.name = cityState.name
                dataCity.countryCode = cityState.countryCode
                dataCity.population = cityState.population
                dataCity.elevation = cityState.elevation
                dataCity.timeZone = cityState.timeZone
                dataCity.longitude = cityState.longitude
                dataCity.latitude = cityState.latitude
            }
        }

        fun findById(id: Int): DataCity {
            return transaction(DbSettings.citiesDb) {
                DataCity.findById(id)!!
            }
        }

        fun DataCity.toCityState(): CityState {
            return CityState(this)
        }

    }
}