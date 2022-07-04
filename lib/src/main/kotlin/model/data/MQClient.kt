package model.data

import MQ_INTERNAL_BROKER_IP_ADDRESS
import MQ_INTERNAL_BROKER_PORT
import MQ_INTERNAL_EDITOR_STATE_SAVED
import MQ_INTERNAL_EDITOR_STATE_UNSAVED
import MQ_INTERNAL_TOPIC_PATH_EDITOR
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import java.net.InetSocketAddress
import java.util.concurrent.Executor


class MQClient(val type: String, val id: Int) {

    private var running = false
    private lateinit var client: Mqtt5BlockingClient
    private var ident: String = "$type:$id"

    init {
        try {
            client = MqttClient.builder()
                .identifier(ident)
                .serverAddress(InetSocketAddress(MQ_INTERNAL_BROKER_IP_ADDRESS, MQ_INTERNAL_BROKER_PORT))
                .useMqttVersion5()
                .buildBlocking()
            client.connect()
            running = true
        } catch (ex:Exception) {
            running = false
        }
    }

    internal fun publish(topic: String, msg: String) {
        if (!running) return
        val msgWithId: String = "$ident:$msg"
        client.publishWith()
            .topic(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .payload(msgWithId.toByteArray())
            .send()
    }

    fun publishUnsaved() {
        publish(MQ_INTERNAL_TOPIC_PATH_EDITOR, MQ_INTERNAL_EDITOR_STATE_UNSAVED)
    }

    fun publishSaved() {
        publish(MQ_INTERNAL_TOPIC_PATH_EDITOR, MQ_INTERNAL_EDITOR_STATE_SAVED)
    }


    fun subscribe(topic: String, callBack: (String)->Unit) {
        if (!running) return
        val clbck: (Mqtt5Publish) -> Unit = {
            callBack(String(it.payloadAsBytes))
        }
        client.toAsync().subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback(clbck)
            .send()
    }

    fun subscribe(topic: String, callBack: (String)->Unit, executor: Executor) {
        if (!running) return
        val clbck: (Mqtt5Publish) -> Unit = {
            callBack(String(it.payloadAsBytes))
        }
        client.toAsync().subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .callback(clbck)
            .executor(executor)
            .send()
    }

}