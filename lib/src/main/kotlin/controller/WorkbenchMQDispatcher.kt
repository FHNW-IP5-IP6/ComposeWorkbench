package controller

import MQ_INTERNAL_EDITOR_STATE_CLOSED
import MQ_INTERNAL_EDITOR_STATE_SAVED
import MQ_INTERNAL_EDITOR_STATE_UNSAVED
import MQ_INTERNAL_TOPIC_PATH_EDITOR
import model.state.WorkbenchStaticState
import model.data.MQClient
import java.util.concurrent.Executors

internal class WorkbenchMQDispatcher (val model: WorkbenchStaticState, private val controller: WorkbenchController)
{
    private var mqClient: MQClient = MQClient("workbench-editors-dispatcher", 0)

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
                    controller.addUnsavedModule(type, dataId)
                } else if (msg == MQ_INTERNAL_EDITOR_STATE_SAVED || msg == MQ_INTERNAL_EDITOR_STATE_CLOSED) {
                    controller.removeSavedModule(type, dataId)
                }
            } catch (exception: NumberFormatException) {
                return
            }
        }
    }
}
