package realestateexplorer.controller

import androidx.compose.runtime.mutableStateListOf
import realestateexplorer.data.ExplorerData
import realestateexplorer.data.Repository

class ExplorerController {
    private val repository = Repository("/data/scratchDB".URL())

    var allRealEstates = mutableStateListOf<ExplorerData>().apply {
        addAll(repository.readAll())
    }


    fun triggerAction(action: ExplorerAction){
        when(action){
            is ExplorerAction.New    -> create()
            is ExplorerAction.Update -> update(action.id, action.field, action.value)
        }
    }

    private fun update(id: Int, field: String, valueAsText: String){
        println("update explorer $id, $field, $valueAsText")
    }

    private fun create(){
        allRealEstates.add(ExplorerData(id = repository.create()))
    }


    private fun String.URL() : String =
        "jdbc:sqlite:${ExplorerController::class.java.getResource(this)!!.toExternalForm()}"

}