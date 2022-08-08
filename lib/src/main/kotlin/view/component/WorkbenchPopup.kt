package view.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import controller.Action
import controller.WorkbenchAction
import model.data.TabRowKey
import model.data.enums.PopUpType
import model.state.PopUpState
import model.state.WorkbenchInformationState
import java.util.*


@Composable
internal fun handlePopUps(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
){
    if (informationState.isShowPopUp()) {
        val popUp = informationState.popUpList.first()
        onActionRequired(WorkbenchAction.UpdateSelection(TabRowKey(popUp.moduleState), popUp.moduleState))
        when (popUp.type) {
            PopUpType.ON_CLOSE -> WorkbenchPopupOnClose(popUp, onActionRequired)
            PopUpType.ON_EDITOR_SWITCH -> WorkbenchPopupOnClose(popUp, onActionRequired)
            PopUpType.SAVE_FAILED -> WorkbenchPopupSaveFailed(popUp, onActionRequired)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WorkbenchPopupOnClose (
    popUpState: PopUpState,
    onActionRequired: (Action) -> Unit,
    dismissible: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = {
            if (dismissible) {
                onActionRequired(WorkbenchAction.ClosePopUp(popUpState))
            }
        },
        title = {
            PopUpTitle("Unsaved Changes")
        },
        text = {
            PopUpMessage("You have made changes. Do you want to save or discard them?")
        },
        buttons = {
            Row( modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 4.dp) ) {
                Button(
                    onClick = { onActionRequired(WorkbenchAction.ClosePopUp(popUpState)) }
                ) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(45.dp))
                Button(
                    onClick = { onActionRequired(WorkbenchAction.DiscardChanges(popUpState.moduleState, popUpState))}
                ) {
                    Text("Discard")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    modifier = Modifier.focusTarget(),
                    onClick = { onActionRequired(WorkbenchAction.SaveAndClose(popUpState.moduleState, popUpState)) }
                ) {
                    Text("Save")
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WorkbenchPopupSaveFailed (
    popUpState: PopUpState,
    onActionRequired: (Action) -> Unit,
    dismissible: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = {
            if (dismissible) {
                WorkbenchAction.ClosePopUp(popUpState)
            }
        },
        title = {
            PopUpTitle("Save Failed")
        },
        text = {
            PopUpMessage("You can not save, due to invalid changes.")
        },
        buttons = {
            Row( modifier = Modifier.padding(start = 60.dp, end = 60.dp, bottom = 4.dp) ) {
                Button(
                    onClick = { onActionRequired(WorkbenchAction.ClosePopUp(popUpState)) }
                ) {
                    Text("OK")
                }
            }
        },
    )
}

@Composable
private fun PopUpTitle(text: String) {
    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text.uppercase(Locale.getDefault()), color = MaterialTheme.colors.primary.copy(alpha = 0.7f), fontSize = 13.sp,  textAlign = TextAlign.Center)
    }
}

@Composable
private fun PopUpMessage(text: String) {
    Column (
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, color = MaterialTheme.colors.onBackground, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

// source: https://gist.github.com/EugeneTheDev/a27664cb7e7899f964348b05883cbccd
@Composable
internal fun DotsPulsing() {
    val dotSize = 24.dp
    val delayUnit = 400

    @Composable
    fun Dot(
        scale: Float
    ) = Spacer(
        Modifier
            .size(dotSize)
            .scale(scale)
            .background(
                color = MaterialTheme.colors.primary,
                shape = CircleShape
            )
    )

    val infiniteTransition = rememberInfiniteTransition()

    @Composable
    fun animateScaleWithDelay(delay: Int) = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = delayUnit * 4
                0f at delay with LinearEasing
                1f at delay + delayUnit with LinearEasing
                0f at delay + delayUnit * 2
            }
        )
    )

    val scale1 by animateScaleWithDelay(0)
    val scale2 by animateScaleWithDelay(delayUnit)
    val scale3 by animateScaleWithDelay(delayUnit * 2)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val spaceSize = 2.dp

        Dot(scale1)
        Spacer(Modifier.width(spaceSize))
        Dot(scale2)
        Spacer(Modifier.width(spaceSize))
        Dot(scale3)
    }
}