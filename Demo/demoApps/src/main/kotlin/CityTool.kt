
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home

fun main() {

    val workbench: Workbench = Workbench("Cities App")
    val explorerModel: List<CitiesState> = listOf(getSwissCities(),getSmallCities(), getBigCities(),  getGermanCities())

    workbench.registerEditor<CityState>(type = "City",
        loader = {getCityState(it)},
        icon = Icons.Default.Edit,
        title = { it.name },
        onSave = { it.persist() }
        ) {
            m -> CityEditorUi(m)
            explorerModel.forEach{ it.reload() }
            //TODO: replace with message to explorer
    }

    workbench.registerEditor<CityLocationState>(type = "City",
        loader = {getCityLocationState(it)},
        icon = Icons.Default.Home,
        title = { it.name },
        onSave = { it.persist() }
        ) {
            m -> CityMapEditorUi(m)
            explorerModel.forEach{ it.reload() }
            //TODO: replace with message to explorer
    }

    workbench.registerExplorer<CitiesState>(type = "Cities", title = { it.title() }
    ) { m ->
        CitiesExplorerUi(m) {
            workbench.requestEditor<CityState>("City", it)
        }
    }

    workbench.requestExplorer("Cities", explorerModel[0], true, ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", explorerModel[1], true, ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", explorerModel[2], false, ExplorerLocation.BOTTOM)
    workbench.requestExplorer("Cities", explorerModel[3], true, ExplorerLocation.BOTTOM, false)
    workbench.run { println("Exit my Compose Workbench App") }
}
