package view.component


import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import model.data.Command
import model.data.MenuEntry


private val contentPadding = PaddingValues(8.dp, 2.dp, 10.dp, 2.dp)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun WorkbenchMenu(menuEntry: MenuEntry, imageVector: ImageVector) {
    val onMenu = remember { mutableStateOf(false) }
    val preventReopen = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
    ) {
        IconButton(
            onClick = {
                if (!preventReopen.value) menuEntry.expanded.value = !menuEntry.expanded.value else preventReopen.value = false
                      },
            modifier = Modifier
                .pointerMoveFilter(
                    onEnter = {
                        onMenu.value = true
                        false
                    },
                    onExit = {
                        onMenu.value = false
                        false
                    })) {
            Icon(imageVector, contentDescription = "Menu")
        }
        DropdownMenu(
            expanded = menuEntry.expanded.value,
            onDismissRequest = {
                menuEntry.expanded.value = false;
                preventReopen.value = onMenu.value
            }
        ) {
            menuEntry.children.forEach {
                if(it is MenuEntry) {
                    WBSubMenu(it)
                    menuEntry.expanded.value = menuEntry.expanded.value || it.expanded.value
                }
                else if (it is Command) WBMenuItem(it)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ColumnScope.WBSubMenu(menuEntry: MenuEntry) {
    DropdownMenuItem(
        onClick = {},
        contentPadding = contentPadding,
        modifier = Modifier
            .setMaxHeightByFontSize()
            .pointerMoveFilter(
            onEnter = {
                menuEntry.expanded.value = true
                false
            },
            onExit = {
                menuEntry.expanded.value = false
                false
            }),
    ) {
        Row (modifier = Modifier.fillMaxWidth()) {
            Row( modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(menuEntry.text)
                Icon(Icons.Filled.PlayArrow, "subs", Modifier.size(12.dp), tint = MaterialTheme.colors.onSurface)
            }
            Box(
                Modifier.align(Alignment.Top)
            ) {
                DropdownMenu(
                    offset = DpOffset(x=0.dp, y=0.dp.minus(12.dp)),
                    expanded = menuEntry.expanded.value,
                    onDismissRequest = { menuEntry.expanded.value = false },
                    modifier = Modifier
                        .pointerMoveFilter(
                            onEnter = {
                                menuEntry.expanded.value = true
                                false
                            },
                            onExit = {
                                menuEntry.expanded.value = false
                                false
                            }).align(Alignment.TopEnd),
                ) {
                    menuEntry.children.forEach {
                        if(it is MenuEntry) {
                            WBSubMenu(it)
                            menuEntry.expanded.value = menuEntry.expanded.value || it.expanded.value
                        }
                        else if (it is Command) WBMenuItem(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.WBMenuItem(command: Command) {
    DropdownMenuItem(
        onClick = command.action,
        contentPadding = contentPadding,
        modifier = Modifier.setMaxHeightByFontSize()
    ) {
        Text(text = command.text)
    }
}

@Composable
private fun Modifier.setMaxHeightByFontSize(): Modifier =
    sizeIn(maxHeight = (MaterialTheme.typography.body1.fontSize.value*2.3).dp)
