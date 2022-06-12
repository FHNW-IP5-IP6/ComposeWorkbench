package model.data

import androidx.compose.ui.input.key.KeyShortcut

interface MenuItem {
    val text: String
    operator fun compareTo(other: MenuItem): Int {
        return if (text == other.text) 0 else 1
    }
}

class Command (
    override val text: String,
    val action: () -> Unit,
    val path: String,
    val shortcut: KeyShortcut? = null,
) : MenuItem {}


class MenuEntry(override val text: String) : MenuItem {
    var children = mutableListOf<MenuItem>()
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