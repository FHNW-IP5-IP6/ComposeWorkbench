package view.conponent

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import controller.WorkbenchModuleController
import model.state.WorkbenchModuleState
import util.selectedButtonColors
import util.vertical

@Composable
internal fun WorkbenchTabRow(controller: WorkbenchModuleController) {
    if (controller.displayType.orientation.toInt() != 0) {
        VerticalWorkbenchTabRow(controller)
    } else {
        HorizontalWorkbenchTabRow(controller)
    }
}

@Composable
private fun HorizontalWorkbenchTabRow(controller: WorkbenchModuleController) {
    val tabScrollState = rememberScrollState()

    Column() {
        Row(modifier = Modifier.horizontalScroll(tabScrollState)) {
            WorkbenchTabs(controller)
        }
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            adapter = rememberScrollbarAdapter(tabScrollState)
        )
        controller.getSelectedModule().value?.content()
    }
}

@Composable
private fun VerticalWorkbenchTabRow(controller: WorkbenchModuleController) {
    val tabScrollState = rememberScrollState()

    Row(modifier = Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.verticalScroll(tabScrollState)) {
            WorkbenchTabs(controller)
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterVertically),
            adapter = rememberScrollbarAdapter(tabScrollState)
        )
        //TODO: Make scrollable
        controller.getSelectedModule().value?.content()
    }
}

@Composable
private fun WorkbenchTabs(controller: WorkbenchModuleController) {
    for (tab in controller.getModulesFiltered()) {
        WorkbenchTab(
            tab = tab,
            controller,
            selected = controller.isModuleSelected(tab),
            onClick = { controller.moduleSelectorPressed(tab) })
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun WorkbenchTab(tab: WorkbenchModuleState<*>, controller: WorkbenchModuleController, selected: Boolean, onClick: ()->Unit) {
    val colors = ButtonDefaults.selectedButtonColors(selected)
    val backgroundColor = colors.backgroundColor(selected).value
    val contentColor = colors.contentColor(selected).value

    var writerModifier: Modifier
    if (controller.displayType.orientation.toInt() != 0) {
        writerModifier = Modifier.vertical(controller.displayType.orientation).clickable {onClick()}
    } else{
        writerModifier = Modifier.clickable {onClick()}
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

    ContextMenuArea(items = {
        listOf(
            ContextMenuItem("Open in Window") { controller.convertToWindow(tab) },
        )
    }) {
        Row(
            modifier = writerModifier,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Text(
                text = tab.title,
                color = contentColor,
                overflow = TextOverflow.Visible,
                maxLines = 1,
                modifier = Modifier.padding(15.dp, 0.dp)
            )
            IconButton(onClick = tab::onClose) {
                Icon(Icons.Filled.Close, "close", tint = contentColor)
            }
        }
    }
}