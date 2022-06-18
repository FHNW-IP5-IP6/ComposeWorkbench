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
import androidx.compose.ui.geometry.Offset
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
import model.WorkbenchModel
import model.state.DragState
import model.state.WindowStateAware
import model.state.WorkbenchModuleState

/**
 * Window in which a drag animation is visible
 * Modules which are dragged outside this Container will be opened as a new window
 *
 * @param model: Workbench model
 * @param currentWindow: Holder of the windows state
 * @param content: Content of the drag and drop container
 * @param onCloseRequest: Callback which is called, when the window is closed
 * @param moduleReceiver: Callback which is called, when module is dropped outside the given window
 * @param windowScope: Configurable scope for this window
 */
@Composable
internal fun DragAndDropWindow(
    model: WorkbenchModel,
    currentWindow: WindowStateAware,
    onCloseRequest: () -> Unit,
    moduleReceiver: (WorkbenchModuleState<*>) -> Unit,
    windowScope: @Composable FrameWindowScope.() -> Unit = {},
    content: @Composable () -> Unit
){
    Window(
        onCloseRequest = onCloseRequest,
        title = model.appTitle,
        state = currentWindow.windowState
    ) {
        with(model.dragState){
            val pos = positionOnScreen
            val density = LocalDensity.current

            Box(modifier = Modifier.fillMaxSize().onGloballyPositioned {
                currentWindow.isDropTarget = getBounds(true, currentWindow, it.boundsInWindow(), density).contains(Offset(pos.x.value, pos.y.value))
                if (isDragging && currentWindow.isDropTarget && parentWindow != currentWindow) {
                    parentWindow = currentWindow
                }
            }) {
                if (!parentWindow.isDropTarget && !isDragging && module != null && parentWindow == currentWindow) {
                    onModuleDropped(module!!)
                    moduleReceiver(module!!)
                    reset()
                }
                windowScope()
                content()

                if(parentWindow == currentWindow){
                    DragAnimation(model.dragState)
                }
            }
        }
    }
}

/**
 * Target to drop a dragged module
 *
 * @param content: content of the drop target
 * @param modifier: Modifier to for the Box surrounding the content
 * @param controller: WorkbenchController responsible for the target
 */
@Composable
internal fun DropTarget(
    controller: WorkbenchController,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)
){
    with(controller.getDragState()) {
        val pos = positionOnScreen
        val density = LocalDensity.current
        var isCurrentDropTarget by remember { mutableStateOf(false) }

        Box(modifier = modifier.onGloballyPositioned {
            isCurrentDropTarget = getBounds(false, controller.getWindow(),  it.boundsInWindow(), density).contains(Offset(pos.x.value, pos.y.value))
        }) {
            val isValidTarget = module != null && getModuleType() == controller.moduleType && !controller.containsModule(module!!)
            // println("isValid ${controller.displayType}, $isValidTarget")
            if (isCurrentDropTarget && isValidTarget){
                controller.previewState.previewTitle = module!!.getTitle()
            } else {
                controller.previewState.previewTitle = null
            }
            if (isCurrentDropTarget && !isDragging && isValidTarget) {
                onModuleDropped(module!!)
                controller.onModuleDroppedIn(module!!)
                reset()
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
 * @param controller: WorkbenchController responsible for the target
 * @param module: ModuleType which can be dragged
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DragTarget(
    modifier: Modifier = Modifier,
    module: WorkbenchModuleState<*>,
    controller: WorkbenchController,
    content: @Composable BoxScope.() -> Unit
){
    with(controller.getDragState()) {
        Box(modifier = modifier
            .onPointerEvent(PointerEventType.Move) {
                if (isDragging) {
                    positionOnScreen = DpOffset(it.awtEvent.xOnScreen.dp, it.awtEvent.yOnScreen.dp)
                }
            }
            .pointerInput(key1 = module.id) {
                detectDragGestures(onDragStart = {
                    reset()
                    isDragging = true
                    controller.getDragState().module = module
                    parentWindow = controller.getWindow()
                    onModuleDropped = controller::onModuleDraggedOut
                }, onDrag = { change, _ ->
                    change.consumeAllChanges()
                }, onDragEnd = {
                    isDragging = false
                }, onDragCancel = {
                    reset()
                })
            }) {
            content()
        }
    }
}

@Composable
private fun DragAnimation(state: DragState){
    var dragAnimationSize by remember { mutableStateOf(IntSize.Zero) }
    with(state){
        if (isDragging) {
            if (parentWindow != null && !parentWindow!!.isDropTarget){
                Window(
                    onCloseRequest = {},
                    transparent = false,
                    resizable = false,
                    undecorated = true,
                    state = WindowState(
                        size =  DpSize(dragAnimationSize.width.dp / 2, dragAnimationSize.height.dp / 2),
                        position = WindowPosition(positionOnScreen.x, positionOnScreen.y)
                    )
                ) {
                    Box() {
                        module?.content()
                    }
                }
            } else {
                val offset = toOffset()
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
                    module?.content()
                }
            }
        }
    }
}

private fun getBounds(isWindow: Boolean = false, windowState: WindowStateAware, relativeToWindow: Rect, density: Density): Rect{
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