package model.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.KeyShortcut
import controller.Action

interface MenuItem {
    val text: String
    var index: Int
    operator fun compareTo(other: MenuItem): Int {
        return if (text == other.text) 0 else 1
    }
}

class Command (
    override val text: String,
    override var index: Int = 0,
    val action: Action,
    val paths: MutableList<String>,
    val shortcut: KeyShortcut? = null,
) : MenuItem {}


class MenuEntry(
    override val text: String,
    override var index: Int = 0
) : MenuItem {
    var children = mutableListOf<MenuItem>()
    var expanded = mutableStateOf<Boolean>(false)

    fun getMenu(text: String) : MenuEntry {
        val c = children.firstOrNull() { text == it.text }
        return if (c != null && c is MenuEntry) {
            c
        } else {
            val m = MenuEntry(text)
            children.add(m)
            m
        }
    }
}