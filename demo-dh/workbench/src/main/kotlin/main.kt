
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

import realestateeditor.controller.ApplicationController
import realestateeditor.controller.RealEstateAction
import realestateeditor.controller.RealEstateController
import realestateeditor.data.RealEstateRepository
import realestateeditor.view.RealEstateEditor
import realestateexplorer.controller.ExplorerAction
import realestateexplorer.controller.ExplorerController
import realestateexplorer.view.ExplorerUI

const val TYPE_REAL_ESTATE = "RealEstate"


fun main() {

    val workbench = Workbench("Estate Agent Workbench", true)

    val repo = RealEstateRepository("/data/scratchDB".URL())

    workbench.registerEditor(
        type = TYPE_REAL_ESTATE,
        loader = { id, mqtt ->  RealEstateController(data = repo.read(id),
                                                    repo = repo,
                                                    onChange = { field, value ->
                                                        println("$field changed to $value")
                                                        mqtt.publish("""$TYPE_REAL_ESTATE/$id/$field/$value""", "")
                                                        mqtt.publishUnsaved(TYPE_REAL_ESTATE, id)
                                                    }) },
        icon = Icons.Default.Edit,
        title = { "${it.editorState.data.id}" },
        onClose = {controller, mqtt  ->  },
        onSave = { controller, mqtt ->
            controller.triggerAction(RealEstateAction.Save())
            mqtt.publishSaved(TYPE_REAL_ESTATE, controller.editorState.data.id)
            true
        },
        content = { controller ->
            RealEstateEditor(editorState = controller.editorState,
                trigger = { controller.triggerAction(it) })
        }
    )

    workbench.registerExplorer<ExplorerController>(
        type    = "RealEstates",
        title   = { "Real Estates" },
        initMessaging = {controller, mqtt ->
            mqtt.subscribe("$TYPE_REAL_ESTATE/#", updateTempChanges(controller) )
            mqtt.subscribeForSelectedEditor(TYPE_REAL_ESTATE) { id ->
                println("Selected Editor for type $TYPE_REAL_ESTATE with id $id")
            }
        },
        content = { controller ->
            ExplorerUI(realEstates = controller.allRealEstates,
                           trigger = { controller.triggerAction(it) },
                           onClick = { workbench.requestEditor<RealEstateController>("RealEstate", it)})

        })

    workbench.requestExplorer("RealEstates", ExplorerController(), true, ExplorerLocation.LEFT)

//    workbench.registerExplorer<CitiesState>(type = "Cities", title = { it.title() }
//    ) { m, c ->
//        c.subscribeForUpdates("City") { id, msg ->
//            if (msg == "saved" || msg == "closed") m.reload(id)
//        }
//        //c.subscribe("$CITY_MQ_TOPIC/city/#", updateTempChanges(m))
//        CitiesExplorerUi(m) {
//            workbench.requestEditor<CityState>("City", it)
//        }
//    }



    workbench.run { println("Exit my Compose Workbench App") }
}

private fun updateTempChanges(c: ExplorerController) = { topic: String, msg: String ->
    val topicSplit = topic.split("/")
    if (topicSplit.size == 4) {
        val id = topicSplit[1].toIntOrNull()
        if (id != null) {
            val field = topicSplit[2]
            val value = topicSplit[3]
            c.triggerAction(ExplorerAction.Update(id, field, value))
        }
    }
}

private fun String.URL() : String =
    "jdbc:sqlite:${ApplicationController::class.java.getResource(this)!!.toExternalForm()}"