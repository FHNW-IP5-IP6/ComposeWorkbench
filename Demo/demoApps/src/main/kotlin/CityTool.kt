
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home

const val CITY_MQ_TOPIC = "city-tool"

fun main() {

    val workbench = Workbench("Cities App", true)
    val explorerModel: List<CitiesState> = listOf(getSwissCities(),getSmallCities(), getBigCities(),  getGermanCities())


    workbench.registerEditor<CityState>(
        type = "City",
        controller = {controller, mqtt -> getCityState(controller)},
        icon = Icons.Default.Edit,
        title = { it.name },
        onClose = {controller, mqtt ->  },
        onSave = { controller, mqtt ->
            controller.persist()
            mqtt.publishSaved("City", controller.id)
            true
        }
    ){controller ->
        CityEditorUi(controller)
    }

    workbench.registerEditor<CityLocationState>(
        type = "City",
        loader = {controller, mqtt -> getCityLocationState(controller)},
        icon = Icons.Default.Home,
        title = { it.name },
        onClose = { controller, mqtt ->  },
        onSave = { controller, mqtt ->
            controller.persist()
            mqtt.publishSaved("City", controller.id.value)
            true
        }
    ){ c ->
        CityMapEditorUi(c)
    }

    workbench.registerExplorer<CitiesState>(type = "Cities", title = { it.title() }
    ) { m, c ->
        c.subscribeForUpdates("City") { id, msg ->
            if (msg == "saved" || msg == "closed") m.reload(id)
        }
        c.subscribe("$CITY_MQ_TOPIC/city/#", updateTempChanges(m))
        CitiesExplorerUi(m) {
            workbench.requestEditor<CityState>("City", it)
        }
    }

    workbench.requestExplorer("Cities", explorerModel[1], true, ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", explorerModel[0], true, ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", explorerModel[2], false, ExplorerLocation.BOTTOM)
    workbench.requestExplorer("Cities", explorerModel[3], true, ExplorerLocation.BOTTOM, false)
    workbench.run { println("Exit my Compose Workbench App") }

}

private fun updateTempChanges(m: CitiesState) = { topic: String, msg: String ->
    val topicSplit = topic.split("/")
    if (topicSplit.size == 4) {
        val id = topicSplit[2]
        val city = m.state.find { it.id.toString() == id }
        if (city != null) {
            when (topicSplit[3]) {
                "name" -> city.name = msg
                "country" -> city.countryCode = msg
                "population" -> {
                    val pop = msg.toIntOrNull()
                    if (pop != null) {
                        city.population = pop
                    }
                }
            }
        }
    }

}
