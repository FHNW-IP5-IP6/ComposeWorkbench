package view.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.unit.dp
import model.data.enums.OnCloseResponse


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WorkbenchPopupSave (onAction: (OnCloseResponse) -> Unit, dismissible: Boolean = true) {
    val openDialog = remember { mutableStateOf(true) }
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                if (dismissible) {
                    openDialog.value = false
                    onAction(OnCloseResponse.CANCEL)
                }
            },
            title = {
                Text(text = "Do you want to save the changes to this Editor?")
            },
            text = {
                Text("If you don't save, your changes will be lost.")
            },
            buttons = {
                Row( modifier = Modifier.padding(start = 8.dp, end = 8.dp) ) {
                    Button(
                        onClick = { openDialog.value = false; onAction(OnCloseResponse.DISCARD) }
                    ) {
                        Text("Discard")
                    }
                    Spacer(Modifier.width(45.dp))
                    Button(
                        onClick = { openDialog.value = false; onAction(OnCloseResponse.CANCEL) }
                    ) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.focusTarget(),
                        onClick = { openDialog.value = false; onAction(OnCloseResponse.SAVE) }
                    ) {
                        Text("Save")
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }
}

@Composable
fun InputField() {
    var text by remember { mutableStateOf("") }
    Column() {
        TextField(
            value = text,
            onValueChange = { text = it }
        )
        Text("Custom Text")
        Checkbox(checked = false, onCheckedChange = {})
    }
}

// TODO: Use this later for loading screen
// source: https://gist.github.com/EugeneTheDev/a27664cb7e7899f964348b05883cbccd
@Composable
fun DotsPulsing() {
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