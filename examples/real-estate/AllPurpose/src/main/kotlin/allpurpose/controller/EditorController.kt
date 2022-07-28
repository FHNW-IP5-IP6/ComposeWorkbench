package allpurpose.controller

import java.text.DecimalFormatSymbols
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import allpurpose.data.CrudRepository
import allpurpose.model.Attribute
import allpurpose.model.EditorState
import allpurpose.model.UndoState
import allpurpose.data.Identifiable

abstract class EditorController<D : Identifiable, A: Action, S: EditorState<D>>(initialState: S, protected val repo : CrudRepository<D>) {

    //that's the only mutableState we need
    var editorState by mutableStateOf(initialState, policy = neverEqualPolicy())
    private set

    private val actionChannel = Channel<A>(UNLIMITED)

    private val defaultScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val uiScope      = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    protected val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    protected val ch = Locale("de", "CH")
    protected val chGroupingSeparator = DecimalFormatSymbols(ch).groupingSeparator

    protected val matchAllRegex  = Regex(pattern = """^.*$""")
    protected val asciiTextRegex = Regex(pattern = """[\s\S]*""")
    protected val zipCodeRegex   = Regex(pattern = """^[1-9](\d){3}$""")
    protected val yearRegex      = Regex(pattern = """^(19|20)(\d){2}$""")
    protected val intCHRegex     = Regex(pattern = """^[+-]{0,1}[\d$chGroupingSeparator]{1,11}$""")

    private val undoController = UndoController<S>()

    private val undoStackScheduler = Scheduler(delayInMillis = 200L)

    //this state will be put on Undo-Stack as 'beforeAction'
    private var beforeActionState = editorState

    init {
        uiScope.launch {
            while (true) {
                val action = actionChannel.receive() //wait for next Action

                val deferredNewState = defaultScope.async {
                    executeAction(action, editorState)
                }

                val newState = deferredNewState.await()

                if (null != newState) {
                    editorState = newState
                    if(action.undoable){
                        scheduleUndoStackUpdate(newState)
                    }
                }
            }
        }
    }

    /**
     * Just sends 'action' to 'actionChannel'.
     *
     * UI cannot freeze. It's ready to trigger the next action.
     */
    fun triggerAction(action: A) {
        defaultScope.launch {
            actionChannel.send(action)
        }
    }

    /**
     * @return the new editorState or null if action hasn't changed the state (like 'save')
     */
    protected abstract fun executeAction(action: A, currentState: S): S?

    protected open fun sendUpdateNotifications(oldData: D, newData: D, someDataChanged: Boolean) = Unit

    protected fun save(state: S): S {
        val data = state.data
        ioScope.launch {
            repo.update(data)
        }

        @Suppress("UNCHECKED_CAST")
        return state.duplicate(savedData = data) as S
    }

    protected open fun delete(data: D): S? {
        ioScope.launch {
            repo.delete(data.id)
        }

        return null
    }

    protected fun undo() : S? {
        val oldState = undoController.undo()
        if(null != oldState){
            setNewState(updateUndoAndSavedState(state = oldState,
                                         newUndoState = undoController.undoState,
                                             oldState = editorState))
        }

        return null
    }

    protected fun redo() : S? {
        val newState = undoController.redo()
        if(null != newState){
            setNewState(updateUndoAndSavedState(state = newState,
                                         newUndoState = undoController.undoState,
                                             oldState = editorState))
        }

        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateUndoAndSavedState(state: S, newUndoState: UndoState, oldState: S) : S =
        state.duplicate(undoState = newUndoState, savedData = oldState.savedData) as S

    /**
     *  to enable a "bulk-undo" we have to wait until there is no new action triggered
     */
    private fun scheduleUndoStackUpdate(newState: S) = undoStackScheduler.scheduleTask {
        undoController.pushOnUndoStack(beforeActionState, newState)
        setNewState(updateUndoAndSavedState(state = editorState,
                                     newUndoState = undoController.undoState,
                                         oldState = editorState))
    }


    private fun setNewState(newEditorState: S){
        sendUpdateNotifications(beforeActionState.data, newEditorState.data, newEditorState.data != editorState.savedData)

        editorState = newEditorState
        beforeActionState = newEditorState
    }


    //some convenient extension functions

    protected fun Attribute<String>.copyString(valueAsText: String)  =
        copy(valueAsText = valueAsText,
                 checker = matchAllRegex,
               converter = { it },
               formatter = { it })


    protected fun<T> Attribute<T>.copy(valueAsText: String,
                                       checker: Regex = matchAllRegex,
                                       converter: (String) -> T,
                                       formatter: (T) -> String = { it.toString() }) =
        when {
            checker.matches(valueAsText) -> {
                val newValue = converter(valueAsText)
                this.copy(value = newValue, valueAsText = formatter(newValue), isValid = true)
            }
            else -> {
                this.copy(valueAsText = valueAsText, isValid = false)
            }
        }

    protected fun Number?.format(pattern: String, nullFormat : String = "?"): String =
        if (null == this) nullFormat else pattern.format(ch, this)


}