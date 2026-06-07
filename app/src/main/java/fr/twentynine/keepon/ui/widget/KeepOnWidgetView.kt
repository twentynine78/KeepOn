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
import fr.twentynine.keepon.ui.state.WidgetUIState

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

                // Get click action. Permission is enforced at click time by the callback
                // (the rendered action can be stale), so the render only routes straight to
                // the app when there is nothing to cycle.
                val clickAction = remember(currentState.canCycleTimeout, currentState.currentScreenTimeout) {
                    if (currentState.canCycleTimeout) {
                        actionRunCallback<SetNextTimeoutActionCallback>(
                            actionParametersOf(
                                SetNextTimeoutActionCallback.currentTimeoutParameterKey to currentState.currentScreenTimeout.value
                            )
                        )
                    } else {
                        val mainActivityIntent =
                            Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        actionStartActivity(mainActivityIntent)
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
