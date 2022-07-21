
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

import realestateeditor.controller.ApplicationController
import realestateeditor.controller.RealEstateAction
import realestateeditor.controller.RealEstateController
import realestateeditor.data.RealEstateRepository
import realestateeditor.view.RealEstateEditor
import realestateexplorer.controller.ExplorerController
import realestateexplorer.view.ExplorerUI


fun main() {

    val workbench = Workbench("Estate Agent Workbench", false)

    val repo = RealEstateRepository("/data/scratchDB".URL())


    workbench.registerEditor(
        type = "RealEstate",
        controller = { id, mqtt ->  RealEstateController(data = repo.read(id),
                                                        repo = repo,
                                                    onChange = { field, value ->
                                                        println("$field changed to $value")
                                                        mqtt.publish("""RealEstate/$id/$field/$value""")
                                                    }) },
        icon = Icons.Default.Edit,
        title = { "${it.editorState.data.id}" },
        onClose = {controller, mqtt  ->  },
        onSave = { controller, mqtt ->
            controller.triggerAction(RealEstateAction.Save())
            mqtt.publishSaved()
            true
        },
        content = { controller, mqtt ->
            RealEstateEditor(editorState = controller.editorState,
                trigger = { controller.triggerAction(it) })
        }
    )

    workbench.registerExplorer<ExplorerController>(
        type    = "RealEstates",
        title   = { "Real Estates" },
        content = { controller, mqtt ->
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

private fun String.URL() : String =
    "jdbc:sqlite:${ApplicationController::class.java.getResource(this)!!.toExternalForm()}"