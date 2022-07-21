package realestateeditor.model


import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import allpurpose.model.EditorState
import allpurpose.model.UndoState
import realestateeditor.data.RealEstateData

data class RealEstateEditorState(override val data: RealEstateData,
                                 override val savedData:  RealEstateData = data,
                                 override val undoState: UndoState = UndoState(),

                                 val windowState: WindowState =  WindowState(width = 800.dp,
                                                                            height = 600.dp,
                                                                          position = WindowPosition(Alignment.Center)
                       )
) : EditorState<RealEstateData> {

    override fun duplicate(
        data: RealEstateData,
        savedData: RealEstateData,
        undoState: UndoState
    ): EditorState<RealEstateData>  =
        copy(data = data, savedData = savedData, undoState = undoState)

}

