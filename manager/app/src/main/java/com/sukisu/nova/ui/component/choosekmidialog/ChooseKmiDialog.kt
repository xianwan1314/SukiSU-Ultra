package com.sukisu.nova.ui.component.choosekmidialog

import androidx.compose.runtime.Composable
import com.sukisu.nova.ui.LocalUiMode
import com.sukisu.nova.ui.UiMode

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
