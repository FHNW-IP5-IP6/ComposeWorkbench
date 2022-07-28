package realestateexplorer.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import realestateexplorer.data.ExplorerData
import realestateexplorer.data.Repository

class ExplorerController {
    private val repository = Repository("/data/scratchDB".URL())

    var allRealEstates = mutableStateListOf<ExplorerData>().apply {
        addAll(repository.readAll())
    }

    var selectedId: Int? by mutableStateOf(null)

    fun triggerAction(action: ExplorerAction){
        when(action){
            is ExplorerAction.New    -> create()
            is ExplorerAction.Update -> update(action.id, action.field, action.value)
        }
    }

    private fun update(id: Int, field: String, valueAsText: String) {
        val idx = allRealEstates.indexOfFirst { it.id == id }
        val old = allRealEstates.get(idx)
        val new = when (field) {
            "TYPE"          -> old.copy(type = valueAsText)
            "STREET"        -> old.copy(street = valueAsText)
            "STREET_NUMBER" -> old.copy(streetNumber = valueAsText)
            "ZIP_CODE"      -> old.copy(zipCode = valueAsText.toInt())
            "CITY"          -> old.copy(city = valueAsText)
            else            -> null
        }

        if (null != new) {
            allRealEstates.removeAt(idx)
            allRealEstates.add(idx, new)
        }
    }

    private fun create(){
        allRealEstates.add(ExplorerData(id = repository.create(), type = "Gewerblich"))
    }


    private fun String.URL() : String =
        "jdbc:sqlite:${ExplorerController::class.java.getResource(this)!!.toExternalForm()}"

}