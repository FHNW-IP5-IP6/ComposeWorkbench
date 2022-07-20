package realestateeditor.allpurpose.model


interface EditorState<D : Any> {
    val data:       D
    val savedData:  D
    val undoState: UndoState
    val isChanged:  Boolean
    get() = data != savedData


    fun duplicate(data: D = this.data,
                  savedData: D = this.savedData,
                  undoState: UndoState = this.undoState) : EditorState<D>
}