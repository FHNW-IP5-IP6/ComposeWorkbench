package controller

import Workbench
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.state.DropTarget
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkbenchDragTest {

    private var wb = Workbench()
    private var sut = wb.getController()

    @BeforeEach
    fun setup() {
        wb = Workbench()
        sut = wb.getController()
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
            WorkbenchModule(ModuleType.EDITOR, "type", title = { "title" }, loader = { _, _ -> "model" }) { }
        sut.triggerAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.triggerAction(WorkbenchActionSync.RequestEditorState("type", 1))
        val moduleState = sut.informationState.modules.find { it.module.modelType == "type" && it.dataId == 1 }!!

        sut.triggerAction(DragAndDropAction.StartDragging(moduleState))
        sut.triggerAction(DragAndDropAction.SetPosition(DpOffset(20.dp, 10.dp)))

        assertTrue { sut.dragState.isDragging }
        assertEquals(moduleState, sut.dragState.module)
        assertEquals(DpOffset(20.dp, 10.dp), sut.dragState.positionOnScreen)

        sut.triggerAction(DragAndDropAction.Reset())
        assertFalse { sut.dragState.isDragging }
        assertNull(sut.dragState.module)
        assertEquals(DpOffset.Unspecified, sut.dragState.positionOnScreen)
    }

    @Test
    fun getCurrentReverseDropTarget() {
        val windowState1 = sut.informationState.mainWindow
        val windowState2 = sut.informationState.mainWindow
        val tabRowKey1 = TabRowKey(DisplayType.WINDOW, ModuleType.BOTH, windowState1)
        val tabRowKey2 = TabRowKey(DisplayType.WINDOW, ModuleType.BOTH, windowState2)
        sut.triggerAction(DragAndDropAction.AddDropTarget(tabRowKey1, Rect(0f, 0f, 100f, 100f), true))
        sut.triggerAction(DragAndDropAction.AddDropTarget(tabRowKey2, Rect(50f, 50f, 100f, 100f), true))
        windowState1.hasFocus = true
        windowState2.hasFocus = false

        sut.triggerAction(DragAndDropAction.SetPosition(DpOffset(60.dp, 60.dp)))
        assertEquals(windowState1, sut.dragState.getCurrentReverseDopTarget()?.tabRowKey?.windowState)

        windowState2.hasFocus = true
        windowState1.hasFocus = false
        assertEquals(windowState2, sut.dragState.getCurrentReverseDopTarget()?.tabRowKey?.windowState)
    }

    @Test
    fun isValidDropTarget() {
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER, "type", title = { "title" }) { }
        sut.triggerAction(WorkbenchActionSync.RegisterExplorer("type", explorerModule))

        val explorerLeft = WorkbenchModuleState(
            module = explorerModule,
            controller = "model1",
            displayType = DisplayType.LEFT,
            id = 1,
            window = sut.informationState.mainWindow
        )
        val explorerBottom = WorkbenchModuleState(
            module = explorerModule,
            controller = "model2",
            displayType = DisplayType.BOTTOM,
            id = 2,
            window = sut.informationState.mainWindow
        )
        sut.triggerAction(WorkbenchActionSync.RequestExplorerState(explorerLeft))
        sut.triggerAction(WorkbenchActionSync.RequestExplorerState(explorerBottom))

        val tabRowKeyBottom = TabRowKey(DisplayType.BOTTOM, ModuleType.EXPLORER, sut.informationState.mainWindow)
        val target = DropTarget(false, tabRowKeyBottom, Rect(0f, 50f, 100f, 100f))

        sut.triggerAction(DragAndDropAction.StartDragging(explorerLeft))
        assertTrue { sut.dragState.isValidDropTarget(target.tabRowKey, sut.informationState) }

        sut.triggerAction(DragAndDropAction.StartDragging(explorerBottom))
        assertFalse { sut.dragState.isValidDropTarget(target.tabRowKey, sut.informationState) }

        sut.triggerAction(DragAndDropAction.Reset())
        assertFalse { sut.dragState.isValidDropTarget(target.tabRowKey, sut.informationState) }
    }

    @Test
    fun dropTargets() {
        val editorModule =
            WorkbenchModule(ModuleType.EDITOR, "type", title = { "title" }, loader = { id, _ -> "model $id" }) { }
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER, "type", title = { "title" }) { }
        sut.triggerAction(WorkbenchActionSync.RegisterExplorer("type", explorerModule))
        sut.triggerAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.triggerAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.triggerAction(WorkbenchActionSync.RequestEditorState("type", 1))
        val editor1 = sut.informationState.modules.find { it.module.modelType == "type" && it.dataId == 1 }!!
        sut.triggerAction(WorkbenchActionSync.RequestEditorState("type", 2))
        val editor2 = sut.informationState.modules.find { it.module.modelType == "type" && it.dataId == 2 }!!
        editor2.displayType = DisplayType.TAB2

        val explorer = WorkbenchModuleState(
            module = explorerModule,
            controller = "model",
            displayType = DisplayType.LEFT,
            id = 1,
            window = sut.informationState.mainWindow
        )
        sut.triggerAction(WorkbenchActionSync.RequestExplorerState(explorer))
        val editorTabRowKey1 = TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, sut.informationState.mainWindow)
        val editorTabRowKey2 = TabRowKey(DisplayType.TAB2, ModuleType.EDITOR, sut.informationState.mainWindow)
        val explorerTabRowKey = TabRowKey(DisplayType.LEFT, ModuleType.EXPLORER, sut.informationState.mainWindow)

        //Only one reverse target (main window)
        sut.triggerAction(
            DragAndDropAction.AddDropTarget(
                TabRowKey(
                    DisplayType.WINDOW,
                    ModuleType.BOTH,
                    sut.informationState.mainWindow
                ), Rect(0f, 0f, 100f, 100f),
                true
            )
        )
        assertEquals(
            sut.informationState.mainWindow,
            sut.dragState.getCurrentReverseDopTarget()?.tabRowKey?.windowState
        )
        assertFalse { sut.dragState.isCurrentDropTarget(explorerTabRowKey) }
        assertFalse { sut.dragState.isCurrentDropTarget(editorTabRowKey1) }
        assertFalse { sut.dragState.isCurrentDropTarget(editorTabRowKey2) }

        sut.triggerAction(DragAndDropAction.StartDragging(editor1))
        sut.triggerAction(DragAndDropAction.AddDropTarget(editorTabRowKey1, Rect(0f, 0f, 10f, 10f), false))
        sut.triggerAction(DragAndDropAction.AddDropTarget(editorTabRowKey2, Rect(20f, 20f, 30f, 30f), false))
        sut.triggerAction(DragAndDropAction.AddDropTarget(explorerTabRowKey, Rect(50f, 50f, 100f, 100f), false))
        assertEquals(4, sut.dragState.dropTargets.size)

        //one drop target is valid and position matches bounds
        sut.triggerAction(DragAndDropAction.SetPosition(DpOffset(25.dp, 25.dp)))
        assertFalse { sut.dragState.isCurrentDropTarget(explorerTabRowKey) }
        assertFalse { sut.dragState.isCurrentDropTarget(editorTabRowKey1) }
        assertTrue { sut.dragState.isCurrentDropTarget(editorTabRowKey2) }
        assertTrue { sut.dragState.isValidDropTarget(editorTabRowKey2, sut.informationState) }

        sut.triggerAction(DragAndDropAction.StartDragging(explorer))

        //drop target is no longer valid, position matches bounds
        sut.triggerAction(DragAndDropAction.SetPosition(DpOffset(25.dp, 25.dp)))
        assertFalse { sut.dragState.isCurrentDropTarget(explorerTabRowKey) }
        assertFalse { sut.dragState.isCurrentDropTarget(editorTabRowKey1) }
        assertTrue { sut.dragState.isCurrentDropTarget(editorTabRowKey2) }
        assertFalse { sut.dragState.isValidDropTarget(editorTabRowKey2, sut.informationState) }
    }
}