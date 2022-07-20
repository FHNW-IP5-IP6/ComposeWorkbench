package realestateexplorer.controller

import androidx.compose.runtime.mutableStateListOf
import realestateexplorer.data.ExplorerData
import realestateexplorer.data.Repository

class ExplorerController {
    val repository = Repository("/data/scratchDB".URL())

    var allRealEstates = mutableStateListOf<ExplorerData>().apply {
        addAll(repository.readAll())
    }


    private fun String.URL() : String =
        "jdbc:sqlite:${ExplorerController::class.java.getResource(this)!!.toExternalForm()}"


    fun create(){
        allRealEstates.add(ExplorerData(id = repository.create()))

    }

}