package estateagent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

import Workbench
import realestateeditor.controller.ApplicationController
import realestateeditor.controller.RealEstateAction
import realestateeditor.controller.RealEstateController
import realestateeditor.data.RealEstateRepository
import realestateeditor.view.RealEstateEditor
import realestateeditor.view.RealEstateEditorWindow
import realestateexplorer.controller.ExplorerController
import realestateexplorer.view.ExplorerUI


fun main() {

    val workbench = Workbench("Estate Agent Workbench", false)

    val repo = RealEstateRepository("/data/scratchDB".URL())
    repo.create()

    workbench.registerEditor(
        type = "RealEstate",
        loader = { RealEstateController(repo.read(it), repo) },
        icon = Icons.Default.Edit,
        title = { "$it.editorState.data.id" },
        onClose = {controller, mqtt  ->  },
        onSave = { controller, mqtt ->
            controller.triggerAction(RealEstateAction.Save())
            mqtt.publishSaved()
            true
        }
    ){m, c ->
        RealEstateEditor(m.editorState, {m.triggerAction(it)})
    }

    workbench.registerExplorer<ExplorerController>(
        type = "RealEstates",
        title = {"Real Estates"}){m, c ->
        ExplorerUI(m.allRealEstates)

    }

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
//
//    workbench.requestExplorer("Cities", getSwissCities(), true, ExplorerLocation.LEFT)

    workbench.requestEditor<RealEstateController>("RealEstate", 1)

    workbench.run { println("Exit my Compose Workbench App") }
}

private fun String.URL() : String =
    "jdbc:sqlite:${ApplicationController::class.java.getResource(this)!!.toExternalForm()}"