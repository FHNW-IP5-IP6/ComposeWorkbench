package realestateeditor.allpurpose.view

import java.awt.Cursor
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuScope
import realestateeditor.allpurpose.controller.Action
import realestateeditor.allpurpose.model.Attribute


@Composable
fun <A : Action> ActionIconStrip(trigger: (A) -> Unit, vararg actionGroup : List<A>){
    Row(       modifier = Modifier.height(IntrinsicSize.Max),
                                  //.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
                content = { actionGroup.forEach { actionGroup ->
                               actionGroup.forEach { action ->  ActionIcon(trigger, action = action)  }

                               VerticalDivider()
                               }
                          })
}

@Composable
fun <A : Action> ActionButtonStrip(trigger: (A) -> Unit, actionGroup: List<A>, modifier: Modifier) {
    Row(            modifier = modifier.fillMaxWidth(),
       horizontalArrangement = Arrangement.SpaceEvenly,
       verticalAlignment     = Alignment.CenterVertically,
       content               =  { actionGroup.forEach {
                                      ActionButton(trigger = trigger,
                                                             action = it)
                                   }
                                })

}

@Composable
fun <A : Action> ActionButton(trigger: (A) -> Unit, action: A){
    Button(modifier = Modifier.cursor(if(action.enabled) Cursor.HAND_CURSOR else Cursor.DEFAULT_CURSOR),
            onClick = { trigger(action) },
            enabled = action.enabled,
            content = { Text(action.name) })
}

@Composable
fun <A : Action> ActionIcon(trigger: (A) -> Unit, action: A){
    IconButton(modifier = Modifier.cursor(if(action.enabled) Cursor.HAND_CURSOR else Cursor.DEFAULT_CURSOR),
                onClick = { trigger(action) },
                enabled = action.enabled,
                content = { if(action.icon != null) Icon(action.icon!!, action.name) else Text(action.name) })
}

@Composable
fun <A : Action> MenuScope.ActionMenuItem(trigger: (A) -> Unit, action: A){
    Item(text = action.name,
      onClick = { trigger(action) })
}

@Composable
fun AttributeField(attribute: Attribute<*>, onValueChange : (String) -> Unit, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start){
    val focusManager = LocalFocusManager.current
    OutlinedTextField(modifier = modifier,
                         value = attribute.valueAsText,
                       isError = !attribute.isValid,
                    singleLine = true,
                     textStyle = LocalTextStyle.current.copy(textAlign = textAlign),
               keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = MaterialTheme.colors.secondary,
                                                                                  cursorColor = MaterialTheme.colors.onPrimary),
                 onValueChange = onValueChange)
}

@Composable
fun AttributeTextAreaField(attribute: Attribute<*>, onValueChange : (String) -> Unit, modifier: Modifier = Modifier){
    val focusManager = LocalFocusManager.current
    OutlinedTextField(modifier = modifier,
                         value = attribute.valueAsText,
                       isError = !attribute.isValid,
                    singleLine = false,
               keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = MaterialTheme.colors.secondary,
                   cursorColor = MaterialTheme.colors.onPrimary),
                 onValueChange = onValueChange)
}

@Composable
fun VerticalDivider(){
    Box(modifier = Modifier.fillMaxHeight()
                           .padding(vertical = 14.dp)
                           .width(1.dp)
                           .background(MaterialTheme.colors.onPrimary.copy(alpha = 0.5f)))
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.cursor(cursorId: Int) : Modifier = pointerHoverIcon(PointerIcon(Cursor(cursorId)))

val CH = Locale("de", "CH")

fun Number?.format(pattern: String, nullFormat : String = "?"): String {
    return if (null == this) nullFormat else pattern.format(CH, this)
}
