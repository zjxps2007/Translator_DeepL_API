package io.github.inho.deepl_local

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "deepl_local",
    ) {
        App()
    }
}