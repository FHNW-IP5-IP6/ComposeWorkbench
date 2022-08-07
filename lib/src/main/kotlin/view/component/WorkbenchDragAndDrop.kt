package view.component

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import controller.Action
import controller.DragAndDropAction
import controller.WorkbenchAction
import model.data.TabRowKey
import model.state.WorkbenchDragState
import model.state.WorkbenchInformationState
import model.state.WorkbenchModuleState
import model.state.WorkbenchWindowState

/**
 * Window in which a drag animation is visible
 * Modules which are dragged outside this Container will be opened as a new window
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DragAndDropWindow(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
    tabRowKey: TabRowKey,
    onCloseRequest: () -> Unit,
    windowScope: @Composable FrameWindowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    Window(
        onCloseRequest = onCloseRequest,
        title = informationState.appTitle,
        state = tabRowKey.windowState.windowState
    ) {
        val density = LocalDensity.current
        Box(modifier = Modifier.fillMaxSize().onGloballyPositioned {
            onActionRequired.invoke(WorkbenchAction.SetWindowOffset(tabRowKey.windowState,  tabRowKey.windowState.windowState.size.height - with (density) {it.boundsInWindow().height.toDp()}))
            onActionRequired.invoke(DragAndDropAction.AddDropTarget(tabRowKey, getBounds(true, tabRowKey.windowState, it.boundsInWindow(), density), true))
        }) {
            windowScope()
            content()
        }
    }
}

/**
 * Target to drop a dragged module
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DropTarget(
    onActionRequired: (Action) -> Unit,
    tabRowKey: TabRowKey,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)
) {
    val density = LocalDensity.current
    Box(
        modifier = modifier.onGloballyPositioned {
            onActionRequired.invoke(DragAndDropAction.AddDropTarget(tabRowKey, getBounds(false, tabRowKey.windowState, it.boundsInWindow(), density), false))
        }
    ) {
        content()
    }
}

/**
 * Module that can be dragged. Must be inside a DragAndDropContainer!
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DragTarget(
    onActionRequired: (Action) -> Unit,
    modifier: Modifier = Modifier,
    module: WorkbenchModuleState<*>,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier
        .onPointerEvent(PointerEventType.Move) {
            /*
                Get the absolute mouse position. The PointerInputScope.detectDragGesture is only providing an offset
                which is not helpful in globally positioning tha animation window
             */
            val awtEvent = it.awtEventOrNull
            if(awtEvent!= null && it.buttons.isPrimaryPressed) {
                onActionRequired.invoke(
                    DragAndDropAction.SetPosition(
                        DpOffset(
                            awtEvent.xOnScreen.dp,
                            awtEvent.yOnScreen.dp
                        )
                    )
                )
            }
        }
        .pointerInput(key1 = module) {
            detectDragGestures(onDragStart = {
                onActionRequired.invoke(DragAndDropAction.StartDragging(module))
            }, onDrag = { change, _ ->
                change.consumeAllChanges()
            }, onDragEnd = {
                onActionRequired.invoke(WorkbenchAction.DropDraggedModule())
            }, onDragCancel = {
                onActionRequired.invoke(DragAndDropAction.Reset())
            })
        }) {
        content()
    }
}

private fun getBounds(
    isWindow: Boolean = false,
    windowState: WorkbenchWindowState,
    relativeToWindow: Rect,
    density: Density
): Rect {
    with(density) {
        var top = windowState.windowState.position.y + relativeToWindow.top.toDp()
        if (!isWindow) {
            top += windowState.windowHeaderOffset
        }
        val left = windowState.windowState.position.x + relativeToWindow.left.toDp()
        val right = left + relativeToWindow.width.toDp()
        var bottom = top + relativeToWindow.height.toDp()
        if (isWindow) {
            bottom += windowState.windowHeaderOffset
        }
        return if(!isWindow){
            Rect(left = left.value - 10, top = top.value - 10, right = right.value + 10, bottom = bottom.value + 10)
        }else {
            Rect(left = left.value, top = top.value, right = right.value, bottom = bottom.value)
        }
    }
}

@Composable
internal fun WorkbenchDragAnimation(
    dragState: WorkbenchDragState,
) {
    if (dragState.isDragging) {
        Window(
            onCloseRequest = {},
            transparent = false,
            resizable = false,
            undecorated = true,
            state = dragState.dragWindowState
        ) {
            Box {
                dragState.module?.content()
            }
        }
    }
}