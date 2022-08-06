package model.data
import java.util.concurrent.Executor

sealed interface MqClient {

    fun publish(topic: String, msg: String)
    fun publish(type: String, id: Int, msg: String)
    
    fun subscribe(topic: String, callBack: (String, String)->Unit)
    fun subscribe(topic: String, callBack: (String, String)->Unit, executor: Executor)

    /**
     * Let the workbench know that the entity with the given type and id has changed
     */
    fun publishUnsaved(type: String, id: Int)

    /**
     * Get notified when an editor of the given type is selected
     */
    fun subscribeForSelectedEditor(editorType: String, callBack: (Int)->Unit)

    /**
     * Get notified when an editor of the given type is updated
     */
    //TODO: message can be Saved, or Closed use enum
    fun subscribeForUpdates(editorType: String, callBack: (id: Int, msg: String)->Unit)
}