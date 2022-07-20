package realestateexplorer

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import realestateexplorer.controller.ExplorerController
import realestateexplorer.view.ExplorerUI

fun main() {
    val controller = ExplorerController()
    controller.create()
    controller.create()
    controller.create()

    application {
        Window(
            title = "Real Estates",
            state = rememberWindowState(
                width = 450.dp,
                height = 400.dp,
                position = WindowPosition(Alignment.Center)
            ),
            onCloseRequest = ::exitApplication
        ) {
            ExplorerUI(controller.allRealEstates)
        }

    }
}