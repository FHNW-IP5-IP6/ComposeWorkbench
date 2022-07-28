
import Cities.countryCode
import Cities.population
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction


data class CitiesState(
    val title: String = "",
    val cities: List<CityCState> = emptyList(),
)

object Cities: IntIdTable() {
    val name = text("NAME")
    val countryCode = text("COUNTRY_CODE")
    val population = integer("POPULATION")
}

class DataCityC(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DataCityC>(Cities)

    var name by Cities.name
    var countryCode by Cities.countryCode
    var population by Cities.population
}

data class CityCState(
    val id: Int = -1,
    val name: String = "",
    val countryCode: String = "",
    val population: Int = 0,
)
{
    constructor (dataCity: DataCityC) : this(
        dataCity.id.value,
        dataCity.name,
        dataCity.countryCode,
        dataCity.population,
    )
}

class CitiesRepository {

    companion object {

        fun getCityById(id: Int): CityCState? {
            val dbRead = transaction((DbSettings.citiesDb)) {
                DataCityC.findById(id)
            }
            return if (dbRead != null) CityCState(dbRead) else null
        }

        fun getSmallCities(): CitiesState {
            val dbRead = { transaction((DbSettings.citiesDb)) {
                DataCityC.find { population less 500 and(population greater 0) }
                    .sortedBy { it.population }
                    .toList()
            }
            }
            return CitiesState("Small Cities", dbRead().map { CityCState(it) })
        }

        fun getBigCities(): CitiesState {
            val dbRead = { transaction((DbSettings.citiesDb)) {
                DataCityC.find { population greater 300000 }
                    .sortedByDescending { it.population }
                    .toList()
            }
            }
            return CitiesState("Big Cities", dbRead().map{ CityCState(it) })
        }

        fun getSwissCities(): CitiesState {
            val dbRead = { transaction(DbSettings.citiesDb) {
                DataCityC.find { countryCode eq "CH" }.toList()
            }
            }
            return CitiesState("Swiss Cities", dbRead().map { CityCState(it) })
        }

        fun getGermanCities(): CitiesState {
            val dbRead = { transaction(DbSettings.citiesDb) {
                DataCityC.find { countryCode eq "DE" }.toList()
            }
            }
            return CitiesState("German Cities", dbRead().map { CityCState(it) })
        }
    }
}
