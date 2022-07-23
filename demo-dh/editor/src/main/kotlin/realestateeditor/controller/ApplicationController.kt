package realestateeditor.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import realestateeditor.data.ApplicationState
import realestateeditor.data.RealEstateData
import realestateeditor.data.RealEstateRepository

class ApplicationController {
    val realEstateControllers = mutableStateListOf<RealEstateController>()

    var applicationState by mutableStateOf(ApplicationState(realEstates = emptyList()))
    private set

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val repo = RealEstateRepository("/data/scratchDB".URL())

    fun triggerAction(action: ApplicationAction){
        when(action){
            is ApplicationAction.New         -> create()
            is ApplicationAction.Open        -> open(action.id)
            is ApplicationAction.Close       -> close(action.id)
            is ApplicationAction.OpenDialog  -> openDialog()
            is ApplicationAction.CloseDialog -> closeDialog()
            is ApplicationAction.Exit        -> {} //todo: should be done here, not in UI
        }
    }

    private fun create() = editorController(RealEstateData(id = repo.create()))

    private fun open(id: Int): Job {
        closeDialog()
        return editorController(repo.read(id))
    }

    private fun close(id: Int) = realEstateControllers.removeIf { it.editorState.data.id == id }

    private fun openDialog() =
        ioScope.launch {
            val onDB = repo.readAll().toMutableList()
            val alreadyOpen = realEstateControllers.map{it.editorState.data.id}
            onDB.removeIf{alreadyOpen.contains(it.id)}
            applicationState = applicationState.copy(isDialogOpen = true, realEstates = onDB)
        }

    private fun closeDialog() {
        applicationState = applicationState.copy(isDialogOpen = false)
    }


    private fun editorController(data: RealEstateData) =
        ioScope.launch {
            realEstateControllers.add(
                RealEstateController(data = data,
                                     repo = repo,
                                 onDelete = { close(data.id) }
                )
            )
        }

    private fun String.URL() : String =
        "jdbc:sqlite:${ApplicationController::class.java.getResource(this)!!.toExternalForm()}"
}