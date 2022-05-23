package view.conponent

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import model.WorkbenchModel
import model.data.ModuleType
import model.state.DisplayType
import model.state.DragState
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
    DropTarget(reverse = true, modifier = Modifier.fillMaxSize(), model, ModuleType.EXPLORER, {
        val window = WorkbenchModuleState(it, model::removeTab, DisplayType.WINDOW)
        model.removeTab(it)
        model.addState(window)
    })
    {
        content()
        DragAnimation(model)
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
    reverse: Boolean = false,
    modifier: Modifier = Modifier,
    model: WorkbenchModel,
    acceptedType: ModuleType,
    moduleReceiver: (WorkbenchModuleState<*>) -> Unit,
    content: @Composable (BoxScope.(isActive: Boolean) -> Unit)
){
    with(model.dragState){
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
            val isValidTarget = module != null && getModuleType() == acceptedType
            if ((!reverse && isCurrentDropTarget || reverse && !isCurrentDropTarget) && !model.dragState.isDragging && isValidTarget) {
                moduleReceiver(module!!)
                model.dragState = DragState()
            }
            content(isCurrentDropTarget && isValidTarget)
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
@Composable
internal fun DragTarget(
    modifier: Modifier = Modifier,
    model: WorkbenchModel,
    module: WorkbenchModuleState<*>,
    content: @Composable BoxScope.() -> Unit
){
    var dragTargetPosition by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier
        .onGloballyPositioned {
            dragTargetPosition = it.localToWindow(Offset.Zero)
        }
        .pointerInput(key1 = module.id){
            detectDragGestures(onDragStart = {
                model.dragState = DragState()
                model.dragState.isDragging = true
                model.dragState.dragOffset = Offset.Zero
                model.dragState.dragPosition = dragTargetPosition + it
                model.dragState.module = module
            }, onDrag = { change, dragAmount ->
                change.consumeAllChanges()
                model.dragState.dragOffset += Offset(dragAmount.x, dragAmount.y)
            }, onDragEnd = {
                model.dragState.isDragging = false
                model.dragState.dragOffset = Offset.Zero
                model.dragState.dragPosition = Offset.Zero
            }, onDragCancel = {
                model.dragState = DragState()
            })
        }){
        content()
    }
}

@Composable
private fun DragAnimation(model: WorkbenchModel){
    var dragAnimationSize by remember { mutableStateOf(IntSize.Zero) }

    with(model.dragState){
        if (isDragging) {
            Box(modifier = Modifier
                .graphicsLayer {
                    val offset = (dragPosition + dragOffset)
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
