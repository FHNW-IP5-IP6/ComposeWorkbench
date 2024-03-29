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
const val TYPE_ALL_REAL_ESTATES = "AllRealEstates"


fun main() {
    with(Workbench("Estate Agent Workbench", true)) {

        val repo = RealEstateRepository("/data/scratchDB".URL())

        registerEditor(
            type = TYPE_REAL_ESTATE,
            initController = { id, mqtt ->
                RealEstateController(data = repo.read(id),
                    repo = repo,
                    onChange = { field, value, someDataChanged ->
                        mqtt.publish("""$TYPE_REAL_ESTATE/$id/$field""", value)
                        if (someDataChanged) {
                            mqtt.publishUnsaved(TYPE_REAL_ESTATE, id)
                        }
                    },
                    onDelete = { println("Entity was deleted, close Editor") }
                )
            },
            icon = Icons.Default.Edit,
            title = { "${it.editorState.data.street.value} ${it.editorState.data.streetNumber.value}" },
            onSave = { controller, mqtt ->
                controller.triggerAction(RealEstateAction.Save())
                success()
            },
            editorView = { controller ->
                RealEstateEditor(editorState = controller.editorState,
                    trigger = { controller.triggerAction(it) })
            }
        )

        registerExplorer<ExplorerController>(
            type = TYPE_ALL_REAL_ESTATES,
            title = { "Real Estates" },
            init = { controller, mqtt ->
                mqtt.subscribe("$TYPE_REAL_ESTATE/#", updateTempChanges(controller))
                mqtt.subscribeForSelectedEditor(TYPE_REAL_ESTATE) { id ->
                    println("Selected Editor for type $TYPE_REAL_ESTATE with id $id")
                    controller.selectedId = id
                }
            },
            explorerView = { controller ->
                ExplorerUI(selectedId = controller.selectedId,
                    realEstates = controller.allRealEstates,
                    trigger = { controller.triggerAction(it) },
                    onClick = { requestEditor(TYPE_REAL_ESTATE, it) })

            })

        requestExplorer(TYPE_ALL_REAL_ESTATES, ExplorerController(), true, ExplorerLocation.LEFT)

        run {
            println("Exit my Compose Workbench App")
            success()
        }
    }

}

private fun updateTempChanges(c: ExplorerController) = { topic: String, value: String ->
    val topicSplit = topic.split("/")
    if (topicSplit.size == 3) {
        val id = topicSplit[1].toInt()
        val field = topicSplit[2]
        c.triggerAction(ExplorerAction.Update(id, field, value))
    }
}

private fun String.URL(): String =
    "jdbc:sqlite:${ApplicationController::class.java.getResource(this)!!.toExternalForm()}"