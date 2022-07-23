package realestateeditor.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import allpurpose.model.UndoState
import allpurpose.view.ActionIconStrip
import allpurpose.view.AttributeField
import allpurpose.view.AttributeRadioButtonGroup
import allpurpose.view.AttributeTextAreaField
import realestateeditor.controller.RealEstateAction
import realestateeditor.controller.RealEstateController
import realestateeditor.data.RealEstateData
import realestateeditor.data.RealEstateType
import realestateeditor.model.RealEstateEditorState


@Composable
fun RealEstateEditorWindow(controller: RealEstateController, onClose: (id: Int) -> Unit){
    val trigger : (RealEstateAction) -> Unit = { controller.triggerAction(it) }

    with(controller.editorState){
        Window(    title = "${data.street.value} ${data.streetNumber.value}, ${data.city.value}",
                   state = windowState,
          onCloseRequest = { onClose(data.id) },
                 content = { //WindowMenuBar(trigger)

                             RealEstateEditor(this@with, trigger)
                           })
    }
}

@Composable
fun RealEstateEditor(editorState: RealEstateEditorState, trigger: (RealEstateAction) -> Unit){
    MaterialTheme(colors = lightColors(primary = Color(0xFFEBE8DF),
          primaryVariant = Color(0xFF6A6A6A),
               secondary = Color(0xFFFBBA00),
        secondaryVariant = Color(0xFFA6572),
              background = Color(0xFFFFFFFC),
               onPrimary = Color(0xFF343434)),
                 content = { with(editorState) {
                              Scaffold(topBar = { Bar(isChanged, undoState, trigger) },
                                      content = { Body(data, trigger) })
                           }})

}


@Composable
private fun Bar(saveEnabled : Boolean, undoState: UndoState, trigger: (RealEstateAction) -> Unit) {
    TopAppBar (title = { ActionIconStrip(trigger,
                                         listOf(RealEstateAction.Save(saveEnabled)),
                                         listOf(
                                             RealEstateAction.Undo(undoState.undoAvailable),
                                             RealEstateAction.Redo(undoState.redoAvailable)
                                         ),
                                         listOf(RealEstateAction.Delete()),
                                    )
                      })
}


@Composable
private fun Body(info: RealEstateData, trigger: (RealEstateAction) -> Unit) {
    Column(Modifier.padding(20.dp).fillMaxSize().width(IntrinsicSize.Max), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        AttributeRadioButtonGroup(attribute = info.type,
            options = RealEstateType.values(),
            onSelection = { trigger(RealEstateAction.UpdateType(it)) })

        FormRow("Adresse") {
            AttributeField(attribute = info.street,
                       onValueChange = { trigger(RealEstateAction.UpdateStreet(it)) },
                            modifier = Modifier.fillMaxWidth(0.8f))

            AttributeField(attribute = info.streetNumber,
                       onValueChange = { trigger(RealEstateAction.UpdateStreetNumber(it)) },
                            modifier = Modifier.fillMaxWidth())
        }

        FormRow("") {
            AttributeField(attribute = info.zipCode,
                       onValueChange = { trigger(RealEstateAction.UpdateZipCode(it)) },
                            modifier = Modifier.width(90.dp),
                           textAlign = TextAlign.End)

            AttributeField(attribute = info.city,
                       onValueChange = { trigger(RealEstateAction.UpdateCity(it)) },
                            modifier = Modifier.fillMaxWidth())

        }

        Spacer(Modifier.height(15.dp))

        FormRow("Baujahr") {
            AttributeField(attribute = info.yearOfConstruction,
                       onValueChange = { trigger(RealEstateAction.UpdateYearOfConstruction(it)) },
                            modifier = Modifier.width(90.dp),
                           textAlign = TextAlign.End)
        }

        FormRow("Marktwert") {
            AttributeField(attribute = info.marketValue,
                       onValueChange = { trigger(RealEstateAction.UpdateMarketValue(it)) },
                            modifier = Modifier.fillMaxWidth(0.3f),
                           textAlign = TextAlign.End)
            Text("SFr")
        }

        Spacer(Modifier.height(15.dp))

        AttributeTextAreaField(attribute = info.description,
                           onValueChange = { trigger(RealEstateAction.UpdateDescription(it)) },
                                modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun FormRow(label: String, content: @Composable RowScope.() -> Unit) {
    Row(  verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
                    content = { Label(label)
                                content()
                              }
    )
}

@Composable
private fun Label(text: String){
    Text(text = text, modifier = Modifier.width(90.dp), overflow = TextOverflow.Ellipsis, softWrap = false)
}

//@Composable
//private fun FrameWindowScope.WindowMenuBar(onActionRequired: (RealEstateAction) -> Unit) {
//    MenuBar {
//        Menu("File") {
//            Item("New ",    onClick = { onActionRequired(New()) })
//            Item("Open...", onClick = { } )
//            Item("Save",    onClick = { })
//        }
//    }
//}