package view.conponent

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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import model.state.DragState
import model.state.DragState.getWindowPosition
import model.state.WindowStateAware
import model.state.WorkbenchModuleState

/**
 * Container which holds the drag and drop animation.
 * Modules which are dragged outside this Container will be opened as a new window
 *
 * @param model: workbench model
 * @param content: Content of the drag and drop container
 */
@Composable
internal fun DragAndDropContainer(model: WorkbenchModel, content: @Composable () -> Unit){
    DropTarget(model = model, reverse = true, modifier = Modifier.fillMaxSize(), dropTargetType = DisplayType.WINDOW, moduleReceiver = {
        model.windows += WindowStateAware(position = getWindowPosition(), moduleState = it.toWindow())
    })
    {
        content()
        DragAnimation()
    }
}

/**
 * Target to drop a dragged module
 *
 * @param content: content of the drop target
 * @param reverse: if true the drag target is everything outside the given content
 * @param modifier: Modifier to for the Box surrounding the content
 * @param model: Workbench model, as drag state holder
 * @param acceptedType: ModuleType which can be dropped into this target
 * @param moduleReceiver: callback to handle the dropped module
 */
@Composable
internal fun DropTarget(
    model: WorkbenchModel,
    reverse: Boolean = false,
    modifier: Modifier = Modifier,
    acceptedType: ModuleType? = null,
    dropTargetType: DisplayType,
    moduleReceiver: (WorkbenchModuleState<*>) -> Unit,
    content: @Composable (BoxScope.() -> Unit)
){
    with(DragState){
        val offset = dragPosition + dragOffset
        var isCurrentDropTarget by remember { mutableStateOf(false) }
        Box(modifier = modifier.onGloballyPositioned {
                it.boundsInWindow().let { rect ->
                    val right = if(rect.right - rect.left <= 10) rect.left + 60f else rect.right
                    val top = if(rect.bottom - rect.top <= 10) rect.bottom - 60f else rect.top
                    val bounds = Rect(left = rect.left, top = top, right = right, bottom = rect.bottom)
                    isCurrentDropTarget = bounds.contains(offset)
                }
            }
        ) {
            val isValidTarget = module != null && (acceptedType == null || getModuleType() == acceptedType)
            if (reverse) {
                isWindow = !isCurrentDropTarget
                model.clearPreview() //in the reverse part there is no need for a preview
            } else if (isCurrentDropTarget && isValidTarget && module!!.displayType != dropTargetType){
                model.updatePreviewModule(WorkbenchModuleState(id = model.getNextKey(), state = module!!, displayType = dropTargetType!!))
            }
            if ((!reverse && isCurrentDropTarget || reverse && !isCurrentDropTarget) && !isDragging && isValidTarget) {
                moduleReceiver(module!!)
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
 * @param model: Workbench model, as drag state holder
 * @param module: ModuleType which can be dragged
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DragTarget(
    modifier: Modifier = Modifier,
    module: WorkbenchModuleState<*>,
    content: @Composable BoxScope.() -> Unit
){
    with(DragState) {
        var dragTargetPosition by remember { mutableStateOf(Offset.Zero) }

        Box(modifier = modifier
            .onGloballyPositioned {
                dragTargetPosition = it.localToWindow(Offset.Zero)
            }
            .onPointerEvent(PointerEventType.Move) {
                if (isDragging) {
                    positionOnScreen = IntOffset(it.awtEvent.xOnScreen, it.awtEvent.yOnScreen)
                }
            }
            .pointerInput(key1 = module.id) {
                detectDragGestures(onDragStart = {
                    reset()
                    isDragging = true
                    dragPosition = dragTargetPosition + it
                    DragState.module = module
                }, onDrag = { change, dragAmount ->
                    change.consumeAllChanges()
                    dragOffset += Offset(dragAmount.x, dragAmount.y)
                }, onDragEnd = {
                    isDragging = false
                    dragOffset = Offset.Zero
                    dragPosition = Offset.Zero
                }, onDragCancel = {
                    reset()
                })
            }) {
            content()
        }
    }
}

@Composable
private fun DragAnimation(){
    var dragAnimationSize by remember { mutableStateOf(IntSize.Zero) }
    with(DragState){
        val offset = dragPosition + dragOffset
        if (isDragging) {
            if (isWindow){
                Window(
                    onCloseRequest = {},
                    transparent = false,
                    resizable = false,
                    undecorated = true,
                    state = WindowState(
                        size =  DpSize(dragAnimationSize.width.dp / 2, dragAnimationSize.height.dp / 2),
                        position = WindowPosition(positionOnScreen.x.dp, positionOnScreen.y.dp)
                    )
                ) {
                    Box() {
                        module?.content()
                    }
                }
            } else {
                Box(modifier = Modifier
                    .graphicsLayer {
                        scaleX = 0.5f
                        scaleY = 0.5f
                        alpha = if (dragAnimationSize == IntSize.Zero) 0f else .9f
                        translationX = offset.x.minus(dragAnimationSize.width / 3)
                        translationY = offset.y.minus(dragAnimationSize.height / 3)
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
