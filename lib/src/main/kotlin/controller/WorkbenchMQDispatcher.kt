package controller

import MQ_INTERNAL_EDITOR_STATE_SAVED
import MQ_INTERNAL_EDITOR_STATE_UNSAVED
import MQ_INTERNAL_TOPIC_PATH_EDITOR
import model.WorkbenchModel
import model.data.MQClient
import java.util.concurrent.Executors

internal class WorkbenchMQDispatcher (val model: WorkbenchModel, private val commandController: WorkbenchCommandController)
{
    private var mqClient: MQClient = MQClient("workbench-editor-dispatcher", 0)

    init {
        mqClient.subscribe(MQ_INTERNAL_TOPIC_PATH_EDITOR, ::dispatchEditorMessagesForSavingState, Executors.newSingleThreadExecutor())
    }

    private fun dispatchEditorMessagesForSavingState(msg: String) {
        val splitMsg = msg.split(":")
        if (splitMsg.size == 3) {
            val type = splitMsg[0]
            val dataId = splitMsg[1].toInt()
            val cmd = splitMsg[2]
            if (cmd == MQ_INTERNAL_EDITOR_STATE_UNSAVED) {
                commandController.addUnsavedModule(type, dataId)
            } else if (cmd == MQ_INTERNAL_EDITOR_STATE_SAVED) {
                commandController.removeSavedModule(type, dataId)
            }
            model.unsavedState = model.unsavedEditors.isNotEmpty()
        }
    }




}
