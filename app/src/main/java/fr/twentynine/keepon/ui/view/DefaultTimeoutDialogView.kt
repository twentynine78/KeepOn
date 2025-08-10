package fr.twentynine.keepon.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.mapper.ScreenTimeoutUIToScreenTimeoutMapper
import fr.twentynine.keepon.data.model.ScreenTimeout
import fr.twentynine.keepon.data.model.ScreenTimeoutUI

@Composable
fun DefaultTimeoutDialogView(
    openDialog: MutableState<Boolean>,
    onConfirmation: (ScreenTimeout) -> Unit,
    screenTimeoutUI: MutableState<ScreenTimeoutUI?>,
) {
    val isOpen by openDialog
    val currentScreenTimeoutUI by screenTimeoutUI

    if (isOpen && currentScreenTimeoutUI != null) {
        val rememberedScreenTimeoutUI = currentScreenTimeoutUI!!

        AlertDialog(
            icon = {
                Icon(Icons.Default.Build, contentDescription = null)
            },
            title = {
                Text(
                    text = stringResource(R.string.dialog_default_timeout_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.dialog_default_timeout_text,
                        rememberedScreenTimeoutUI.displayName
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            onDismissRequest = {
                openDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmation(ScreenTimeoutUIToScreenTimeoutMapper.map(rememberedScreenTimeoutUI))
                        openDialog.value = false
                    }
                ) {
                    Text(text = stringResource(R.string.dialog_default_timeout_confirm_button_text))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text(text = stringResource(R.string.dialog_default_timeout_dismiss_button_text))
                }
            }
        )
    }
}

