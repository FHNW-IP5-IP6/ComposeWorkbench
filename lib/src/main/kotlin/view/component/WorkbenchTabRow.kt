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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import controller.WorkbenchController
import model.state.WorkbenchModuleState
import util.vertical

/**
 * Component combining tab headers and currently selected Module of the given controller
 */
@Composable
internal fun TabSpace(controller: WorkbenchController){
    Column {
        WorkbenchTabRow(controller)
        WorkbenchTabBody(controller)
    }
}

/**
 * Component which shows Tab headers for all modules in the given controller
 */
@Composable
internal fun WorkbenchTabRow(controller: WorkbenchController) {
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
internal fun WorkbenchTabBody(controller: WorkbenchController) {
    BoxWithConstraints {
        val contentSize = controller.getContentDimension(DpSize(maxWidth, maxHeight))
        Column(modifier = Modifier.size(maxWidth, maxHeight).clipToBounds()) {
            //The content is strictly sized, because a proper sizing and layout is not guaranteed by the users
            Box(modifier = Modifier.size(contentSize).fillMaxWidth()) {
                controller.getSelectedModule()?.content()
            }
            WorkbenchEditorSelector(model = controller.model, state = controller.getSelectedModule())
        }
    }
}


@Composable
private fun HorizontalWorkbenchTabRow(controller: WorkbenchController) {
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
private fun VerticalWorkbenchTabRow(controller: WorkbenchController) {
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
private fun ScrollToSelected(controller: WorkbenchController, state: LazyListState){
    LaunchedEffect(controller.getModulesFiltered().size) {
        state.scrollToItem(controller.getScrollToIndex())
    }
}

private fun LazyListScope.WorkbenchTabs(controller: WorkbenchController) {
    preview(controller)
    items(controller.getModulesFiltered()){ item ->
        WorkbenchTab(
            tab = item,
            controller = controller,
            selected = controller.isModuleSelected(item),
            onClick = { controller.moduleSelectorPressed(item) },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.preview(controller: WorkbenchController) {
    stickyHeader() {
        if(controller.previewState.hasPreview()){
            val writerModifier = getTabModifier(controller, true) {}
            WorkbenchTab(writerModifier, controller.previewState.previewTitle!!, {})
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun WorkbenchTab(tab: WorkbenchModuleState<*>, controller: WorkbenchController, selected: Boolean, onClick: ()->Unit) {
    val writerModifier = getTabModifier(controller, selected, onClick)

    DragTarget(module = tab, controller = controller) {
        ContextMenuArea(items = {
            listOf(
                ContextMenuItem("Open in Window") { controller.convertToWindow(tab) },
            )
        }) {
            WorkbenchTab(writerModifier, tab.getTitle(), tab::onClose)
        }
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
private fun getTabModifier(controller: WorkbenchController, selected: Boolean, onClick: () -> Unit): Modifier{
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
                val ypos = height - (strokeWidth/2)
                drawLine(
                    start = Offset(x = 0f, y = ypos),
                    end = Offset(x = width, y = ypos),
                    color = contentColor,
                    strokeWidth = strokeWidth
                )
            }
        }
}
