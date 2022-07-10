package view.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import controller.WorkbenchController
import controller.WorkbenchDisplayController
import model.data.enums.OnCloseResponse
import model.state.WorkbenchModuleState
import util.vertical

/**
 * Component combining tab headers and currently selected Module of the given controller
 */
@Composable
internal fun TabSpace(displayController: WorkbenchDisplayController, controller: WorkbenchController){
    Column {
        WorkbenchTabRow(displayController, controller)
        WorkbenchTabBody(displayController, controller)
    }
}

/**
 * Component which shows Tab headers for all modules in the given controller
 */
@Composable
internal fun WorkbenchTabRow(displayController: WorkbenchDisplayController, controller: WorkbenchController) {
    if (displayController.displayType.orientation.toInt() != 0) {
        Box {
            VerticalWorkbenchTabRow(displayController, controller)
        }
    } else {
        Box {
           HorizontalWorkbenchTabRow(displayController, controller)
        }
    }
}

/**
 * Component which shows the currently selected Module of the given controller
 */
@Composable
internal fun WorkbenchTabBody(displayController: WorkbenchDisplayController, controller: WorkbenchController) {
    BoxWithConstraints {
        val contentSize = displayController.getContentDimension(DpSize(maxWidth, maxHeight))
        Column(modifier = Modifier.size(maxWidth, maxHeight)) {
            //The content is strictly sized, because a proper sizing and layout is not guaranteed by the users
            Box(modifier = Modifier.size(contentSize).fillMaxWidth()
            ) {
                displayController.getSelectedModule()?.content()
            }
            WorkbenchEditorSelector(controller = controller, displayController = displayController)
        }
    }
}


@Composable
private fun HorizontalWorkbenchTabRow(displayController: WorkbenchDisplayController, controller: WorkbenchController) {
    val scrollState = rememberLazyListState()
    ScrollToSelected(displayController, scrollState)
    Column {
        LazyRow(state = scrollState) {
            WorkbenchTabs(displayController, controller)
        }
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun VerticalWorkbenchTabRow(displayController: WorkbenchDisplayController, controller: WorkbenchController) {
    val scrollState = rememberLazyListState()
    ScrollToSelected(displayController, scrollState)
    Row {
        LazyColumn(state = scrollState) {
            WorkbenchTabs(displayController, controller)
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterVertically),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun ScrollToSelected(controller: WorkbenchDisplayController, state: LazyListState){
    LaunchedEffect(controller.getModulesFiltered().size) {
        state.scrollToItem(controller.getScrollToIndex())
    }
}

private fun LazyListScope.WorkbenchTabs(displayController: WorkbenchDisplayController, controller: WorkbenchController) {
    preview(displayController)
    items(displayController.getModulesFiltered()){ item ->
        WorkbenchTab(
            moduleState = item,
            controller = controller,
            displayController = displayController,
            selected = displayController.isModuleSelected(item),
            onClick = { displayController.moduleStateSelectorPressed(item) },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.preview(controller: WorkbenchDisplayController) {
    stickyHeader {
        if(controller.previewState.hasPreview()){
            val writerModifier = getTabModifier(controller, true) {}
            WorkbenchTab(writerModifier, controller.previewState.previewTitle!!) {}
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkbenchTab(moduleState: WorkbenchModuleState<*>, controller: WorkbenchController, displayController: WorkbenchDisplayController, selected: Boolean, onClick: ()->Unit) {
    val writerModifier = getTabModifier(displayController, selected, onClick)
    var displayOnSave = remember { mutableStateOf(false) }

    DragTarget(module = moduleState, controller = displayController) {
        ContextMenuArea(items = {
            listOf(
                ContextMenuItem("Open in Window") { controller.moduleToWindow(moduleState) },
            )
        }) {
            WorkbenchTab(writerModifier, moduleState.getTitle()) {
                if (controller.isUnsaved(moduleState)) {
                    displayOnSave.value = true
                } else {
                    moduleState.onClose()
                }
            }
        }
    }

    if (displayOnSave.value) {
        WorkbenchPopupSave({resp ->
            if (resp == OnCloseResponse.DISCARD) {
                moduleState.onClose()
            } else if (resp == OnCloseResponse.SAVE) {
                if (moduleState.onSave()) {
                    moduleState.onClose()
                }
            }
            displayOnSave.value = false
        }, false)
    }
}

@Composable
private fun WorkbenchTab(
    writerModifier: Modifier,
    title: String,
    onClose: () -> Unit
) {
    Row(
        modifier = writerModifier,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Text(
            text = title,
            overflow = TextOverflow.Visible,
            maxLines = 1,
            modifier = Modifier.padding(15.dp, 0.dp)
        )
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, "close")
        }
    }
}

@Composable
private fun getTabModifier(controller: WorkbenchDisplayController, selected: Boolean, onClick: () -> Unit): Modifier{
    val backgroundColor = if (selected) MaterialTheme.colors.background else MaterialTheme.colors.surface
    val contentColor = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface

    val result: Modifier = if (controller.displayType.orientation.toInt() != 0) {
        Modifier.vertical(controller.displayType.orientation).clickable {onClick()}
    } else{
        Modifier.clickable {onClick()}
    }
    return result
        .background(color = backgroundColor)
        .drawBehind {
            if (selected) {
                val height = size.height
                val width = size.width
                val strokeWidth = 10f
                val yPos = height - (strokeWidth/2)
                drawLine(
                    start = Offset(x = 0f, y = yPos),
                    end = Offset(x = width, y = yPos),
                    color = contentColor,
                    strokeWidth = strokeWidth
                )
            }
        }
}
