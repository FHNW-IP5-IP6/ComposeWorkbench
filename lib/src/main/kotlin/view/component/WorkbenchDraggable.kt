package view.component

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
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
import controller.Action
import controller.DragAndDropAction
import controller.WorkbenchAction
import model.data.TabRowKey
import model.state.WorkbenchDragState
import model.state.WorkbenchInformationState
import model.state.WorkbenchModuleState
import model.state.WorkbenchWindowState
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

/**
 * Window in which a drag animation is visible
 * Modules which are dragged outside this Container will be opened as a new window
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DragAndDropWindow(
    informationState: WorkbenchInformationState,
    dragState: WorkbenchDragState,
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
        window.addWindowFocusListener(object : WindowFocusListener {
            override fun windowGainedFocus(e: WindowEvent) {
                tabRowKey.windowState.hasFocus = true
            }

            override fun windowLostFocus(e: WindowEvent) {
                tabRowKey.windowState.hasFocus = false
            }
        })

        val density = LocalDensity.current
        Box(modifier = Modifier.fillMaxSize().onGloballyPositioned {
            updateTarget(
                onActionRequired, dragState, tabRowKey,
                getBounds(true, tabRowKey.windowState, it.boundsInWindow(), density),
                true
            )
        }) {
            windowScope()
            content()
            DragAnimation(dragState, tabRowKey.windowState)
        }
    }
}

/**
 * Target to drop a dragged module
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DropTarget(
    informationState: WorkbenchInformationState,
    dragState: WorkbenchDragState,
    onActionRequired: (Action) -> Unit,
    tabRowKey: TabRowKey,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)
) {
    val density = LocalDensity.current
    Box(
        modifier = modifier.onGloballyPositioned {
            updateTarget(
                onActionRequired, dragState, tabRowKey,
                getBounds(false, tabRowKey.windowState, it.boundsInWindow(), density),
                false
            )
        }
    ) {
        //TODO: Can this be handled when changing the drag position??
        if (dragState.isCurrentDropTarget(tabRowKey) && dragState.isValidDropTarget(tabRowKey, informationState)) {
            onActionRequired.invoke(WorkbenchAction.UpdatePreviewTitle(tabRowKey, dragState.module!!.getTitle()))
        } else if (informationState.tabRowState[tabRowKey]?.preview != null){
            onActionRequired.invoke(WorkbenchAction.UpdatePreviewTitle(tabRowKey, null))
        }
        content()
    }
}

/**
 * Module that can be dragged. Must be inside a DragAndDropContainer!
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun DragTarget(
    dragState: WorkbenchDragState,
    onActionRequired: (Action) -> Unit,
    modifier: Modifier = Modifier,
    module: WorkbenchModuleState<*>,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier
        .onPointerEvent(PointerEventType.Move) {
            if (dragState.isDragging) {
                val awtEvent = it.awtEventOrNull
                if(awtEvent!= null) {
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

@Composable
private fun DragAnimation(
    dragState: WorkbenchDragState,
    currentWindow: WorkbenchWindowState
) {
    if(dragState.positionOnScreen.isSpecified) {
        val dropTarget = dragState.getCurrentReverseDopTarget()
        var dragAnimationSize by remember { mutableStateOf(IntSize.Zero) }

        if (dragState.isDragging) {
            if (dropTarget == null) {
                Window(
                    onCloseRequest = {},
                    transparent = false,
                    resizable = false,
                    undecorated = true,
                    state = WindowState(
                        size = DpSize(dragAnimationSize.width.dp / 2, dragAnimationSize.height.dp / 2),
                        position = WindowPosition(dragState.positionOnScreen.x, dragState.positionOnScreen.y)
                    )
                ) {
                    Box {
                        dragState.module?.content()
                    }
                }
            } else if (currentWindow == dropTarget.tabRowKey.windowState) {
                val offset = dragState.toOffset(dropTarget.tabRowKey.windowState)
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

private fun getBounds(
    isWindow: Boolean = false,
    windowState: WorkbenchWindowState,
    relativeToWindow: Rect,
    density: Density
): Rect {
    with(density) {
        if (isWindow) {
            windowState.windowHeaderOffset = windowState.windowState.size.height - relativeToWindow.height.toDp()
        }
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

private fun updateTarget(
    onActionRequired: (Action) -> Unit,
    dragState: WorkbenchDragState,
    tabRowKey: TabRowKey,
    bounds: Rect,
    isReverse: Boolean
) {
    val same = dragState.dropTargets.filter { it.tabRowKey == tabRowKey && it.isReverse == isReverse }.find { it.bounds == bounds }
    if (same == null) {
        onActionRequired.invoke(
            DragAndDropAction.AddDropTarget(
                tabRowKey,
                bounds,
                isReverse
            )
        )
    }
}