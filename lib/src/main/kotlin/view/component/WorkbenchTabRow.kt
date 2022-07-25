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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import controller.WorkbenchController
import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.OnCloseResponse
import model.data.enums.PopUpType
import model.state.WorkbenchModuleState
import util.vertical

/**
 * Component combining tab headers and currently selected Module of the given controller
 */
@Composable
internal fun TabSpace(tabRowKey: TabRowKey, controller: WorkbenchController, onSelect: (WorkbenchModuleState<*>) -> Unit){
    Column {
        WorkbenchTabRow(tabRowKey, controller, onSelect)
        WorkbenchTabBody(tabRowKey, controller)
    }
}

/**
 * Component which shows Tab headers for all modules in the given controller
 */
@Composable
internal fun WorkbenchTabRow(tabRowKey: TabRowKey, controller: WorkbenchController, onSelect: (WorkbenchModuleState<*>) -> Unit = {}) {
    if (tabRowKey.displayType.orientation.toInt() != 0) {
        Box {
            VerticalWorkbenchTabRow(tabRowKey, controller, onSelect)
        }
    } else {
        Box {
           HorizontalWorkbenchTabRow(tabRowKey, controller, onSelect)
        }
    }
    handlePopUps(controller, tabRowKey)
}

/**
 * Component which shows the currently selected Module of the given controller
 */
@Composable
internal fun WorkbenchTabBody(tabRowKey: TabRowKey, controller: WorkbenchController) {
    val hasMultipleEditors = controller.getRegisteredEditors(controller.getSelectedModule(tabRowKey)).size > 1
    val selected = controller.informationState.tabRowState[tabRowKey]?.selected
    Column {
        Box(modifier = Modifier.weight(if (hasMultipleEditors) 0.85f else 1f).fillMaxSize()) {
            selected?.content()
        }
        if(hasMultipleEditors && selected != null){
            Box(modifier = Modifier.weight(0.15f).fillMaxSize()) {
                WorkbenchEditorSelector(controller = controller, tabRowKey = tabRowKey, moduleState = selected)
            }
        }
    }
}

@Composable
internal fun WorkbenchEditorSelector(tabRowKey: TabRowKey, controller: WorkbenchController, moduleState: WorkbenchModuleState<*>) {
    val editors = controller.getRegisteredEditors(controller.getSelectedModule(tabRowKey))
    Row(modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        for (editor: WorkbenchModule<*> in editors){
            IconButton(
                onClick = {
                    if (controller.isUnsaved(moduleState)) {
                        controller.setPopUp(tabRowKey, PopUpType.SAVE){
                            controller.updateModule(controller.getSelectedModule(tabRowKey)!!, editor)
                        }
                    } else {
                        controller.updateModule(controller.getSelectedModule(tabRowKey)!!, editor)
                    }
                }
            ){
                Icon(editor.icon, "")
            }
        }
    }
}

@Composable
private fun HorizontalWorkbenchTabRow(tabRowKey: TabRowKey, controller: WorkbenchController, onSelect: (WorkbenchModuleState<*>) -> Unit) {
    val scrollState = rememberLazyListState()
    val moduleStates = controller.getModulesFiltered(tabRowKey)
    val preview = controller.getPreviewTitle(tabRowKey)
    val selected = controller.getSelectedModule(tabRowKey)
    ScrollToSelected(tabRowKey, controller, scrollState)
    Column {
        LazyRow(state = scrollState) {
            WorkbenchTabs(selected, preview, moduleStates, tabRowKey, controller, onSelect)
        }
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun VerticalWorkbenchTabRow(tabRowKey: TabRowKey, controller: WorkbenchController, onSelect: (WorkbenchModuleState<*>) -> Unit) {
    val scrollState = rememberLazyListState()
    val moduleStates = controller.getModulesFiltered(tabRowKey)
    val preview = controller.getPreviewTitle(tabRowKey)
    val selected = controller.getSelectedModule(tabRowKey)
    ScrollToSelected(tabRowKey, controller, scrollState)
    Row {
        LazyColumn(state = scrollState) {
            WorkbenchTabs(selected, preview, moduleStates, tabRowKey, controller, onSelect)
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterVertically),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun handlePopUps(controller: WorkbenchController, tabRowKey: TabRowKey){
    if (controller.isShowPopUp(tabRowKey)) {
        val selected = controller.getSelectedModule(tabRowKey)!!
        val popUpState = controller.informationState.tabRowState[tabRowKey]!!.popUpState!!
        when (popUpState.type) {
            PopUpType.SAVE ->  WorkbenchPopupSave({resp ->
                when (resp) {
                    OnCloseResponse.DISCARD -> popUpState.action.invoke()
                    OnCloseResponse.SAVE -> controller.save(selected, popUpState.action)
                    OnCloseResponse.CANCEL -> controller.removePopUp(tabRowKey)
                }
            }, false)
            PopUpType.SAVE_FAILED -> WorkbenchPopupActionFailed(controller,"save", popUpState, tabRowKey)
            PopUpType.CLOSE_FAILED -> WorkbenchPopupActionFailed(controller,"close", popUpState, tabRowKey)
            else -> throw UnsupportedOperationException()
        }
    }
}

@Composable
private fun ScrollToSelected(tabRowKey: TabRowKey, controller: WorkbenchController, state: LazyListState){
    LaunchedEffect(controller.getModulesFiltered(tabRowKey).size) {
        state.scrollToItem(controller.getScrollToIndex(tabRowKey))
    }
}

private fun LazyListScope.WorkbenchTabs(selected: WorkbenchModuleState<*>?,preview: String?, moduleStates: List<WorkbenchModuleState<*>> ,tabRowKey: TabRowKey, controller: WorkbenchController, onSelect: (WorkbenchModuleState<*>) -> Unit) {
    preview(preview, tabRowKey)
    items(moduleStates){ item ->
        WorkbenchTab(
            moduleState = item,
            controller = controller,
            tabRowKey = tabRowKey,
            selected = selected == item,
            onClick = {
                        onSelect.invoke(item)
                        controller.moduleStateSelectorPressed(tabRowKey, item)
                      },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.preview(preview: String?, tabRowKey: TabRowKey) {
    stickyHeader {
        if(preview != null){
            val writerModifier = getTabModifier(tabRowKey, true) {}
            WorkbenchTab(writerModifier, preview) {}
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkbenchTab(moduleState: WorkbenchModuleState<*>, controller: WorkbenchController, tabRowKey: TabRowKey, selected: Boolean, onClick: ()->Unit) {
    val writerModifier = getTabModifier(tabRowKey, selected, onClick)

    DragTarget(module = moduleState, controller = controller) {
        ContextMenuArea(items = {
            listOf(
                ContextMenuItem("Open in Window") {
                    controller.reselect(moduleState)
                    controller.moduleToWindow(moduleState) },
            )
        }) {
            WorkbenchTab(writerModifier, moduleState.getTitle()) {
                if (controller.isUnsaved(moduleState)) {
                    controller.setPopUp(type = PopUpType.SAVE, tabRowKey = tabRowKey) {
                        controller.close(moduleState)
                    }
                } else {
                    controller.close(moduleState)
                }
            }
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
private fun getTabModifier(tabRowKey: TabRowKey,selected: Boolean, onClick: () -> Unit): Modifier{
    val backgroundColor = if (selected) MaterialTheme.colors.background else MaterialTheme.colors.surface
    val contentColor = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface

    val result: Modifier = if (tabRowKey.displayType.orientation.toInt() != 0) {
        Modifier.vertical(tabRowKey.displayType.orientation).clickable {onClick()}
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
