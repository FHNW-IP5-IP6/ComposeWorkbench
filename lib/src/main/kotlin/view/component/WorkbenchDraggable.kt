package view.component

import TAB_ROW_HEIGHT
import TAB_ROW_WIDTH
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEvent
import androidx.compose.ui.focus.onFocusChanged
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
import controller.WorkbenchDisplayController
import model.state.WorkbenchModuleState
import model.state.WorkbenchWindowState

/**
 * Window in which a drag animation is visible
 * Modules which are dragged outside this Container will be opened as a new window
 *
 * @param controller: Workbench Controller
 * @param currentWindow: Holder of the windows state
 * @param content: Content of the drag and drop container
 * @param onCloseRequest: Callback which is called, when the window is closed
 * @param windowScope: Configurable scope for this window
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DragAndDropWindow(
    controller: WorkbenchController,
    currentWindow: WorkbenchWindowState,
    onCloseRequest: () -> Unit,
    windowScope: @Composable FrameWindowScope.() -> Unit = {},
    content: @Composable () -> Unit
){
    Window(
        onCloseRequest = onCloseRequest,
        title = controller.getAppTitle(),
        state = currentWindow.windowState
    ) {
        with(controller.dragController) {
            val density = LocalDensity.current
            Box(modifier = Modifier.onFocusChanged {
                currentWindow.hasFocus = it.hasFocus
            }.fillMaxSize().onGloballyPositioned {
                addReverseDropTarget(currentWindow, getBounds(true, currentWindow, it.boundsInWindow(), density))
            }) {
                windowScope()
                content()
                DragAnimation(controller, currentWindow)
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
 * @param displayController: WorkbenchController responsible for the target
 */
@Composable
internal fun DropTarget(
    controller: WorkbenchController,
    displayController: WorkbenchDisplayController,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)
){
    with(controller.dragController) {
        val density = LocalDensity.current

        Box(modifier = modifier.onGloballyPositioned {
            addDropTarget(displayController, getBounds(false, displayController.windowState,  it.boundsInWindow(), density))
        }) {
            if (isCurrentDropTarget(displayController)){
                displayController.previewState.previewTitle = getModuleState()!!.getTitle()
            } else {
                displayController.previewState.previewTitle = null
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
 * @param displayController: WorkbenchController responsible for the target
 * @param module: ModuleType which can be dragged
 * @param controller: Workbench Controller
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DragTarget(
    modifier: Modifier = Modifier,
    module: WorkbenchModuleState<*>,
    controller: WorkbenchController,
    displayController: WorkbenchDisplayController,
    content: @Composable BoxScope.() -> Unit
){
    with(controller.dragController) {
        Box(modifier = modifier
            .onPointerEvent(PointerEventType.Move) {
                if (isDragging()) {
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
                    dropDraggedModule(controller, displayController)
                }, onDragCancel = {
                    reset()
                })
            }) {
            content()
        }
    }
}

private fun dropDraggedModule(
    controller: WorkbenchController,
    displayController: WorkbenchDisplayController) {
    with(controller.dragController){
        if(getModuleState() != null){
            val reverseDropTarget = getCurrentReverseDopTarget()
            if(reverseDropTarget == null){
                displayController.onModuleDraggedOut(getModuleState() !!)
                controller.moduleToWindow(getModuleState() !!)
            }else {
                val dropTarget = getCurrentDopTarget(reverseDropTarget.windowState)
                if(dropTarget != null && isValidDropTarget(dropTarget)){
                    displayController.onModuleDraggedOut(getModuleState() !!)
                    dropTarget.displayController.onModuleDroppedIn(getModuleState() !!)
                }
            }
        }
        reset()
    }
}

@Composable
private fun DragAnimation(controller: WorkbenchController, currentWindow: WorkbenchWindowState){
    with(controller.dragController){
        val dropTarget = getCurrentReverseDopTarget()
        var dragAnimationSize by remember { mutableStateOf(IntSize.Zero) }

        if (isDragging()) {
            if (dropTarget == null){
                Window(
                    onCloseRequest = {},
                    transparent = false,
                    resizable = false,
                    undecorated = true,
                    state = WindowState(
                        size =  DpSize(dragAnimationSize.width.dp / 2, dragAnimationSize.height.dp / 2),
                        position = WindowPosition(getPosition().x, getPosition().y)
                    )
                ) {
                    Box() {
                        getModuleState()?.content()
                    }
                }
            } else if (currentWindow == dropTarget.windowState) {
                val offset = toOffset(dropTarget.windowState)
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
                    getModuleState()?.content()
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
        val right = left + max(relativeToWindow.width.toDp(), TAB_ROW_WIDTH.dp)
        var bottom = top + max(relativeToWindow.height.toDp(), TAB_ROW_HEIGHT.dp)
        if (isWindow){
            bottom += windowState.windowHeaderOffset
        }
        return Rect(left = left.value, right = right.value, top = top.value, bottom = bottom.value)
    }
}