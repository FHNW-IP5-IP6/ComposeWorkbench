package controller

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.state.DropTarget
import model.state.getMainWorkbenchWindowState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkbenchDragControllerTest {

    private var controller = WorkbenchController("appTitle")
    private var sut = WorkbenchDragController(controller)

    @BeforeEach
    fun setup() {
        controller = WorkbenchController("appTitle")
        sut = WorkbenchDragController(controller)
    }

    @Test
    fun initialState() {
        assertFalse { sut.dragState.isDragging }
        assertNull(sut.dragState.module)
        assertEquals(DpOffset.Zero, sut.dragState.positionOnScreen)
    }

    @Test
    fun reset() {
        val editorModule =
            WorkbenchModule(ModuleType.EDITOR, "type", title = { "title" }, loader = { c, m -> "model" }) { _, _ -> }
        controller.registerEditor("type", editorModule)
        val moduleState = controller.requestEditorState<String>("type", 1)

        sut.setDragging(true)
        sut.setModuleState(moduleState)
        sut.setPosition(DpOffset(20.dp, 10.dp))

        assertTrue { sut.dragState.isDragging }
        assertEquals(moduleState, sut.dragState.module)
        assertEquals(DpOffset(20.dp, 10.dp), sut.dragState.positionOnScreen)

        sut.reset()
        assertFalse { sut.dragState.isDragging }
        assertNull(sut.dragState.module)
        assertEquals(DpOffset.Zero, sut.dragState.positionOnScreen)
    }

    @Test
    fun getCurrentReverseDropTarget() {
        val windowState1 = getMainWorkbenchWindowState()
        val windowState2 = getMainWorkbenchWindowState()
        val tabRowKey1 = TabRowKey(DisplayType.WINDOW, ModuleType.BOTH, windowState1)
        val tabRowKey2 = TabRowKey(DisplayType.WINDOW, ModuleType.BOTH, windowState2)
        sut.addReverseDropTarget(tabRowKey1, Rect(0f, 0f, 100f, 100f))
        sut.addReverseDropTarget(tabRowKey2, Rect(50f, 50f, 100f, 100f))
        windowState1.hasFocus = true
        windowState2.hasFocus = false

        sut.setPosition(DpOffset(60.dp, 60.dp))
        assertEquals(windowState1, sut.getCurrentReverseDopTarget()?.tabRowKey?.windowState)

        windowState2.hasFocus = true
        windowState1.hasFocus = false
        assertEquals(windowState2, sut.getCurrentReverseDopTarget()?.tabRowKey?.windowState)
    }

    @Test
    fun isValidDropTarget() {
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER, "type", title = { "title" }) { _, _ -> }
        controller.registerExplorer("type", explorerModule)
        val explorerLeft = controller.requestExplorerState(id = 1, moduleType = "type", explorerModel = "model1", displayType = DisplayType.LEFT)
        val explorerBottom = controller.requestExplorerState(id = 2, moduleType = "type", explorerModel = "model2", displayType = DisplayType.BOTTOM)

        val tabRowKeyBottom = TabRowKey(DisplayType.BOTTOM, ModuleType.EXPLORER, controller.getMainWindow())
        val target = DropTarget(false, tabRowKeyBottom, Rect(0f, 50f, 100f, 100f))

        sut.setModuleState(explorerLeft)
        assertTrue {  sut.isValidDropTarget(target.tabRowKey) }

        sut.setModuleState(explorerBottom)
        assertFalse {  sut.isValidDropTarget(target.tabRowKey) }

        sut.setModuleState(null)
        assertFalse {  sut.isValidDropTarget(target.tabRowKey) }
    }

    @Test
    fun dropTargets() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR, "type", title = { "title" }, loader = {c, m ->  "model $c" }) { _, _ -> }
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER, "type", title = { "title" }) { _, _ -> }
        controller.registerExplorer("type", explorerModule)
        controller.registerEditor("type", editorModule)
        controller.registerEditor("type", editorModule)
        val editor1 = controller.requestEditorState<String>("type", 1)
        val editor2 = controller.requestEditorState<String>("type", 2)
        editor2.displayType = DisplayType.TAB2
        val explorer = controller.requestExplorerState(id = 1, moduleType = "type", explorerModel = "model", displayType = DisplayType.LEFT)
        val editorTabRowKey1 = TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, controller.getMainWindow())
        val editorTabRowKey2 = TabRowKey(DisplayType.TAB2, ModuleType.EDITOR, controller.getMainWindow())
        val explorerTabRowKey = TabRowKey(DisplayType.LEFT, ModuleType.EXPLORER, controller.getMainWindow())

        //Only one reverse target (main window)
        sut.addReverseDropTarget(TabRowKey(DisplayType.WINDOW, ModuleType.BOTH, controller.getMainWindow()), Rect(0f, 0f, 100f, 100f))
        assertEquals(controller.getMainWindow() ,sut.getCurrentReverseDopTarget()?.tabRowKey?.windowState)
        assertFalse { sut.isCurrentDropTarget(explorerTabRowKey) }
        assertFalse { sut.isCurrentDropTarget(editorTabRowKey1) }
        assertFalse { sut.isCurrentDropTarget(editorTabRowKey2) }

        sut.setModuleState(editor1)
        sut.addDropTarget(editorTabRowKey1, Rect(0f,0f,10f,10f))
        sut.addDropTarget(editorTabRowKey2, Rect(20f,20f,30f,30f))
        sut.addDropTarget(explorerTabRowKey, Rect(50f,50f,100f,100f))
        assertEquals(4, sut.dragState.dropTargets.size)

        //one drop target is valid and position matches bounds
        sut.setPosition(DpOffset(25.dp, 25.dp))
        assertFalse { sut.isCurrentDropTarget(explorerTabRowKey) }
        assertFalse { sut.isCurrentDropTarget(editorTabRowKey1) }
        assertTrue { sut.isCurrentDropTarget(editorTabRowKey2) }
        assertTrue { sut.isValidDropTarget(editorTabRowKey2) }

        sut.setModuleState(explorer)

        //drop target is no longer valid, position matches bounds
        sut.setPosition(DpOffset(25.dp, 25.dp))
        assertFalse { sut.isCurrentDropTarget(explorerTabRowKey) }
        assertFalse { sut.isCurrentDropTarget(editorTabRowKey1) }
        assertTrue { sut.isCurrentDropTarget(editorTabRowKey2) }
        assertFalse { sut.isValidDropTarget(editorTabRowKey2) }
    }
}