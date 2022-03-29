package model

import androidx.compose.runtime.Composable

@FunctionalInterface
interface ContentHolder {

    @Composable
    fun content()
}