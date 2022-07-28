
import java.util.concurrent.Executor

interface MqClient {

    fun publish(topic: String, msg: String)
    fun publish(type: String, id: Int, msg: String)
    
    fun subscribe(topic: String, callBack: (String, String)->Unit)
    fun subscribe(topic: String, callBack: (String, String)->Unit, executor: Executor)

    fun publishUnsaved(type: String, id: Int)
    fun publishSaved(type: String, id: Int)
    fun subscribeForSelectedEditor(editorType: String, callBack: (Int)->Unit)
    fun subscribeForUpdates(editorType: String, callBack: (id: Int, msg: String)->Unit)

}