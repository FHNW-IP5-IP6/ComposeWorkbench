package allpurpose.controller

import allpurpose.model.UndoState


class UndoController<S> {

    private val undoStack = ArrayDeque<ActionSnapshot<S>>()
    private val redoStack = ArrayDeque<ActionSnapshot<S>>()

    val undoState
    get() = UndoState(undoStack.isNotEmpty(), redoStack.isNotEmpty())


    fun pushOnUndoStack(beforeAction: S, afterAction:S) {
        undoStack.push(ActionSnapshot(beforeAction, afterAction))
        redoStack.clear()
    }

    fun undo() : S? = if(undoState.undoAvailable){
                          val snapshot = undoStack.pop()
                          redoStack.push(snapshot)

                          snapshot.beforeAction
                      } else {
                          null
                      }

    fun redo() : S? = if(undoState.redoAvailable){
                          val snapshot = redoStack.pop()
                          undoStack.push(snapshot)

                          snapshot.afterAction
                      } else {
                          null
                      }


    private fun <S> ArrayDeque<S>.pop() : S = removeFirst()
    private fun <S> ArrayDeque<S>.push(element: S) = addFirst(element)

    private data class ActionSnapshot<S>(val beforeAction: S,
                                         val afterAction: S)
}

