package controller

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.state.DropTarget
import model.state.WorkbenchWindowState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkbenchDragControllerTest {

    private var sut = WorkbenchDragController()
    private var controller = WorkbenchController("appTitle")

    @BeforeEach
    fun setup() {
        sut = WorkbenchDragController()
        controller = WorkbenchController("appTitle")
    }

    @Test
    fun initialState() {
        assertFalse { sut.isDragging() }
        assertNull(sut.getModuleState())
        assertEquals(DpOffset.Zero, sut.getPosition())
    }

    @Test
    fun reset() {
        val editorModule =
            WorkbenchModule(ModuleType.EDITOR, "type", title = { "title" }, loader = { "model" }) { _, _ -> }
        controller.registerEditor("type", editorModule)
        val moduleState = controller.requestEditorState<String>("type", 1)

        sut.setDragging(true)
        sut.setModuleState(moduleState)
        sut.setPosition(DpOffset(20.dp, 10.dp))

        assertTrue { sut.isDragging() }
        assertEquals(moduleState, sut.getModuleState())
        assertEquals(DpOffset(20.dp, 10.dp), sut.getPosition())

        sut.reset()
        assertFalse { sut.isDragging() }
        assertNull(sut.getModuleState())
        assertEquals(DpOffset.Zero, sut.getPosition())
    }

    @Test
    fun getCurrentReverseDropTarget() {
        val windowState1 = WorkbenchWindowState(position = WindowPosition(0.dp, 0.dp))
        val windowState2 = WorkbenchWindowState(position = WindowPosition(0.dp, 0.dp))
        sut.addReverseDropTarget(windowState1, Rect(0f, 0f, 100f, 100f))
        sut.addReverseDropTarget(windowState2, Rect(50f, 50f, 100f, 100f))

        sut.setPosition(DpOffset(60.dp, 60.dp))
        assertEquals(windowState1, sut.getCurrentReverseDopTarget()?.windowState)

        windowState2.hasFocus = true
        assertEquals(windowState2, sut.getCurrentReverseDopTarget()?.windowState)
    }

    @Test
    fun isValidDropTarget() {
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER, "type", title = { "title" }) { _, _ -> }
        controller.registerExplorer("type", explorerModule)
        val explorerLeft = controller.requestExplorerState(id = 1, moduleType = "type", explorerModel = "model1", displayType = DisplayType.LEFT)
        val explorerBottom = controller.requestExplorerState(id = 2, moduleType = "type", explorerModel = "model2", displayType = DisplayType.BOTTOM)

        val displayControllerBottom = controller.getDisplayController(DisplayType.BOTTOM, ModuleType.EXPLORER, true)
        val target = DropTarget(displayControllerBottom, Rect(0f, 50f, 100f, 100f))

        sut.setModuleState(explorerLeft)
        assertTrue {  sut.isValidDropTarget(target) }

        sut.setModuleState(explorerBottom)
        assertFalse {  sut.isValidDropTarget(target) }

        sut.setModuleState(null)
        assertFalse {  sut.isValidDropTarget(target) }
    }

    @Test
    fun dropTargets() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR, "type", title = { "title" }, loader = { "model $it" }) { _, _ -> }
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER, "type", title = { "title" }) { _, _ -> }
        controller.registerExplorer("type", explorerModule)
        controller.registerEditor("type", editorModule)
        controller.registerEditor("type", editorModule)
        val editor1 = controller.requestEditorState<String>("type", 1)
        val editor2 = controller.requestEditorState<String>("type", 2)
        editor2.displayType = DisplayType.TAB2
        val explorer = controller.requestExplorerState(id = 1, moduleType = "type", explorerModel = "model", displayType = DisplayType.LEFT)
        val editorController1 = controller.getDisplayController(DisplayType.TAB1, ModuleType.EDITOR)
        val editorController2 = controller.getDisplayController(DisplayType.TAB2, ModuleType.EDITOR)
        val explorerController = controller.getDisplayController(DisplayType.LEFT, ModuleType.EXPLORER, true)

        //Only one reverse target (main window)
        sut.addReverseDropTarget(windowState = controller.getMainWindow(), Rect(0f, 0f, 100f, 100f))
        assertEquals(controller.getMainWindow() ,sut.getCurrentReverseDopTarget()?.windowState)
        assertFalse { sut.isCurrentDropTarget(explorerController) }
        assertFalse { sut.isCurrentDropTarget(editorController1) }
        assertFalse { sut.isCurrentDropTarget(editorController2) }

        sut.setModuleState(editor1)
        sut.addDropTarget(editorController1, Rect(0f,0f,10f,10f))
        sut.addDropTarget(editorController2, Rect(20f,20f,30f,30f))
        sut.addDropTarget(explorerController, Rect(50f,50f,100f,100f))

        //one drop target is valid and position matches bounds
        sut.setPosition(DpOffset(25.dp, 25.dp))
        assertFalse { sut.isCurrentDropTarget(explorerController) }
        assertFalse { sut.isCurrentDropTarget(editorController1) }
        assertTrue { sut.isCurrentDropTarget(editorController2) }

        sut.setModuleState(explorer)

        //drop target is no longer valid, position matches bounds
        sut.setPosition(DpOffset(25.dp, 25.dp))
        assertFalse { sut.isCurrentDropTarget(explorerController) }
        assertFalse { sut.isCurrentDropTarget(editorController1) }
        assertFalse { sut.isCurrentDropTarget(editorController2) }
    }
}