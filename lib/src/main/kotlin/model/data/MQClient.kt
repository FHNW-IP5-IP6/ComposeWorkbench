package model.data

import MQ_INTERNAL_BROKER_IP_ADDRESS
import MQ_INTERNAL_BROKER_PORT
import MQ_INTERNAL_TOPIC_PATH_EDITOR
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import java.net.InetSocketAddress
import java.util.concurrent.Executor


class MQClient(ident: String) {

    private var running = false
    private lateinit var client: Mqtt5BlockingClient

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

    fun publish(msg: String) {
        if (!running) return
        client.publishWith()
            .topic(MQ_INTERNAL_TOPIC_PATH_EDITOR)
            .qos(MqttQos.AT_LEAST_ONCE)
            .payload(msg.toByteArray())
            .send()
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