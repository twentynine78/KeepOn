package fr.twentynine.keepon.ui.widget.component

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.core.transition.TransitionPlayer
import fr.twentynine.keepon.domain.catalog.IconTransitionCatalog
import fr.twentynine.keepon.domain.model.IconTransition
import fr.twentynine.keepon.domain.model.IconTransitionTiming
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.ui.state.WidgetUIState
import fr.twentynine.keepon.ui.widget.SetNextTimeoutActionCallback
import fr.twentynine.keepon.ui.widget.getContentBitmap
import fr.twentynine.keepon.ui.widget.theme.KeepOnWidgetTheme
import fr.twentynine.keepon.ui.widget.theme.widgetColors

// ── Widget icon-change animation (EXPERIMENTAL) ────────────────────────────────────────────────
// Glance has no frame clock: the transition is played by mutating the displayed bitmap, each change
// pushing a fresh RemoteViews. Launchers throttle widget updates, so it pushes the smaller
// IconTransitionTiming.WIDGET_FRAME_COUNT and may still look coarse or be dropped on some launchers.

/**
 * Last icon + timeout each widget drew, kept at module level **per GlanceId** so the animation
 * survives a Glance composition restart (an `updateAll` recreates the composition and loses
 * `remember`) AND so multiple widget instances don't share one state (a single shared holder makes
 * only the first widget to react animate — the others see the timeout already updated).
 */
private object WidgetTransitionState {
    private const val MAX_ENTRIES = 16
    private const val LOAD_FACTOR = 0.75f

    private class Entry(val bitmap: Bitmap?, val timeout: ScreenTimeout?)

    // Access-ordered + capped: a get/put marks an entry most-recently-used, and the eldest is
    // evicted past MAX_ENTRIES so stale widgets/size buckets cannot pile up bitmaps forever.
    private val entries = object : LinkedHashMap<String, Entry>(MAX_ENTRIES, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>): Boolean =
            size > MAX_ENTRIES
    }

    @Synchronized
    fun lastBitmap(key: String): Bitmap? = entries[key]?.bitmap

    @Synchronized
    fun lastTimeout(key: String): ScreenTimeout? = entries[key]?.timeout

    @Synchronized
    fun markTimeout(key: String, timeout: ScreenTimeout) {
        entries[key] = Entry(entries[key]?.bitmap, timeout)
    }

    @Synchronized
    fun record(key: String, bitmap: Bitmap, timeout: ScreenTimeout) {
        entries[key] = Entry(bitmap, timeout)
    }

    @Synchronized
    fun clear() = entries.clear()
}

/** Releases the widget transition's out-of-Coil cached bitmaps; called on a memory-pressure trim. */
internal fun clearWidgetTransitionCache() = WidgetTransitionState.clear()

/**
 * Root Glance content of the home-screen widget. Renders the loading/error/success states, plays the
 * experimental icon-change transition on a real timeout change (see the header above), and wires the
 * click to either cycle the timeout (via the action callback) or open the app when nothing can cycle.
 */
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

                // Per-widget AND per-size key: SizeMode.Responsive composes this view once per size
                // bucket, all sharing one GlanceId — keying by id alone makes the buckets race on the
                // shared state so only one size animates. Including the size lets every bucket animate.
                val widgetKey = "${LocalGlanceId.current}#${LocalSize.current}"
                val transition = remember(currentState.iconTransitionAnimation.typeId) {
                    IconTransitionCatalog.fromId(currentState.iconTransitionAnimation.typeId)
                }

                // Start from the last drawn icon (survives composition restarts) to avoid a spinner flash.
                var displayedBitmap by remember(widgetKey) { mutableStateOf(WidgetTransitionState.lastBitmap(widgetKey)) }

                LaunchedEffect(widgetKey, currentState.currentScreenTimeout, currentState.timeoutIconStyle) {
                    val newBitmap = currentState.getContentBitmap(context) ?: return@LaunchedEffect
                    val previousBitmap = WidgetTransitionState.lastBitmap(widgetKey)
                    val previousTimeout = WidgetTransitionState.lastTimeout(widgetKey)
                    // Record the target up-front so a parallel updateAll restart does not replay it.
                    WidgetTransitionState.markTimeout(widgetKey, currentState.currentScreenTimeout)
                    // Animate only on a real timeout change (a previous icon exists and the value
                    // changed) — never on the first render or a pure style change.
                    val timeoutChanged = previousTimeout != null &&
                        currentState.currentScreenTimeout != previousTimeout
                    if (currentState.iconTransitionAnimation.enabled &&
                        timeoutChanged &&
                        previousBitmap != null
                    ) {
                        playWidgetTransition(
                            previousBitmap,
                            newBitmap,
                            transition,
                            currentState.iconTransitionAnimation.durationStep,
                        ) { frame ->
                            displayedBitmap = frame
                        }
                    }
                    displayedBitmap = newBitmap
                    WidgetTransitionState.record(widgetKey, newBitmap, currentState.currentScreenTimeout)
                }

                // Get colors (active vs inactive palette; the preview reuses the inactive one)
                val colors = widgetColors(currentState.keepOnIsActive)

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
                    borderColor = colors.borderColor,
                    backgroundColor = colors.backgroundColor,
                    widgetBackgroundColor = colors.widgetBackgroundColor,
                    imageColorFilter = colors.imageColorFilter,
                    contentColor = colors.contentColor,
                    contentBitmap = displayedBitmap,
                    onClickAction = clickAction,
                )
            }
            is WidgetUIState.Error -> KeepOnWidgetError(currentState)
        }
    }
}

/**
 * Plays the configured transition by pushing successive composite frames (reuses the shared
 * transition renderer); each [emitFrame] mutates the displayed bitmap → a Glance redraw.
 */
private suspend fun playWidgetTransition(
    oldBitmap: Bitmap,
    newBitmap: Bitmap,
    transition: IconTransition,
    durationStep: Int,
    emitFrame: (Bitmap) -> Unit,
) {
    TransitionPlayer.play(
        transition = transition,
        from = oldBitmap,
        to = newBitmap,
        durationMs = IconTransitionTiming.durationMs(durationStep),
        maxFrames = IconTransitionTiming.WIDGET_FRAME_COUNT,
        emitFrame = emitFrame,
    )
}
