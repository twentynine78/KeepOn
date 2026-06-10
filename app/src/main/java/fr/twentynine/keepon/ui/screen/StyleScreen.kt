package fr.twentynine.keepon.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Label
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.model.IconTransitionOptionUI
import fr.twentynine.keepon.ui.model.ItemPosition
import fr.twentynine.keepon.ui.model.ScreenTimeoutUI
import fr.twentynine.keepon.domain.catalog.IconFontFamily
import fr.twentynine.keepon.ui.event.MainUIEvent
import fr.twentynine.keepon.ui.state.MainViewUIState
import fr.twentynine.keepon.domain.model.IconTransitionAnimation
import fr.twentynine.keepon.domain.model.IconTransitionTiming
import fr.twentynine.keepon.domain.model.TimeoutIconStyle
import fr.twentynine.keepon.domain.catalog.IconFontFamilyCatalog
import fr.twentynine.keepon.ui.util.KeepOnNavigationType
import fr.twentynine.keepon.ui.util.bottomSpacerHeight
import fr.twentynine.keepon.ui.util.defaultCardHorizontalPadding
import fr.twentynine.keepon.ui.util.screenContentModifier
import fr.twentynine.keepon.ui.theme.KeepOnCardShape
import fr.twentynine.keepon.ui.theme.StyleCardTopPadding
import fr.twentynine.keepon.ui.theme.StyleContentInset
import fr.twentynine.keepon.ui.theme.StyleListRowVerticalPadding
import fr.twentynine.keepon.ui.theme.StyleRadioGlyphInset
import fr.twentynine.keepon.ui.theme.StyleSwitchRowVerticalPadding
import fr.twentynine.keepon.ui.theme.StyleTopSwitchRowVerticalPadding
import fr.twentynine.keepon.ui.component.CardHeader
import fr.twentynine.keepon.ui.component.GhostSizedText
import fr.twentynine.keepon.ui.component.rememberBehaviorSwitchLabel
import fr.twentynine.keepon.ui.component.ItemCard
import fr.twentynine.keepon.ui.component.LabeledControlRow
import fr.twentynine.keepon.ui.component.SegmentedCheckboxGroup
import fr.twentynine.keepon.ui.component.SegmentedCheckboxOption
import fr.twentynine.keepon.ui.component.Subtitle
import kotlin.math.ceil

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
        defaultScreenTimeoutUI = remember(uiState.screenTimeouts) {
            uiState.screenTimeouts.firstOrNull { it.isDefault } ?: uiState.screenTimeouts.first()
        },
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
    defaultScreenTimeoutUI: ScreenTimeoutUI,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType,
    paddingValue: PaddingValues,
) {
    val fontFamilies = remember {
        IconFontFamilyCatalog.iconFontFamilies.values.toList()
    }

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
                defaultScreenTimeoutUI = defaultScreenTimeoutUI,
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
                onEvent = onEvent,
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

@Composable
fun IconTransitionAnimationCard(
    iconTransitionAnimation: IconTransitionAnimation,
    iconTransitionOptions: List<IconTransitionOptionUI>,
    defaultScreenTimeoutUI: ScreenTimeoutUI,
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
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LabeledControlRow(
                    onClick = {
                        onEvent(
                            MainUIEvent.UpdateIconTransitionAnimation(
                                iconTransitionAnimation.copy(enabled = !iconTransitionAnimation.enabled)
                            )
                        )
                    },
                    verticalPadding = StyleTopSwitchRowVerticalPadding,
                    leading = {
                        Switch(
                            checked = iconTransitionAnimation.enabled,
                            onCheckedChange = null,
                        )
                    },
                    label = {
                        // The Home behavior switch label rides as a ghost so this row and the Home
                        // one resolve to the same height and the two switches align across screens.
                        // It must use the same builder (bold span included) so the reserved size matches.
                        GhostSizedText(
                            text = AnnotatedString(stringResource(R.string.icon_transition_enable)),
                            ghostTexts = listOf(
                                rememberBehaviorSwitchLabel(defaultScreenTimeoutUI.displayName)
                            ),
                        )
                    },
                )

                val animationsEnabled = iconTransitionAnimation.enabled
                Subtitle(
                    text = stringResource(R.string.icon_transition_type_subtitle),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 4.dp, start = StyleContentInset, end = StyleContentInset)
                        .alpha(if (animationsEnabled) 1f else DISABLED_CONTENT_ALPHA),
                )
                iconTransitionOptions.forEach { option ->
                    LabeledControlRow(
                        onClick = {
                            onEvent(
                                MainUIEvent.UpdateIconTransitionAnimation(
                                    iconTransitionAnimation.copy(typeId = option.id)
                                )
                            )
                        },
                        enabled = animationsEnabled,
                        leadingGlyphInset = StyleRadioGlyphInset,
                        leading = {
                            RadioButton(
                                selected = option.id == iconTransitionAnimation.typeId,
                                onClick = null,
                                enabled = animationsEnabled,
                            )
                        },
                        label = {
                            Text(
                                modifier = Modifier.alpha(if (animationsEnabled) 1f else DISABLED_CONTENT_ALPHA),
                                text = option.label,
                            )
                        },
                    )
                }

                FontOptionSlider(
                    label = stringResource(R.string.icon_transition_duration),
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

    ItemCard(modifier = modifier, itemPosition = itemPosition) {
        LabeledControlRow(
            onClick = onRowClick,
            verticalPadding = StyleListRowVerticalPadding,
            leadingGlyphInset = StyleRadioGlyphInset,
            leading = {
                RadioButton(
                    selected = iconFontFamily.name == timeoutIconStyle.iconFontFamilyName,
                    onClick = null,
                )
            },
            label = {
                Text(
                    text = iconFontFamily.displayName,
                    fontFamily = fontFamily,
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
                    .padding(top = 8.dp, bottom = 16.dp),
            ) {
                Subtitle(
                    text = stringResource(R.string.font_style_parameters_subtitle),
                    modifier = Modifier
                        .padding(horizontal = StyleContentInset, vertical = 8.dp)
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

                LabeledControlRow(
                    onClick = { onOutlinedChange(!timeoutIconStyle.iconStyleTextOutlined) },
                    verticalPadding = StyleSwitchRowVerticalPadding,
                    leading = {
                        Switch(
                            checked = timeoutIconStyle.iconStyleTextOutlined,
                            onCheckedChange = null,
                        )
                    },
                    label = {
                        Text(text = stringResource(R.string.font_style_parameters_appearance_outlined))
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontOptionsCard(
    timeoutIconStyle: TimeoutIconStyle,
    onEvent: (MainUIEvent) -> Unit,
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
            val nbStep = remember { 10f }
            val range = remember(nbStep) { ceil(nbStep / 2).unaryMinus()..ceil(nbStep / 2) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                FontOptionSlider(
                    label = stringResource(R.string.font_options_size_subtitle),
                    value = timeoutIconStyle.iconStyleFontSize,
                    onValueChange = { newValue ->
                        onEvent(
                            MainUIEvent.UpdateTimeoutIconStyle(
                                timeoutIconStyle.copy(iconStyleFontSize = newValue)
                            )
                        )
                    },
                    valueRange = range,
                    steps = nbStep.toInt() - 1,
                    topPadding = 0.dp
                )

                FontOptionSlider(
                    label = stringResource(R.string.font_options_horizontal_position_subtitle),
                    value = timeoutIconStyle.iconStyleFontHorizontalSpacing,
                    onValueChange = { newValue ->
                        onEvent(
                            MainUIEvent.UpdateTimeoutIconStyle(
                                timeoutIconStyle.copy(iconStyleFontHorizontalSpacing = newValue)
                            )
                        )
                    },
                    valueRange = range,
                    steps = nbStep.toInt() - 1
                )

                FontOptionSlider(
                    label = stringResource(R.string.font_options_vertical_position_subtitle),
                    value = timeoutIconStyle.iconStyleFontVerticalSpacing,
                    onValueChange = { newValue ->
                        onEvent(
                            MainUIEvent.UpdateTimeoutIconStyle(
                                timeoutIconStyle.copy(iconStyleFontVerticalSpacing = newValue)
                            )
                        )
                    },
                    valueRange = range,
                    steps = nbStep.toInt() - 1,
                    bottomPadding = 8.dp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontOptionSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    topPadding: androidx.compose.ui.unit.Dp = 12.dp,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
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
        Subtitle(
            text = label,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 24.dp)
                .alpha(if (enabled) 1f else DISABLED_CONTENT_ALPHA),
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { rawValue ->
                onValueChange(rawValue.toInt())
            },
            steps = steps,
            valueRange = valueRange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            interactionSource = interactionSource,
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
                                    text = value.toString(),
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
