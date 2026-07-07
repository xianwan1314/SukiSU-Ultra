package com.sukisu.ultra.ui.component.choosekmidialog

import androidx.compose.runtime.Composable
import com.sukisu.ultra.ui.LocalUiMode
import com.sukisu.ultra.ui.UiMode

@Composable
fun ChooseKmiDialog(
    show: Boolean,
    preferredKmi: String? = null,
    currentKmi: String = "",
    onDismissRequest: () -> Unit,
    onSelected: (String?) -> Unit
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> ChooseKmiDialogMiuix(show, preferredKmi, currentKmi, onDismissRequest, onSelected)
        UiMode.Material -> ChooseKmiDialogMaterial(show, preferredKmi, currentKmi, onDismissRequest, onSelected)
    }
}
