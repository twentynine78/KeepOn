package fr.twentynine.keepon.ui.widget

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.ColorFilter
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.data.model.WidgetUIState
import fr.twentynine.keepon.util.RequiredPermissionsManager
import fr.twentynine.keepon.util.extensions.getContentBitmap
import fr.twentynine.keepon.widget.SetNextTimeoutActionCallback

@Composable
fun KeepOnWidgetView(
    widgetUIState: WidgetUIState,
) {
    KeepOnWidgetTheme(widgetUIState) { currentState ->
        when (currentState) {
            is WidgetUIState.Loading -> KeepOnWidgetLoading()
            is WidgetUIState.Success -> {
                // Get local context
                val context = LocalContext.current

                // Get bitmap async with LaunchEffect
                var bitmap by remember(currentState) { mutableStateOf<Bitmap?>(null) }
                LaunchedEffect(currentState) {
                    bitmap = currentState.getContentBitmap(context)
                }

                // Get colors
                val borderColor = remember(currentState.keepOnIsActive) {
                    if (currentState.keepOnIsActive) {
                        KeepOnWidgetColorScheme.colors.background.getColor(context)
                    } else {
                        KeepOnWidgetColorScheme.colors.primaryContainer.getColor(context)
                    }
                }
                val widgetBackgroundColor = KeepOnWidgetColorScheme.colors.widgetBackground.getColor(
                    context
                ).copy(alpha = WIDGET_BACKGROUND_COLOR_ALPHA)
                val backgroundColor = remember(currentState.keepOnIsActive) {
                    if (currentState.keepOnIsActive) {
                        KeepOnWidgetColorScheme.colors.primaryContainer
                    } else {
                        KeepOnWidgetColorScheme.colors.background
                    }
                }
                val imageColorFilter = remember(currentState.keepOnIsActive) {
                    ColorFilter.tint(
                        if (currentState.keepOnIsActive) {
                            KeepOnWidgetColorScheme.colors.onPrimaryContainer
                        } else {
                            KeepOnWidgetColorScheme.colors.onBackground
                        }
                    )
                }
                val contentColor = remember(currentState.keepOnIsActive) {
                    if (currentState.keepOnIsActive) {
                        KeepOnWidgetColorScheme.colors.onPrimaryContainer
                    } else {
                        KeepOnWidgetColorScheme.colors.onBackground
                    }
                }

                // Get click action
                val timeoutsWithDefault = remember(currentState.selectedTimeouts, currentState.defaultTimeout) {
                    if (currentState.selectedTimeouts.contains(currentState.defaultTimeout)) {
                        currentState.selectedTimeouts
                    } else {
                        listOf(currentState.defaultTimeout) + currentState.selectedTimeouts
                    }
                }
                val filteredSelectedScreenTimeouts = remember(timeoutsWithDefault, currentState.currentScreenTimeout) {
                    timeoutsWithDefault
                        .filter { screenTimeout -> screenTimeout != currentState.currentScreenTimeout }
                }
                val isSetupIncomplete = filteredSelectedScreenTimeouts.isEmpty() || !RequiredPermissionsManager.isPermissionsGranted(context)
                val clickAction = remember(isSetupIncomplete, currentState.currentScreenTimeout) {
                    if (isSetupIncomplete) {
                        val mainActivityIntent =
                            Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        actionStartActivity(mainActivityIntent)
                    } else {
                        actionRunCallback<SetNextTimeoutActionCallback>(
                            actionParametersOf(
                                SetNextTimeoutActionCallback.currentTimeoutParameterKey to currentState.currentScreenTimeout.value
                            )
                        )
                    }
                }

                KeepOnWidgetContent(
                    borderColor,
                    backgroundColor,
                    widgetBackgroundColor,
                    imageColorFilter,
                    contentColor,
                    bitmap,
                    clickAction
                )
            }
            is WidgetUIState.Error -> KeepOnWidgetError(currentState)
        }
    }
}
