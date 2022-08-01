package controller

import MQ_INTERNAL_EDITOR_STATE_CLOSED
import MQ_INTERNAL_EDITOR_STATE_SAVED
import MQ_INTERNAL_EDITOR_STATE_UNSAVED
import MQ_INTERNAL_TOPIC_PATH_EDITOR
import controller.WorkbenchController.addUnsavedModule
import controller.WorkbenchController.removeSavedModule
import model.data.MQClientImpl
import java.util.concurrent.Executors

internal class WorkbenchMQDispatcher
{
    private var mqClient: MQClientImpl = MQClientImpl

    init {
        mqClient.subscribe("$MQ_INTERNAL_TOPIC_PATH_EDITOR/#", ::dispatchEditorMessages, Executors.newSingleThreadExecutor())
    }

    private fun dispatchEditorMessages(topic: String, msg: String) {
        val splitTopic = topic.split("/")
        if (splitTopic.size == 4) {
            val type = splitTopic[splitTopic.size-2]
            try {
                val dataId = splitTopic[splitTopic.size-1].toInt()
                if (msg == MQ_INTERNAL_EDITOR_STATE_UNSAVED) {
                    addUnsavedModule(type, dataId)
                } else if (msg == MQ_INTERNAL_EDITOR_STATE_SAVED || msg == MQ_INTERNAL_EDITOR_STATE_CLOSED) {
                    removeSavedModule(type, dataId)
                }
            } catch (exception: NumberFormatException) {
                return
            }
        }
    }
}
