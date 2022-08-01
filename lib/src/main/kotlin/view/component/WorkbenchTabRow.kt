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
import controller.WorkbenchController.moduleStateSelectorPressed
import controller.WorkbenchController.moduleToWindow
import controller.WorkbenchController.removePopUp
import controller.WorkbenchController.reselect
import controller.WorkbenchController.save
import controller.WorkbenchController.setPopUp
import controller.WorkbenchController.updateModule
import controller.WorkbenchDragController
import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.OnCloseResponse
import model.data.enums.PopUpType
import model.state.WorkbenchInformationState
import model.state.WorkbenchModuleState
import util.vertical

/**
 * Component combining tab headers and currently selected Module of the given controller
 */
@Composable
internal fun TabSpace(informationState: WorkbenchInformationState, tabRowKey: TabRowKey, onSelect: (WorkbenchModuleState<*>) -> Unit){
    Column {
        WorkbenchTabRow(informationState, tabRowKey, onSelect)
        WorkbenchTabBody(informationState, tabRowKey)
    }
}

/**
 * Component which shows Tab headers for all modules in the given controller
 */
@Composable
internal fun WorkbenchTabRow(informationState: WorkbenchInformationState, tabRowKey: TabRowKey, onSelect: (WorkbenchModuleState<*>) -> Unit = {}) {
    if (tabRowKey.displayType.orientation.toInt() != 0) {
        Box {
            VerticalWorkbenchTabRow(informationState, tabRowKey, onSelect)
        }
    } else {
        Box {
           HorizontalWorkbenchTabRow(informationState, tabRowKey, onSelect)
        }
    }
    handlePopUps(informationState, tabRowKey)
}

/**
 * Component which shows the currently selected Module of the given controller
 */
@Composable
internal fun WorkbenchTabBody(informationState: WorkbenchInformationState, tabRowKey: TabRowKey) {
    val hasMultipleEditors = informationState.getRegisteredEditors<Any>(informationState.getSelectedModule(tabRowKey)).size > 1
    val selected = informationState.tabRowState[tabRowKey]?.selected
    Column {
        Box(modifier = Modifier.weight(if (hasMultipleEditors) 0.85f else 1f).fillMaxSize()) {
            selected?.content()
        }
        if(hasMultipleEditors && selected != null){
            Box(modifier = Modifier.weight(0.15f).fillMaxSize()) {
                WorkbenchEditorSelector(informationState = informationState, tabRowKey = tabRowKey, moduleState = selected)
            }
        }
    }
}

@Composable
internal fun WorkbenchEditorSelector(informationState: WorkbenchInformationState, tabRowKey: TabRowKey, moduleState: WorkbenchModuleState<*>) {
    val editors = informationState.getRegisteredEditors<Any>(informationState.getSelectedModule(tabRowKey))
    Row(modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        for (editor: WorkbenchModule<*> in editors){
            IconButton(
                onClick = {
                    if (informationState.isUnsaved(moduleState)) {
                        setPopUp(tabRowKey, PopUpType.SAVE){
                            updateModule(informationState.getSelectedModule(tabRowKey)!!, editor)
                        }
                    } else {
                        updateModule(informationState.getSelectedModule(tabRowKey)!!, editor)
                    }
                }
            ){
                Icon(editor.icon, "")
            }
        }
    }
}

@Composable
private fun HorizontalWorkbenchTabRow(informationState: WorkbenchInformationState, tabRowKey: TabRowKey, onSelect: (WorkbenchModuleState<*>) -> Unit) {
    val scrollState = rememberLazyListState()
    val moduleStates = informationState.getModulesFiltered(tabRowKey)
    val preview = informationState.getPreviewTitle(tabRowKey)
    val selected = informationState.getSelectedModule(tabRowKey)
    ScrollToSelected(informationState, tabRowKey, scrollState)
    Column {
        LazyRow(state = scrollState) {
            WorkbenchTabs(informationState, selected, preview, moduleStates, tabRowKey, onSelect)
        }
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun VerticalWorkbenchTabRow(informationState: WorkbenchInformationState, tabRowKey: TabRowKey, onSelect: (WorkbenchModuleState<*>) -> Unit) {
    val scrollState = rememberLazyListState()
    val moduleStates = informationState.getModulesFiltered(tabRowKey)
    val preview = informationState.getPreviewTitle(tabRowKey)
    val selected = informationState.getSelectedModule(tabRowKey)
    ScrollToSelected(informationState, tabRowKey, scrollState)
    Row {
        LazyColumn(state = scrollState) {
            WorkbenchTabs(informationState, selected, preview, moduleStates, tabRowKey, onSelect)
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterVertically),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun handlePopUps(informationState: WorkbenchInformationState, tabRowKey: TabRowKey){
    if (informationState.isShowPopUp(tabRowKey)) {
        val selected = informationState.getSelectedModule(tabRowKey)!!
        val popUpState = informationState.tabRowState[tabRowKey]!!.popUpState!!
        when (popUpState.type) {
            PopUpType.SAVE ->  WorkbenchPopupSave({resp ->
                when (resp) {
                    OnCloseResponse.DISCARD -> popUpState.action.invoke()
                    OnCloseResponse.SAVE -> save(selected, popUpState.action)
                    OnCloseResponse.CANCEL -> removePopUp(tabRowKey)
                }
            }, false)
            PopUpType.SAVE_FAILED -> WorkbenchPopupActionFailed("save", popUpState, tabRowKey)
            PopUpType.CLOSE_FAILED -> WorkbenchPopupActionFailed("close", popUpState, tabRowKey)
            else -> throw UnsupportedOperationException()
        }
    }
}

@Composable
private fun ScrollToSelected(informationState: WorkbenchInformationState, tabRowKey: TabRowKey, state: LazyListState){
    LaunchedEffect(informationState.getModulesFiltered(tabRowKey).size) {
        state.scrollToItem(informationState.getScrollToIndex(tabRowKey))
    }
}

private fun LazyListScope.WorkbenchTabs(informationState: WorkbenchInformationState, selected: WorkbenchModuleState<*>?,preview: String?, moduleStates: List<WorkbenchModuleState<*>> ,tabRowKey: TabRowKey, onSelect: (WorkbenchModuleState<*>) -> Unit) {
    preview(preview, tabRowKey)
    items(moduleStates){ item ->
        WorkbenchTab(
            informationState = informationState,
            moduleState = item,
            tabRowKey = tabRowKey,
            selected = selected == item,
            onClick = {
                        onSelect.invoke(item)
                        moduleStateSelectorPressed(tabRowKey, item)
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
private fun WorkbenchTab(informationState: WorkbenchInformationState, moduleState: WorkbenchModuleState<*>, tabRowKey: TabRowKey, selected: Boolean, onClick: ()->Unit) {
    val writerModifier = getTabModifier(tabRowKey, selected, onClick)

    DragTarget(module = moduleState, informationState = informationState, dragState = WorkbenchDragController.dragState) {
        ContextMenuArea(items = {
            listOf(
                ContextMenuItem("Open in Window") {
                    reselect(moduleState)
                    moduleToWindow(moduleState) },
            )
        }) {
            WorkbenchTab(writerModifier, moduleState.getTitle()) {
                if (informationState.isUnsaved(moduleState)) {
                    setPopUp(type = PopUpType.SAVE, tabRowKey = tabRowKey) {
                        WorkbenchController.close(moduleState)
                    }
                } else {
                    WorkbenchController.close(moduleState)
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
