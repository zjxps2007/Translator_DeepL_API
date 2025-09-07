package io.github.inho.deepl_local

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import deepl_local.composeapp.generated.resources.Res
import deepl_local.composeapp.generated.resources.icon

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DeepL Local",
        icon = painterResource(Res.drawable.icon)
    ) {
        App()
    }
}