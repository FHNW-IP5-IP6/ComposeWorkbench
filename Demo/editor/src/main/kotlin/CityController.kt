import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CityController(id: Int, val onValueChange: (id: Int, field: String, value: String)->Unit) {

    var cityState by mutableStateOf(CityRepository.getCityState(id))
    private set

    fun onFieldChanged(field: String, value: String) {
        when(field.lowercase()) {
            "name" -> cityState = cityState.copy(name = value)
            "countryCode" -> cityState = cityState.copy(countryCode = value)
            "population" -> cityState = cityState.copy(population = value.toIntOrNull() ?: 0)
            "elevation" -> cityState = cityState.copy(elevation = value.toDoubleOrNull() ?: 0.0)
            "timeZone" -> cityState = cityState.copy(timeZone = value)
            "latitude" -> cityState = cityState.copy(latitude = value.toDoubleOrNull() ?: 0.0)
            "longitude" -> cityState = cityState.copy(longitude = value.toDoubleOrNull() ?: 0.0)
        }
        onValueChange(cityState.id, field, value)
    }

    fun persist() {
        CityRepository.persist(cityState)
    }
}