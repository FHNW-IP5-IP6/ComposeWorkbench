package model.state

import androidx.compose.runtime.Composable

@FunctionalInterface
interface ContentHolder {

    @Composable
    fun content()
}