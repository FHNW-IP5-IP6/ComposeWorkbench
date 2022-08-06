
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home

const val CITY_MQ_TOPIC = "city-tool"

fun main() {

    val workbench = Workbench("Cities App", true)


    workbench.registerEditor<CityController>(
        type = "City",
        initController = { id, mqtt -> CityController(id) { id, field, value ->
            mqtt.publish("$CITY_MQ_TOPIC/city/$id/$field", value)
            mqtt.publishUnsaved("City", id)
        } },
        icon = Icons.Default.Edit,
        title = { controller ->  controller.cityState.name },
        onClose = {controller, mqtt ->  success()},
        onSave = { controller, mqtt ->
            if(controller.cityState.name.length > 2) {
                controller.persist()
                success()
            } else{
                ActionResult(false, "City name cannot be <= 2 chars")
            }
        }
    ){controller ->
        CityEditorUi(controller.cityState, controller::onFieldChanged)
    }

    workbench.registerEditor<CityController>(
        type = "City",
        initController = { id, mqtt -> CityController(id){ id, field, value ->
            mqtt.publish("$CITY_MQ_TOPIC/city/$id/$field", value)
            mqtt.publishUnsaved("City", id)
        } },
        icon = Icons.Default.Home,
        title = { controller -> controller.cityState.name },
        onClose = { controller, mqtt ->  success()},
        onSave = { controller, mqtt ->
            controller.persist()
            mqtt.publishSaved("City", controller.cityState.id)
            success()
        }
    ){ controller ->
        CityMapEditorUi(controller.cityState, controller::onFieldChanged)
    }

    workbench.registerExplorer<CitiesController>(
        type = "Cities",
        title = { it.title() },
        init = { controller, mqtt ->
            mqtt.subscribeForUpdates("City") { _, msg ->
                if (msg == "saved" || msg == "closed") controller.reload()
            }
            mqtt.subscribe("$CITY_MQ_TOPIC/city/#", updateTempChanges(controller))
        },
    ) { controller ->
        CitiesExplorerUi(controller.citiesState) {
            workbench.requestEditor("City", it)
        }
    }

    workbench.requestExplorer("Cities", CitiesController(CitiesRepository::getSmallCities), true, ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", CitiesController(CitiesRepository::getSwissCities), true, ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", CitiesController(CitiesRepository::getBigCities), false, ExplorerLocation.BOTTOM)
    workbench.requestExplorer("Cities", CitiesController(CitiesRepository::getGermanCities), true, ExplorerLocation.BOTTOM, false)
    workbench.run {
        println("Exit my Compose Workbench App")
        success()
    }

}

private fun updateTempChanges(controller: CitiesController) = { topic: String, msg: String ->
    val topicSplit = topic.split("/")
    if (topicSplit.size == 4) {
        val id = topicSplit[2].toIntOrNull()
        if (id != null) {
            val field = topicSplit[3]
            controller.updateField(id, field, msg)
        }
    }
}
