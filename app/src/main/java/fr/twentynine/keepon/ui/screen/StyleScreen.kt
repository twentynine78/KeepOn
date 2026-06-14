package fr.twentynine.keepon.ui.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.model.IconTransitionOptionUI
import fr.twentynine.keepon.ui.model.ItemPosition
import fr.twentynine.keepon.domain.catalog.IconFontFamily
import fr.twentynine.keepon.ui.event.MainUIEvent
import fr.twentynine.keepon.ui.state.MainViewUIState
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.IconTransitionTiming
import fr.twentynine.keepon.domain.model.ScreenTimeout
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.catalog.IconFontFamilyCatalog
import fr.twentynine.keepon.ui.util.KeepOnNavigationType
import fr.twentynine.keepon.ui.util.bottomSpacerHeight
import fr.twentynine.keepon.ui.util.defaultCardHorizontalPadding
import fr.twentynine.keepon.ui.util.screenContentModifier
import fr.twentynine.keepon.ui.util.stableViewportHeight
import fr.twentynine.keepon.ui.theme.KeepOnCardElevation
import fr.twentynine.keepon.ui.theme.KeepOnCardShape
import fr.twentynine.keepon.ui.theme.StyleCardTopPadding
import fr.twentynine.keepon.ui.theme.StyleContentInset
import fr.twentynine.keepon.ui.theme.StyleListRowVerticalPadding
import fr.twentynine.keepon.ui.theme.StyleSwitchRowVerticalPadding
import fr.twentynine.keepon.ui.theme.StyleTopSwitchRowVerticalPadding
import fr.twentynine.keepon.ui.component.CardHeader
import fr.twentynine.keepon.ui.component.FontPreviewChip
import fr.twentynine.keepon.ui.component.IconPositionPad
import fr.twentynine.keepon.ui.component.IconTransitionTypeGrid
import fr.twentynine.keepon.ui.component.ItemCard
import fr.twentynine.keepon.ui.component.LabeledControlRow
import fr.twentynine.keepon.ui.component.SegmentedCheckboxGroup
import fr.twentynine.keepon.ui.component.SegmentedCheckboxOption
import fr.twentynine.keepon.ui.component.Subtitle
import fr.twentynine.keepon.ui.component.SwitchSettingRow
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/** Style destination, stateful wrapper: feeds the icon style/transition slices of [uiState] to [StyleScreen]. */
@Composable
fun StyleRoute(
    uiState: MainViewUIState.Success,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType,
    paddingValue: PaddingValues,
) {
    StyleScreen(
        timeoutIconStyle = uiState.timeoutIconStyle,
        iconTransitionAnimation = uiState.iconTransitionAnimation,
        iconTransitionOptions = uiState.iconTransitionOptions,
        currentScreenTimeout = uiState.currentScreenTimeout,
        positionPadExpanded = uiState.stylePositionPadExpanded,
        onEvent = onEvent,
        navType = navType,
        paddingValue = paddingValue,
    )
}

/**
 * Style destination content (stateless): lets the user configure the icon-change transition and the
 * timeout-icon typography (font family, size, spacing, bold/italic/underline, outlined), with a live
 * preview. User changes are emitted as [MainUIEvent]s through [onEvent].
 */
@Composable
fun StyleScreen(
    timeoutIconStyle: TimeoutIconStyle,
    iconTransitionAnimation: IconTransitionAnimation,
    iconTransitionOptions: List<IconTransitionOptionUI>,
    currentScreenTimeout: ScreenTimeout,
    positionPadExpanded: Boolean,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType,
    paddingValue: PaddingValues,
) {
    val fontFamilies = remember {
        IconFontFamilyCatalog.iconFontFamilies.values.toList()
    }

    // Worst-case visible area (bars expanded): caps the position pad and anchors its centering
    // scroll; stable while scrolling, recomputed on rotation/resize.
    val viewportHeight = stableViewportHeight(navType)

    LazyColumn(
        modifier = Modifier
            .padding(paddingValue)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val maxWidthModifier = screenContentModifier

        item(key = "transitionCard") {
            IconTransitionAnimationCard(
                iconTransitionAnimation = iconTransitionAnimation,
                iconTransitionOptions = iconTransitionOptions,
                currentScreenTimeout = currentScreenTimeout,
                timeoutIconStyle = timeoutIconStyle,
                onEvent = onEvent,
                modifier = maxWidthModifier,
            )
        }

        item(key = "headerCard") {
            Column(
                modifier = maxWidthModifier
                    .padding(top = StyleCardTopPadding),
            ) {
                CardHeader(
                    iconVector = Icons.Outlined.FontDownload,
                    title = stringResource(R.string.font_selection_tile),
                    descText = stringResource(R.string.font_selection_text),
                )
            }
        }

        itemsIndexed(
            items = fontFamilies,
            key = { _, iconFontFamily -> "font_${iconFontFamily.name}" }
        ) { index, iconFontFamily ->
            val itemPosition = remember(index, fontFamilies.size) {
                ItemPosition.getItemPosition(index, fontFamilies.size)
            }

            FontSelectionRow(
                iconFontFamily = iconFontFamily,
                timeoutIconStyle = timeoutIconStyle,
                onEvent = onEvent,
                modifier = maxWidthModifier,
                itemPosition = itemPosition,
            )
        }

        item(key = "styleCard") {
            FontStyleCard(
                timeoutIconStyle = timeoutIconStyle,
                onEvent = onEvent,
                modifier = maxWidthModifier,
            )
        }

        item(key = "optionsCard") {
            FontOptionsCard(
                timeoutIconStyle = timeoutIconStyle,
                positionPadExpanded = positionPadExpanded,
                onEvent = onEvent,
                viewportHeight = viewportHeight,
                modifier = maxWidthModifier,
            )
        }

        item(key = "bottomSpacer") {
            val spacerBottomHeight = bottomSpacerHeight(navType)
            Spacer(modifier = Modifier.padding(bottom = spacerBottomHeight))
        }
    }
}

// Material disabled-content opacity, applied to the animation choices when the toggle is off.
private const val DISABLED_CONTENT_ALPHA = 0.38f

/** Duration label rounded to the nearest 10 ms (display only — the animation keeps the exact value). */
private fun durationMsRoundedForDisplay(durationStep: Int): Int =
    (IconTransitionTiming.durationMs(durationStep) / 10f).roundToInt() * 10

/** Formats a step value with an explicit sign ("+2", "-2", "0"), matching the position-pad readouts. */
private fun signedStep(value: Int): String = if (value > 0) "+$value" else value.toString()

// Shared duration of the position-pad expand/collapse, its chevron rotation and the summary fade.
private const val POSITION_PAD_ANIMATION_MS = 300

// Vertical room kept alongside the pad surface so the surface plus its readout row fit the
// stable viewport at once (readout ~30dp at default font scale, plus breathing margin).
private val PadViewportAllowance = 56.dp

// Usability floor for pathological windows (tiny split screen); below it the screen scrolls.
private val PadMinHeight = 120.dp

// Font-size slider: 10 steps centered on 0 (-5..+5).
private const val FONT_SIZE_SLIDER_STEPS = 10
private val FontSizeSliderRange = -(FONT_SIZE_SLIDER_STEPS / 2f)..FONT_SIZE_SLIDER_STEPS / 2f

@Composable
fun IconTransitionAnimationCard(
    iconTransitionAnimation: IconTransitionAnimation,
    iconTransitionOptions: List<IconTransitionOptionUI>,
    currentScreenTimeout: ScreenTimeout,
    timeoutIconStyle: TimeoutIconStyle,
    onEvent: (MainUIEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(top = StyleCardTopPadding, bottom = 12.dp),
    ) {
        CardHeader(
            iconVector = Icons.Rounded.Animation,
            title = stringResource(R.string.icon_transition_tile),
            descText = stringResource(R.string.icon_transition_text),
        )
        Card(
            modifier = Modifier
                .padding(horizontal = defaultCardHorizontalPadding)
                .align(alignment = Alignment.Start),
            shape = KeepOnCardShape,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SwitchSettingRow(
                    title = stringResource(R.string.icon_transition_enable),
                    subtitle = stringResource(R.string.icon_transition_enable_desc),
                    checked = iconTransitionAnimation.enabled,
                    onCheckedChange = { enabled ->
                        onEvent(
                            MainUIEvent.UpdateIconTransitionAnimation(
                                iconTransitionAnimation.copy(enabled = enabled)
                            )
                        )
                    },
                    verticalPadding = StyleTopSwitchRowVerticalPadding,
                )

                val animationsEnabled = iconTransitionAnimation.enabled
                Subtitle(
                    text = stringResource(R.string.icon_transition_type_subtitle),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp, start = StyleContentInset, end = StyleContentInset)
                        .alpha(if (animationsEnabled) 1f else DISABLED_CONTENT_ALPHA),
                )
                IconTransitionTypeGrid(
                    options = iconTransitionOptions,
                    animation = iconTransitionAnimation,
                    currentScreenTimeout = currentScreenTimeout,
                    timeoutIconStyle = timeoutIconStyle,
                    onSelect = { id ->
                        onEvent(
                            MainUIEvent.UpdateIconTransitionAnimation(
                                iconTransitionAnimation.copy(typeId = id)
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = StyleContentInset)
                        .alpha(if (animationsEnabled) 1f else DISABLED_CONTENT_ALPHA),
                )

                FontOptionSlider(
                    label = stringResource(R.string.icon_transition_duration),
                    valueText = stringResource(
                        R.string.icon_transition_duration_ms,
                        durationMsRoundedForDisplay(iconTransitionAnimation.durationStep),
                    ),
                    value = iconTransitionAnimation.durationStep,
                    onValueChange = { newValue ->
                        onEvent(
                            MainUIEvent.UpdateIconTransitionAnimation(
                                iconTransitionAnimation.copy(durationStep = newValue)
                            )
                        )
                    },
                    valueRange = -IconTransitionTiming.DURATION_STEP_RANGE.toFloat()..
                        IconTransitionTiming.DURATION_STEP_RANGE.toFloat(),
                    steps = IconTransitionTiming.DURATION_STEP_RANGE * 2 - 1,
                    topPadding = 12.dp,
                    bottomPadding = 8.dp,
                    enabled = animationsEnabled,
                )
            }
        }
    }
}

@Composable
fun FontSelectionRow(
    iconFontFamily: IconFontFamily,
    timeoutIconStyle: TimeoutIconStyle,
    itemPosition: ItemPosition,
    onEvent: (MainUIEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val font = remember(iconFontFamily.regularTypefaceId) {
        Font(iconFontFamily.regularTypefaceId)
    }
    val fontFamily = remember(font) {
        FontFamily(font)
    }

    val onRowClick = remember(timeoutIconStyle) {
        {
            onEvent(
                MainUIEvent.UpdateTimeoutIconStyle(
                    timeoutIconStyle.copy(iconFontFamilyName = iconFontFamily.name)
                )
            )
        }
    }

    val selected = iconFontFamily.name == timeoutIconStyle.iconFontFamilyName

    ItemCard(modifier = modifier, itemPosition = itemPosition) {
        LabeledControlRow(
            onClick = onRowClick,
            verticalPadding = StyleListRowVerticalPadding,
            leading = {
                FontPreviewChip(
                    fontFamily = fontFamily,
                    selected = selected,
                )
            },
            trailing = {
                RadioButton(
                    modifier = Modifier.padding(end = 4.dp),
                    selected = selected,
                    onClick = null,
                )
            },
            label = {
                Text(
                    text = iconFontFamily.displayName,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
        )
    }
}

@Composable
fun FontStyleCard(
    timeoutIconStyle: TimeoutIconStyle,
    onEvent: (MainUIEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onBoldChange = remember(timeoutIconStyle) {
        {
                newBoldValue: Boolean ->
            onEvent(
                MainUIEvent.UpdateTimeoutIconStyle(timeoutIconStyle.copy(iconStyleFontBold = newBoldValue))
            )
        }
    }
    val onItalicChange = remember(timeoutIconStyle) {
        {
                newItalicValue: Boolean ->
            onEvent(
                MainUIEvent.UpdateTimeoutIconStyle(timeoutIconStyle.copy(iconStyleFontItalic = newItalicValue))
            )
        }
    }
    val onUnderlineChange = remember(timeoutIconStyle) {
        {
                newUnderlineValue: Boolean ->
            onEvent(
                MainUIEvent.UpdateTimeoutIconStyle(timeoutIconStyle.copy(iconStyleFontUnderline = newUnderlineValue))
            )
        }
    }
    val onOutlinedChange = remember(timeoutIconStyle) {
        {
                newOutlinedValue: Boolean ->
            onEvent(
                MainUIEvent.UpdateTimeoutIconStyle(timeoutIconStyle.copy(iconStyleTextOutlined = newOutlinedValue))
            )
        }
    }

    Column(
        modifier = modifier
            .padding(top = StyleCardTopPadding, bottom = 12.dp),
    ) {
        CardHeader(
            iconVector = Icons.Rounded.FormatColorText,
            title = stringResource(R.string.font_style_tile),
            descText = stringResource(R.string.font_style_text),
        )
        Card(
            modifier = Modifier
                .padding(horizontal = defaultCardHorizontalPadding)
                .align(alignment = Alignment.Start),
            shape = KeepOnCardShape,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 14.dp),
            ) {
                Subtitle(
                    text = stringResource(R.string.font_style_parameters_subtitle),
                    modifier = Modifier
                        .padding(
                            start = StyleContentInset,
                            end = StyleContentInset,
                            top = 8.dp,
                            bottom = 18.dp
                        )
                        .align(Alignment.Start),
                )
                SegmentedCheckboxGroup(
                    options = listOf(
                        SegmentedCheckboxOption(
                            label = stringResource(R.string.font_style_parameters_bold),
                            checked = timeoutIconStyle.iconStyleFontBold,
                            onCheckedChange = onBoldChange,
                            glyph = Icons.Filled.FormatBold,
                        ),
                        SegmentedCheckboxOption(
                            label = stringResource(R.string.font_style_parameters_italic),
                            checked = timeoutIconStyle.iconStyleFontItalic,
                            onCheckedChange = onItalicChange,
                            glyph = Icons.Filled.FormatItalic,
                        ),
                        SegmentedCheckboxOption(
                            label = stringResource(R.string.font_style_parameters_underline),
                            checked = timeoutIconStyle.iconStyleFontUnderline,
                            onCheckedChange = onUnderlineChange,
                            glyph = Icons.Filled.FormatUnderlined,
                        ),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = StyleContentInset, end = StyleContentInset, bottom = 8.dp),
                )

                SwitchSettingRow(
                    title = stringResource(R.string.font_style_parameters_appearance_outlined),
                    subtitle = stringResource(R.string.font_style_parameters_appearance_outlined_desc),
                    checked = timeoutIconStyle.iconStyleTextOutlined,
                    onCheckedChange = onOutlinedChange,
                    verticalPadding = StyleSwitchRowVerticalPadding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontOptionsCard(
    timeoutIconStyle: TimeoutIconStyle,
    positionPadExpanded: Boolean,
    onEvent: (MainUIEvent) -> Unit,
    viewportHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(top = StyleCardTopPadding, bottom = 12.dp),
    ) {
        CardHeader(
            iconVector = Icons.Rounded.LocationSearching,
            title = stringResource(R.string.font_options_title),
            descText = stringResource(R.string.font_options_text),
        )
        Card(
            modifier = Modifier
                .padding(horizontal = defaultCardHorizontalPadding)
                .align(alignment = Alignment.Start),
            shape = KeepOnCardShape,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                val fontSize = timeoutIconStyle.iconStyleFontSize
                FontOptionSlider(
                    label = stringResource(R.string.font_options_size_subtitle),
                    valueText = remember(fontSize) { signedStep(fontSize) },
                    value = fontSize,
                    onValueChange = { newValue ->
                        onEvent(
                            MainUIEvent.UpdateTimeoutIconStyle(
                                timeoutIconStyle.copy(iconStyleFontSize = newValue)
                            )
                        )
                    },
                    valueRange = FontSizeSliderRange,
                    steps = FONT_SIZE_SLIDER_STEPS - 1,
                    topPadding = 0.dp
                )

                // The pad is collapsed by default: it grabs taps and vertical drags meant for the
                // page scroll, so it only unfolds on demand. Its expansion lives in a
                // process-lifetime holder (StylePositionPadState, surfaced through the UI state) so
                // it survives the activity recreation of a rotation while the pad is on screen;
                // any other way of leaving the composition folds it back here, since both the
                // LazyColumn and the NavHost would otherwise restore it on scroll-back/tab-return.
                val activity = LocalActivity.current
                DisposableEffect(Unit) {
                    onDispose {
                        if (activity?.isChangingConfigurations != true) {
                            onEvent(MainUIEvent.SetStylePositionPadExpanded(false))
                        }
                    }
                }
                val chevronRotation by animateFloatAsState(
                    targetValue = if (positionPadExpanded) 180f else 0f,
                    animationSpec = tween(POSITION_PAD_ANIMATION_MS),
                    label = "positionPadChevron",
                )
                // The card's bottom inset lives inside this clickable while the block is the card's
                // last element (pad collapsed), so the tap target reaches the card's bottom edge;
                // expanded, the inset moves below the pad and the block keeps its usual spacing.
                val toggleBottomPadding by animateDpAsState(
                    targetValue = if (positionPadExpanded) 12.dp else 26.dp,
                    animationSpec = tween(POSITION_PAD_ANIMATION_MS),
                    label = "positionPadTogglePadding",
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 10.dp,
                        )
                        .clickable(
                            // No ripple: the expand animation and the pad centering are feedback enough.
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onEvent(MainUIEvent.SetStylePositionPadExpanded(!positionPadExpanded)) }
                        .padding(
                            start = StyleContentInset,
                            end = StyleContentInset,
                            top = 10.dp,
                            bottom = toggleBottomPadding,
                        ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Subtitle(text = stringResource(R.string.font_options_position_subtitle))
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.font_options_position_toggle_desc),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(chevronRotation),
                        )
                    }
                    // Collapsed one-line summary; the pad's own readouts replace it once expanded.
                    AnimatedVisibility(
                        visible = !positionPadExpanded,
                        enter = expandVertically(animationSpec = tween(POSITION_PAD_ANIMATION_MS)) +
                            fadeIn(animationSpec = tween(POSITION_PAD_ANIMATION_MS)),
                        exit = shrinkVertically(animationSpec = tween(POSITION_PAD_ANIMATION_MS)) +
                            fadeOut(animationSpec = tween(POSITION_PAD_ANIMATION_MS)),
                    ) {
                        Text(
                            // H/V match the pad's own legend, deliberately not localized.
                            text = "${stringResource(R.string.font_options_position_horizontal)} " +
                                "${signedStep(timeoutIconStyle.iconStyleFontHorizontalSpacing)} · " +
                                "${stringResource(R.string.font_options_position_vertical)} " +
                                signedStep(timeoutIconStyle.iconStyleFontVerticalSpacing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }

                // Cap the pad surface so the whole pad always fits the worst-case viewport.
                val padMaxHeight = (viewportHeight - PadViewportAllowance).coerceAtLeast(PadMinHeight)

                val density = LocalDensity.current
                // Only read inside the coroutine below, so size changes never recompose.
                var padComponentSize by remember { mutableStateOf(IntSize.Zero) }
                val bringPadIntoViewRequester = remember { BringIntoViewRequester() }
                LaunchedEffect(positionPadExpanded) {
                    if (positionPadExpanded) {
                        // Wait out the expansion so the scroll targets the pad's final bounds.
                        delay(POSITION_PAD_ANIMATION_MS.milliseconds)
                        val padSize = padComponentSize
                        if (padSize == IntSize.Zero) {
                            bringPadIntoViewRequester.bringIntoView()
                        } else {
                            // Extend the requested rect below the pad so it settles around the
                            // viewport centre (readouts clear of the bottom bar/FAB); the default
                            // spec top-aligns when the rect exceeds the viewport, and the end of
                            // the list naturally bounds the lift.
                            val extraPx = ((with(density) { viewportHeight.toPx() } - padSize.height) / 2f)
                                .coerceAtLeast(0f)
                            bringPadIntoViewRequester.bringIntoView(
                                Rect(0f, 0f, padSize.width.toFloat(), padSize.height + extraPx)
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = positionPadExpanded,
                    enter = expandVertically(animationSpec = tween(POSITION_PAD_ANIMATION_MS)) +
                        fadeIn(animationSpec = tween(POSITION_PAD_ANIMATION_MS)),
                    exit = shrinkVertically(animationSpec = tween(POSITION_PAD_ANIMATION_MS)) +
                        fadeOut(animationSpec = tween(POSITION_PAD_ANIMATION_MS)),
                ) {
                    IconPositionPad(
                        horizontal = timeoutIconStyle.iconStyleFontHorizontalSpacing,
                        vertical = timeoutIconStyle.iconStyleFontVerticalSpacing,
                        maxPadHeight = padMaxHeight,
                        onPositionChange = { newHorizontal, newVertical ->
                            onEvent(
                                MainUIEvent.UpdateTimeoutIconStyle(
                                    timeoutIconStyle.copy(
                                        iconStyleFontHorizontalSpacing = newHorizontal,
                                        iconStyleFontVerticalSpacing = newVertical,
                                    )
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = StyleContentInset, end = StyleContentInset, bottom = 22.dp)
                            .onSizeChanged { padComponentSize = it }
                            .bringIntoViewRequester(bringPadIntoViewRequester),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontOptionSlider(
    label: String,
    valueText: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    topPadding: Dp = 12.dp,
    bottomPadding: Dp = 0.dp,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier.padding(
            start = StyleContentInset,
            end = StyleContentInset,
            top = topPadding,
            bottom = bottomPadding,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp)
                .alpha(if (enabled) 1f else DISABLED_CONTENT_ALPHA),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Subtitle(text = label)
            Subtitle(text = valueText)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { rawValue ->
                // roundToInt, not toInt: the snapped step values are approximate floats and
                // truncation would drop e.g. 2.9999998 to 2.
                onValueChange(rawValue.roundToInt())
            },
            steps = steps,
            valueRange = valueRange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            interactionSource = interactionSource,
            // Centered track: the default sits mid-range (0), so the fill grows from the
            // centre toward the thumb and the bar stays uncolored at the default.
            track = { _ ->
                CenterFilledSliderTrack(
                    value = value,
                    valueRange = valueRange,
                    steps = steps,
                    enabled = enabled,
                )
            },
            thumb = {
                Label(
                    label = {
                        PlainTooltip(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .wrapContentWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .wrapContentWidth(),
                                    text = valueText,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    },
                    interactionSource = interactionSource,
                ) {
                    SliderDefaults.Thumb(
                        interactionSource = interactionSource,
                        enabled = enabled,
                    )
                }
            }
        )
    }
}

// Centered-track geometry, mirroring the M3 expressive CenteredTrack metrics (that composable is
// internal in material3 1.4.0, so it is re-implemented here): 16dp bar, 6dp gap each side of the
// 4dp-wide thumb, 2dp corners on the cut edges, 4dp tick dots.
private val SliderTrackHeight = 16.dp
private val SliderThumbTrackGap = 6.dp
private val SliderThumbHalfWidth = 2.dp
private val SliderTrackInsideCornerRadius = 2.dp
private val SliderTickDiameter = 4.dp

/**
 * Slider track whose active fill grows from the **centre** of the range toward the thumb (both
 * directions), so a slider whose default sits mid-range shows no fill at the default. Drawn as the
 * full-width inactive bar split around the thumb gap, the active pill layered from the centre to the
 * thumb, and the step ticks on top (active-colored inside the fill), matching the M3 track styling.
 */
@Composable
private fun CenterFilledSliderTrack(
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SliderDefaults.colors()
    val activeColor = if (enabled) colors.activeTrackColor else colors.disabledActiveTrackColor
    val inactiveColor = if (enabled) colors.inactiveTrackColor else colors.disabledInactiveTrackColor
    val activeTickColor = if (enabled) colors.activeTickColor else colors.disabledActiveTickColor
    val inactiveTickColor = if (enabled) colors.inactiveTickColor else colors.disabledInactiveTickColor

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(SliderTrackHeight)
    ) {
        val width = size.width
        val height = size.height
        val outerRadius = CornerRadius(height / 2f)
        val insideRadius = CornerRadius(SliderTrackInsideCornerRadius.toPx())
        val gap = (SliderThumbHalfWidth + SliderThumbTrackGap).toPx()

        val rangeWidth = valueRange.endInclusive - valueRange.start
        val rawFraction = if (rangeWidth > 0f) (value - valueRange.start) / rangeWidth else 0.5f
        val fraction = if (layoutDirection == LayoutDirection.Ltr) rawFraction else 1f - rawFraction

        val thumbX = width * fraction
        val centerX = width / 2f

        fun drawTrackPiece(left: Float, right: Float, color: Color, leftRadius: CornerRadius, rightRadius: CornerRadius) {
            if (right - left <= 0f) return
            val piece = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = left,
                        top = 0f,
                        right = right,
                        bottom = height,
                        topLeftCornerRadius = leftRadius,
                        topRightCornerRadius = rightRadius,
                        bottomRightCornerRadius = rightRadius,
                        bottomLeftCornerRadius = leftRadius,
                    )
                )
            }
            drawPath(piece, color)
        }

        // Inactive bar, split around the thumb gap.
        drawTrackPiece(0f, thumbX - gap, inactiveColor, outerRadius, insideRadius)
        drawTrackPiece(thumbX + gap, width, inactiveColor, insideRadius, outerRadius)

        // Active fill from the centre to the thumb-gap edge, layered over the inactive bar; a pill
        // end at the centre, the inside corner toward the thumb. Nothing when the thumb is centred.
        if (thumbX > centerX + gap) {
            drawTrackPiece(centerX, thumbX - gap, activeColor, outerRadius, insideRadius)
        } else if (thumbX < centerX - gap) {
            drawTrackPiece(thumbX + gap, centerX, activeColor, insideRadius, outerRadius)
        }

        // Step ticks (ends included), skipping those hidden by the thumb gap; ticks within the
        // active fill flip to the contrasting active tick color. The tick row is inset by the
        // corner radius so the end dots sit centred inside the bar's rounded caps (as M3 does)
        // instead of straddling the curved edge.
        val tickRadius = SliderTickDiameter.toPx() / 2f
        val tickInset = height / 2f
        val activeLeft = minOf(centerX, thumbX)
        val activeRight = maxOf(centerX, thumbX)
        for (i in 0..steps + 1) {
            val tickX = tickInset + (width - 2f * tickInset) * i / (steps + 1f)
            if (tickX in (thumbX - gap)..(thumbX + gap)) continue
            val inActiveFill = tickX in activeLeft..activeRight
            drawCircle(
                color = if (inActiveFill) activeTickColor else inactiveTickColor,
                radius = tickRadius,
                center = Offset(tickX, height / 2f),
            )
        }
    }
}
