package view.conponent

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import model.WorkbenchModel
import model.state.DragState
import model.state.WorkbenchModuleState

@Composable
internal fun Draggable(model: WorkbenchModel, content: @Composable () -> Unit){
    Box(modifier = Modifier.fillMaxSize())
    {
        content()
        DragAnimation(model)
    }
}

@Composable
internal fun DropTarget(model: WorkbenchModel, moduleReceiver: (WorkbenchModuleState<*>) -> Unit,
                        content: @Composable (BoxScope.(isActive: Boolean) -> Unit)) {
    with(model.dragState){
        val offset = dragPosition + dragOffset
        var isCurrentDropTarget by remember { mutableStateOf(false) }

        //TODO: add border color to theme
        Box(modifier = Modifier.onGloballyPositioned {
                it.boundsInWindow().let { rect ->
                    isCurrentDropTarget = rect.contains(offset)
                }
            }.then(if (isCurrentDropTarget) Modifier.border(width = 2.dp, color = Color(0f,0f,1f,0.2f), shape = RoundedCornerShape(5.dp)) else Modifier)
        ) {
            if (isCurrentDropTarget && !model.dragState.isDragging && module != null) {
                moduleReceiver(module!!)
                model.dragState = DragState()
            }
            content(isCurrentDropTarget)
        }
    }
}

@Composable
internal fun DragTarget(model: WorkbenchModel, module: WorkbenchModuleState<*>, content: @Composable BoxScope.() -> Unit) {
    var dragTargetPosition by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = Modifier
        .onGloballyPositioned {
            dragTargetPosition = it.localToWindow(Offset.Zero)

        }
        .pointerInput(key1 = module.title){
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
            Box(modifier = Modifier.fillMaxSize()
                .graphicsLayer {
                    val offset = (dragPosition + dragOffset)
                    scaleX = 0.5f
                    scaleY = 0.5f
                    alpha = if (dragAnimationSize == IntSize.Zero) 0f else .9f
                    translationX = offset.x.minus(dragAnimationSize.width / 2)
                    translationY = offset.y.minus(dragAnimationSize.height / 2)
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
