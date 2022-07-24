import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CitiesController (private val load: ()->CitiesState) {

    var citiesState by mutableStateOf(CitiesState())
        private set

    init {
        reload()
    }

    fun title(): String{
        return "${citiesState.title} ${citiesState.cities.size}"
    }

    fun reload() {
        citiesState = load.invoke()
    }

    fun updateField(id: Int, field: String, valStr: String) {
        val idx = citiesState.cities.indexOfFirst { it.id == id }
        if (idx == -1) return
        when (field) {
            "name" -> {
                val copiedCityState = citiesState.cities[idx].copy(name = valStr)
                val mutableList = citiesState.cities.toMutableList()
                mutableList[idx] = copiedCityState
                citiesState = citiesState.copy(cities = mutableList.toList())
            }
            "countryCode" -> {
                val copiedCityState = citiesState.cities[idx].copy(countryCode = valStr)
                val mutableList = citiesState.cities.toMutableList()
                mutableList[idx] = copiedCityState
                citiesState = citiesState.copy(cities = mutableList.toList())
            }
            "population" -> {
                val copiedCityState = citiesState.cities[idx].copy(population = valStr.toIntOrNull() ?: 0)
                val mutableList = citiesState.cities.toMutableList()
                mutableList[idx] = copiedCityState
                citiesState = citiesState.copy(cities = mutableList.toList())
            }
        }
    }

}