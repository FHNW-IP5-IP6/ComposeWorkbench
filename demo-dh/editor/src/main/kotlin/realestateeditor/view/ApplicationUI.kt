package realestateeditor.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.ui.window.rememberTrayState
import realestateeditor.controller.ApplicationAction
import allpurpose.view.ActionMenuItem
import realestateeditor.controller.RealEstateController
import realestateeditor.data.ApplicationState


@Composable
fun ApplicationScope.ApplicationUI(
    applicationState: ApplicationState,
    realEstateControllers: List<RealEstateController>,
    trigger: (ApplicationAction) -> Unit){

    MaterialTheme(colors = lightColors(primary = Color(0xFFEBE8DF),
                                primaryVariant = Color(0xFF6A6A6A),
                                     secondary = Color(0xFFFBBA00),
                              secondaryVariant = Color(0xFFA6572),
                                    background = Color(0xFFFFFFFC),
                                     onPrimary = Color(0xFF343434)),
                                       content = { realEstateControllers.forEach {
                                                      key(it){
                                                         RealEstateEditorWindow(it)
                                                      }}

                                                   OpenDialog(applicationState, trigger)

                                                 })


    val trayState = rememberTrayState()
    Tray(state = trayState,
          icon = TrayIcon,
          menu = { ActionMenuItem(trigger, ApplicationAction.New())
                   ActionMenuItem(trigger, ApplicationAction.OpenDialog())
                   Separator()

                   Item(text = "Exit",
                     onClick = { exitApplication() })  //todo: should trigger an Action
                  }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun OpenDialog(applicationState: ApplicationState, trigger: (ApplicationAction) -> Unit) {
    with(applicationState) {
        if (isDialogOpen) {
            Dialog(onCloseRequest = { trigger(ApplicationAction.CloseDialog()) },
                            state = rememberDialogState(position = WindowPosition(Alignment.Center))) {
                if (realEstates.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text("noch nix in DB")
                    }

                } else {
                    Column(Modifier.fillMaxSize()) {
                        LazyColumn {
                            items(realEstates) {
                                ListItem( text = { Text("${it.street.valueAsText} ${it.streetNumber.valueAsText}") },
                                  overlineText = { Text("${it.zipCode.valueAsText}, ${it.city.valueAsText}") },
                                      modifier = Modifier.clickable(onClick = { trigger(ApplicationAction.Open(it.id)) })
                                )
                                Divider()
                            }
                        }
                    }
                }
              }
        }
    }
}

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFBBA00))
        drawOval(Color(0xFFEBE8DF), size = Size(size.width - 22, size.height - 22), topLeft = Offset(11f, 11f))
    }
}