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
import controller.Action
import controller.WorkbenchAction
import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.OnCloseResponse
import model.data.enums.PopUpType
import model.state.PopUpState
import model.state.WorkbenchInformationState
import model.state.WorkbenchModuleState
import util.vertical

/**
 * Component combining tab headers and currently selected Module of the given controller
 */
@Composable
internal fun TabSpace(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
    tabRowKey: TabRowKey,
    onSelect: (WorkbenchModuleState<*>) -> Unit
){
    Column {
        WorkbenchTabRow(informationState, onActionRequired, tabRowKey, onSelect)
        WorkbenchTabBody(informationState, onActionRequired, tabRowKey)
    }
}

/**
 * Component which shows Tab headers for all modules in the given controller
 */
@Composable
internal fun WorkbenchTabRow(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
    tabRowKey: TabRowKey,
    onSelect: (WorkbenchModuleState<*>) -> Unit = {}
) {
    if (tabRowKey.displayType.orientation.toInt() != 0) {
        Box {
            VerticalWorkbenchTabRow(informationState, onActionRequired, tabRowKey, onSelect)
        }
    } else {
        Box {
           HorizontalWorkbenchTabRow(informationState, onActionRequired, tabRowKey, onSelect)
        }
    }
    handlePopUps(informationState, onActionRequired, tabRowKey)
}

/**
 * Component which shows the currently selected Module of the given controller
 */
@Composable
internal fun WorkbenchTabBody(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
    tabRowKey: TabRowKey
) {
    val hasMultipleEditors = informationState.getRegisteredEditors<Any>(informationState.getSelectedModule(tabRowKey)).size > 1
    val selected = informationState.tabRowState[tabRowKey]?.selected
    Column {
        Box(modifier = Modifier.weight(if (hasMultipleEditors) 0.85f else 1f).fillMaxSize()) {
            selected?.content()
        }
        if(hasMultipleEditors && selected != null){
            Box(modifier = Modifier.weight(0.15f).fillMaxSize()) {
                WorkbenchEditorSelector(informationState = informationState, onActionRequired = onActionRequired, tabRowKey = tabRowKey, moduleState = selected)
            }
        }
    }
}

@Composable
internal fun WorkbenchEditorSelector(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
    tabRowKey: TabRowKey,
    moduleState: WorkbenchModuleState<*>
) {
    val editors = informationState.getRegisteredEditors<Any>(informationState.getSelectedModule(tabRowKey))
    Row(modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        for (editor: WorkbenchModule<*> in editors){
            IconButton(
                onClick = {
                    if (informationState.isUnsaved(moduleState)) {
                        onActionRequired.invoke(WorkbenchAction.SetPopUp(tabRowKey,
                            popUpState = PopUpState(
                                type = PopUpType.SAVE,
                                message = "") {
                                    onActionRequired.invoke(WorkbenchAction.UpdateEditor(informationState.getSelectedModule(tabRowKey)!!, editor))
                                }
                        ))
                    } else {
                        onActionRequired.invoke(WorkbenchAction.UpdateEditor(informationState.getSelectedModule(tabRowKey)!!, editor))
                    }
                }
            ){
                Icon(editor.icon, "")
            }
        }
    }
}

@Composable
private fun HorizontalWorkbenchTabRow(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
    tabRowKey: TabRowKey,
    onSelect: (WorkbenchModuleState<*>) -> Unit
) {
    val scrollState = rememberLazyListState()
    val moduleStates = informationState.getModulesFiltered(tabRowKey)
    val preview = informationState.getPreviewTitle(tabRowKey)
    val selected = informationState.getSelectedModule(tabRowKey)
    ScrollToSelected(informationState, tabRowKey, scrollState)
    Column {
        LazyRow(state = scrollState) {
            WorkbenchTabs(informationState, onActionRequired, selected, preview, moduleStates, tabRowKey, onSelect)
        }
        HorizontalScrollbar(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun VerticalWorkbenchTabRow(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
    tabRowKey: TabRowKey,
    onSelect: (WorkbenchModuleState<*>) -> Unit
) {
    val scrollState = rememberLazyListState()
    val moduleStates = informationState.getModulesFiltered(tabRowKey)
    val preview = informationState.getPreviewTitle(tabRowKey)
    val selected = informationState.getSelectedModule(tabRowKey)
    ScrollToSelected(informationState, tabRowKey, scrollState)
    Row {
        LazyColumn(state = scrollState) {
            WorkbenchTabs(informationState, onActionRequired, selected, preview, moduleStates, tabRowKey, onSelect)
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterVertically),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
private fun handlePopUps(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
    tabRowKey: TabRowKey
){
    if (informationState.isShowPopUp(tabRowKey)) {
        val selected = informationState.getSelectedModule(tabRowKey)!!
        val popUpState = informationState.tabRowState[tabRowKey]!!.popUpState!!
        when (popUpState.type) {
            PopUpType.SAVE -> WorkbenchPopupSave({resp ->
                when (resp) {
                    OnCloseResponse.DISCARD -> popUpState.action.invoke()
                    OnCloseResponse.SAVE -> onActionRequired.invoke(WorkbenchAction.SaveModuleState(selected, popUpState.action))
                    OnCloseResponse.CANCEL -> onActionRequired.invoke(WorkbenchAction.RemovePopUp(tabRowKey))
                }
            }, false)
            PopUpType.SAVE_FAILED -> WorkbenchPopupActionFailed(onActionRequired,"save", popUpState, tabRowKey)
            PopUpType.CLOSE_FAILED -> WorkbenchPopupActionFailed(onActionRequired,"close", popUpState, tabRowKey)
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

private fun LazyListScope.WorkbenchTabs(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
    selected: WorkbenchModuleState<*>?,
    preview: String?,
    moduleStates: List<WorkbenchModuleState<*>> ,
    tabRowKey: TabRowKey,
    onSelect: (WorkbenchModuleState<*>) -> Unit
) {
    preview(preview, tabRowKey)
    items(moduleStates){ item ->
        WorkbenchTab(
            informationState = informationState,
            onActionRequired = onActionRequired,
            moduleState = item,
            tabRowKey = tabRowKey,
            selected = selected == item,
            onClick = {
                        //TODO: Make this a list of actions to trigger
                        onSelect.invoke(item)
                        onActionRequired.invoke(WorkbenchAction.TabSelectorPressed(tabRowKey, item))
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
private fun WorkbenchTab(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
    moduleState: WorkbenchModuleState<*>,
    tabRowKey: TabRowKey,
    selected: Boolean,
    onClick: ()->Unit
) {
    val writerModifier = getTabModifier(tabRowKey, selected, onClick)

    DragTarget(module = moduleState, onActionRequired = onActionRequired) {
        ContextMenuArea(items = {
            listOf(
                ContextMenuItem("Open in Window") {
                    onActionRequired.invoke(WorkbenchAction.ModuleToWindow(moduleState)) },
            )
        }) {
            WorkbenchTab(writerModifier, moduleState.getTitle()) {
                if (informationState.isUnsaved(moduleState)) {
                    onActionRequired.invoke(WorkbenchAction.SetPopUp(
                        tabRowKey = tabRowKey,
                        popUpState = PopUpState(
                            type = PopUpType.SAVE,
                            message = ""
                        ) {
                            onActionRequired.invoke(WorkbenchAction.CloseModuleState(moduleState))
                        }
                    ))
                } else {
                    onActionRequired.invoke(WorkbenchAction.CloseModuleState(moduleState))
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
