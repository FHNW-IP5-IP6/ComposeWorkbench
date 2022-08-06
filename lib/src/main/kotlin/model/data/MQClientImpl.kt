package model.data

import MQ_INTERNAL_BROKER_IP_ADDRESS
import MQ_INTERNAL_BROKER_PORT
import MQ_INTERNAL_EDITOR_STATE_CLOSED
import MQ_INTERNAL_EDITOR_STATE_CREATED
import MQ_INTERNAL_EDITOR_STATE_SAVED
import MQ_INTERNAL_EDITOR_STATE_SELECTED
import MQ_INTERNAL_EDITOR_STATE_UNSAVED
import MQ_INTERNAL_TOPIC_PATH_EDITOR
import UpdateType
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import toUpdateType
import java.net.InetSocketAddress
import java.util.concurrent.Executor


internal object MQClientImpl: MqClient {

    private var running = false
    private lateinit var client: Mqtt5BlockingClient

    init {
        try {
            client = MqttClient.builder()
                .identifier(this.hashCode().toString())
                .serverAddress(InetSocketAddress(MQ_INTERNAL_BROKER_IP_ADDRESS, MQ_INTERNAL_BROKER_PORT))
                .useMqttVersion5()
                .buildBlocking()
            client.connect()
            running = true
        } catch (ex:Exception) {
            running = false
        }
    }

    override fun publish(topic: String, msg: String) {
        if (!running) return
        client.publishWith()
            .topic(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .payload(msg.toByteArray())
            .send()
    }

    override fun publish(type: String, id: Int, msg: String) {
        publish("$MQ_INTERNAL_TOPIC_PATH_EDITOR/$type/$id", msg)
    }

    internal fun publishCreated(type: String, id: Int) {
        publish(type, id, MQ_INTERNAL_EDITOR_STATE_CREATED)
    }

    override fun publishUnsaved(type: String, id: Int) {
        publish(type, id, MQ_INTERNAL_EDITOR_STATE_UNSAVED)
    }

    internal fun publishSaved(type: String, id: Int) {
        publish(type, id, MQ_INTERNAL_EDITOR_STATE_SAVED)
    }

    internal fun publishClosed(type: String, id: Int) {
        publish(type, id, MQ_INTERNAL_EDITOR_STATE_CLOSED)
    }

    internal fun publishSelected(type: String, id: Int) {
        publish("$MQ_INTERNAL_TOPIC_PATH_EDITOR/$type/$id/$MQ_INTERNAL_EDITOR_STATE_SELECTED", "")
    }

    override fun subscribeForSelectedEditor(editorType: String, callBack: (Int)->Unit) {
        val clbck: (String, String) -> Unit = {topic,_->
            val splitTopic = topic.split("/")
            if (splitTopic.size == 5) {
                val dataId = splitTopic[3].toIntOrNull()
                if (dataId != null) callBack(dataId)
            }
        }
        subscribe("$MQ_INTERNAL_TOPIC_PATH_EDITOR/$editorType/+/$MQ_INTERNAL_EDITOR_STATE_SELECTED", clbck)
    }

    override fun subscribe(topic: String, callBack: (String, String)->Unit) {
        if (!running) return
        val clbck: (Mqtt5Publish) -> Unit = {
            callBack(it.topic.toString(), String(it.payloadAsBytes))
        }
        client.toAsync().subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback(clbck)
            .send()
    }

    internal fun subscribeOnce(topic: String, callBack: (String, String)->Unit) {
        if (!running) return
        val clbck: (Mqtt5Publish) -> Unit = {
            callBack(it.topic.toString(), String(it.payloadAsBytes))
        }
        client.toAsync().subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.EXACTLY_ONCE)
            .callback(clbck)
            .send()
    }

    override fun subscribe(topic: String, callBack: (String, String)->Unit, executor: Executor) {
        if (!running) return
        val clbck: (Mqtt5Publish) -> Unit = {
            callBack(it.topic.toString(), String(it.payloadAsBytes))
        }
        client.toAsync().subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback(clbck)
            .executor(executor)
            .send()
    }

    override fun subscribeForUpdates(editorType: String, callBack: (id: Int, updateType: UpdateType)->Unit) {
        val clbck: (String, String) -> Unit = {topic,msg->
            val splitTopic = topic.split("/")
            var id = -1
            if (splitTopic.size == 4) {
                val dataId = splitTopic[3].toIntOrNull()
                if (dataId != null) id = dataId
            }
            callBack(id, toUpdateType(msg))
        }
        subscribe("$MQ_INTERNAL_TOPIC_PATH_EDITOR/$editorType/#", clbck)
    }
}