package view.component

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEvent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import controller.WorkbenchController
import controller.WorkbenchDragController
import model.data.TabRowKey
import model.state.WorkbenchModuleState
import model.state.WorkbenchWindowState
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

/**
 * Window in which a drag animation is visible
 * Modules which are dragged outside this Container will be opened as a new window
 *
 * @param controller: Workbench Controller
 * @param tabRowKey: Key of this window
 * @param content: Content of the drag and drop container
 * @param onCloseRequest: Callback which is called, when the window is closed
 * @param windowScope: Configurable scope for this window
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DragAndDropWindow(
    controller: WorkbenchController,
    tabRowKey: TabRowKey,
    onCloseRequest: () -> Unit,
    windowScope: @Composable FrameWindowScope.() -> Unit = {},
    content: @Composable () -> Unit
){
    Window(
        onCloseRequest = onCloseRequest,
        title = controller.getAppTitle(),
        state = tabRowKey.windowState.windowState
    ) {
        window.addWindowFocusListener(object : WindowFocusListener {
            override fun windowGainedFocus(e: WindowEvent) {
                tabRowKey.windowState.hasFocus = true
            }
            override fun windowLostFocus(e: WindowEvent) {
                tabRowKey.windowState.hasFocus = false
            }
        })

        with(controller.dragController) {
            val density = LocalDensity.current
            Box(modifier = Modifier.fillMaxSize().onGloballyPositioned {
                addReverseDropTarget(tabRowKey, getBounds(true, tabRowKey.windowState, it.boundsInWindow(), density))
            }) {
                windowScope()
                content()
                DragAnimation(controller.dragController, tabRowKey.windowState)
            }
        }
    }
}

/**
 * Target to drop a dragged module
 *
 * @param controller: Workbench Controller
 * @param content: content of the drop target
 * @param modifier: Modifier to for the Box surrounding the content
 * @param tabRowKey: Key of the Tab Row which this Drop Target belongs to
 */
@Composable
internal fun DropTarget(
    controller: WorkbenchController,
    tabRowKey: TabRowKey,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)
){
    with(controller.dragController) {
        val density = LocalDensity.current

        Box(modifier = modifier.onGloballyPositioned {
            addDropTarget(tabRowKey, getBounds(false, tabRowKey.windowState,  it.boundsInWindow(), density))
        }) {
            if (isCurrentDropTarget(tabRowKey) && isValidDropTarget(tabRowKey)){
                controller.updatePreviewTitle(tabRowKey, dragState.module!!.getTitle())
            } else {
                controller.updatePreviewTitle(tabRowKey, null)
            }
            content()
        }
    }
}

/**
 * Module that can be dragged. Must be inside a DragAndDropContainer!
 *
 * @param content: content of the drag target
 * @param modifier: Modifier to for the Box surrounding the content
 * @param module: ModuleType which can be dragged
 * @param controller: Workbench Controller
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DragTarget(
    modifier: Modifier = Modifier,
    module: WorkbenchModuleState<*>,
    controller: WorkbenchController,
    content: @Composable BoxScope.() -> Unit
){
    with(controller.dragController) {
        Box(modifier = modifier
            .onPointerEvent(PointerEventType.Move) {
                if (dragState.isDragging) {
                    setPosition(DpOffset(it.awtEvent.xOnScreen.dp, it.awtEvent.yOnScreen.dp))
                }
            }
            .pointerInput(key1 = module.id) {
                detectDragGestures(onDragStart = {
                    reset()
                    setDragging(true)
                    setModuleState(module)
                }, onDrag = { change, _ ->
                    change.consumeAllChanges()
                }, onDragEnd = {
                    setDragging(false)
                    dropDraggedModule()
                }, onDragCancel = {
                    reset()
                })
            }) {
            content()
        }
    }
}

@Composable
private fun DragAnimation(controller: WorkbenchDragController, currentWindow: WorkbenchWindowState){
    with(controller){
        val dropTarget = getCurrentReverseDopTarget()
        var dragAnimationSize by remember { mutableStateOf(IntSize.Zero) }

        if (dragState.isDragging) {
            if (dropTarget == null){
                Window(
                    onCloseRequest = {},
                    transparent = false,
                    resizable = false,
                    undecorated = true,
                    state = WindowState(
                        size =  DpSize(dragAnimationSize.width.dp / 2, dragAnimationSize.height.dp / 2),
                        position = WindowPosition(dragState.positionOnScreen.x, dragState.positionOnScreen.y)
                    )
                ) {
                    Box() {
                        dragState.module?.content()
                    }
                }
            } else if (currentWindow == dropTarget.tabRowKey.windowState) {
                val offset = toOffset(dropTarget.tabRowKey.windowState)
                Box(modifier = Modifier
                    .graphicsLayer {
                        scaleX = 0.5f
                        scaleY = 0.5f
                        alpha = if (dragAnimationSize == IntSize.Zero) 0f else .9f
                        translationX = offset.x.minus(dragAnimationSize.width * 0.25f)
                        translationY = offset.y.minus(dragAnimationSize.height * 0.25f)
                    }
                    .onGloballyPositioned {
                        dragAnimationSize = it.size
                    }
                ) {
                    dragState.module?.content()
                }
            }
        }
    }
}

private fun getBounds(isWindow: Boolean = false, windowState: WorkbenchWindowState, relativeToWindow: Rect, density: Density): Rect{
    with(density) {
        if(isWindow) {
            windowState.windowHeaderOffset = windowState.windowState.size.height - relativeToWindow.height.toDp()
        }
        var top = windowState.windowState.position.y + relativeToWindow.top.toDp()
        if (!isWindow){
            top += windowState.windowHeaderOffset
        }
        val left = windowState.windowState.position.x + relativeToWindow.left.toDp()
        val right = left + relativeToWindow.width.toDp()
        var bottom = top + relativeToWindow.height.toDp()
        if (isWindow){
            bottom += windowState.windowHeaderOffset
        }
        return Rect(left = left.value - 10, right = right.value + 10, top = top.value - 10, bottom = bottom.value + 10)
    }
}