package view.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import controller.WorkbenchModuleController
import model.state.WorkbenchModuleState
import util.vertical

/**
 * Component which shows Tab headers for all modules in the given controller
 */
@Composable
internal fun WorkbenchTabRow(controller: WorkbenchModuleController) {
    if (controller.displayType.orientation.toInt() != 0) {
        Box {
            VerticalWorkbenchTabRow(controller)
        }
    } else {
        Box {
           HorizontalWorkbenchTabRow(controller)
        }
    }
}

/**
 * Component which shows the currently selected Module of the given controller
 */
@Composable
internal fun WorkbenchTabBody(controller: WorkbenchModuleController) {
    Box {
        controller.getSelectedModule().value?.content()
    }
}

@Composable
private fun HorizontalWorkbenchTabRow(controller: WorkbenchModuleController) {
    val scrollState = rememberLazyListState()
    ScrollToSelected(controller, scrollState)
    Column {
        LazyRow(state = scrollState) {
            WorkbenchTabs(controller)
        }
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun VerticalWorkbenchTabRow(controller: WorkbenchModuleController) {
    val scrollState = rememberLazyListState()
    ScrollToSelected(controller, scrollState)
    Row {
        LazyColumn(state = scrollState) {
            WorkbenchTabs(controller)
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterVertically),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun ScrollToSelected(controller: WorkbenchModuleController, state: LazyListState){
    LaunchedEffect(controller.getModulesFiltered().size) {
        state.scrollToItem(controller.getScrollToIndex())
    }
}

private fun LazyListScope.WorkbenchTabs(controller: WorkbenchModuleController) {
    items(controller.getModulesFiltered()){ item ->
        WorkbenchTab(
            tab = item,
            controller = controller,
            selected = controller.isModuleSelected(item),
            onClick = { controller.moduleSelectorPressed(item) },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun WorkbenchTab(tab: WorkbenchModuleState<*>, controller: WorkbenchModuleController, selected: Boolean, onClick: ()->Unit) {
    val backgroundColor = if (selected) MaterialTheme.colors.background else MaterialTheme.colors.surface
    val contentColor = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface

    var writerModifier: Modifier = if (controller.displayType.orientation.toInt() != 0) {
        Modifier.vertical(controller.displayType.orientation).clickable {onClick()}
    } else{
        Modifier.clickable {onClick()}
    }

    writerModifier = writerModifier
        .background(color = backgroundColor)
        .drawBehind {
            if (selected) {
                val height = size.height
                val width = size.width
                val strokeWidth = 10f
                val ypos = height - (strokeWidth/2)
                drawLine(
                    start = Offset(x = 0f, y = ypos),
                    end = Offset(x = width, y = ypos),
                    color = contentColor,
                    strokeWidth = strokeWidth
                )
            }
        }

    if(tab.isPreview) {
        writerModifier = writerModifier.background(color = Color(0f,0f,1f,0.3f))
        WorkbenchTab(writerModifier, tab)
    } else {
        DragTarget(module = tab) {
            ContextMenuArea(items = {
                listOf(
                    ContextMenuItem("Open in Window") { controller.convertToWindow(tab) },
                )
            }) {
                WorkbenchTab(writerModifier, tab)
            }
        }
    }
}

@Composable
private fun WorkbenchTab(
    writerModifier: Modifier,
    tab: WorkbenchModuleState<*>,
) {
    Row(
        modifier = writerModifier,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Text(
            text = tab.getTitle(),
            overflow = TextOverflow.Visible,
            maxLines = 1,
            modifier = Modifier.padding(15.dp, 0.dp)
        )
        IconButton(onClick = tab::onClose) {
            Icon(Icons.Filled.Close, "close")
        }
    }
}
