package view.component

import SPLIT_PAIN_HANDLE_ALPHA
import SPLIT_PAIN_HANDLE_AREA
import SPLIT_PAIN_HANDLE_SIZE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import org.jetbrains.compose.splitpane.*
import util.cursorForHorizontalResize
import util.cursorForVerticalResize

/**
 * Split pane abstraction to use in Workbench. Using this split pane implementation will ensure the same look and feel for all split panes.
 */
@OptIn(ExperimentalSplitPaneApi::class)
@Composable
internal fun WorkbenchVerticalSplitPane(
    splitPaneState: SplitPaneState,
    content: @Composable WorkbenchSplitPaneScope.() -> Unit
) {
    val scope = WorkbenchSplitPaneScopeImpl()
    scope.content()

    VerticalSplitPane(splitPaneState = splitPaneState) {
        first { scope.first() }
        second { scope.second() }
        workbenchVerticalSplitter()
    }
}

/**
 * Split pane abstraction to use in Workbench. Using this split pane implementation will ensure the same look and feel for all split panes.
 */
@OptIn(ExperimentalSplitPaneApi::class)
@Composable
internal fun WorkbenchHorizontalSplitPane(
    splitPaneState: SplitPaneState,
    content: @Composable WorkbenchSplitPaneScope.() -> Unit
) {
    val scope = WorkbenchSplitPaneScopeImpl()
    scope.content()

    HorizontalSplitPane(splitPaneState = splitPaneState) {
        first { scope.first() }
        second { scope.second() }
        workbenchHorizontalSplitter()
    }
}

@OptIn(ExperimentalSplitPaneApi::class)
private fun SplitPaneScope.workbenchVerticalSplitter() {
    splitter {
        visiblePart {
            Box( modifier = Modifier.height(SPLIT_PAIN_HANDLE_SIZE).fillMaxWidth()
                    .background(SolidColor(Color.Gray), alpha = SPLIT_PAIN_HANDLE_ALPHA)
            )
        }
        handle {
            Box( modifier = Modifier.markAsHandle().cursorForVerticalResize()
                    .height(SPLIT_PAIN_HANDLE_AREA).fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalSplitPaneApi::class)
private fun SplitPaneScope.workbenchHorizontalSplitter() {
    splitter {
        visiblePart {
            Box( modifier = Modifier.width(SPLIT_PAIN_HANDLE_SIZE).fillMaxHeight()
                    .background(SolidColor(Color.Gray), alpha = SPLIT_PAIN_HANDLE_ALPHA)
            )
        }
        handle {
            Box( modifier = Modifier.markAsHandle().cursorForHorizontalResize()
                    .width(SPLIT_PAIN_HANDLE_AREA).fillMaxHeight()
            )
        }
    }
}

internal interface WorkbenchSplitPaneScope {
    fun first(content: @Composable () -> Unit)
    fun second(content: @Composable () -> Unit)
}

private class WorkbenchSplitPaneScopeImpl() : WorkbenchSplitPaneScope {
    lateinit var first: @Composable () -> Unit
    lateinit var second: @Composable () -> Unit

    override fun first(content: @Composable () -> Unit) {
        first = content
    }

    override fun second(content: @Composable () -> Unit) {
        second = content
    }
}

