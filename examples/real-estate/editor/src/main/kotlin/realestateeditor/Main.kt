package realestateeditor

import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import androidx.compose.ui.window.application
import realestateeditor.controller.ApplicationAction
import realestateeditor.controller.ApplicationController
import realestateeditor.view.ApplicationUI

fun main() {
    LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).level = Level.INFO

    val controller = ApplicationController()
    controller.triggerAction(ApplicationAction.New())

    application {
        ApplicationUI(applicationState = controller.applicationState,
                 realEstateControllers = controller.realEstateControllers,
                               trigger = { controller.triggerAction(action = it)} )
    }
}