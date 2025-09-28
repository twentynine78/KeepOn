package fr.twentynine.keepon.ui.view

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.R
import fr.twentynine.keepon.data.enums.ItemPosition
import fr.twentynine.keepon.data.local.IconFontFamily
import fr.twentynine.keepon.data.model.MainUIEvent
import fr.twentynine.keepon.data.model.MainViewUIState
import fr.twentynine.keepon.data.model.TimeoutIconStyle
import fr.twentynine.keepon.data.repo.IconFontFamilyRepository
import fr.twentynine.keepon.ui.util.KeepOnNavigationType
import fr.twentynine.keepon.ui.util.MAX_SCREEN_CONTENT_WIDTH_IN_DP
import kotlin.math.ceil

@Composable
fun StyleView(
    uiState: MainViewUIState.Success,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType,
    paddingValue: PaddingValues,
) {
    StyleScreen(
        timeoutIconStyle = uiState.timeoutIconStyle,
        onEvent = onEvent,
        navType = navType,
        paddingValue = paddingValue,
    )
}

@Composable
fun StyleScreen(
    timeoutIconStyle: TimeoutIconStyle,
    onEvent: (MainUIEvent) -> Unit,
    navType: KeepOnNavigationType,
    paddingValue: PaddingValues,
) {
    val fontFamilies = remember {
        IconFontFamilyRepository.iconFontFamilies.values.toList()
    }

    LazyColumn(
        modifier = Modifier
            .padding(paddingValue)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val maxWidthModifier = Modifier
            .fillMaxHeight()
            .width(MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp)

        item(key = "headerCard") {
            Column(
                modifier = maxWidthModifier
                    .padding(top = 28.dp),
            ) {
                CardHeaderView(
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
            val spacerBottomHeight = remember(navType) {
                when (navType) {
                    KeepOnNavigationType.BOTTOM_NAVIGATION -> 112.dp
                    else -> 12.dp
                }
            }
            Spacer(modifier = Modifier.padding(bottom = spacerBottomHeight))
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

    val onRowClick = remember(iconFontFamily.name) {
        {
            onEvent(
                MainUIEvent.UpdateTimeoutIconStyle(
                    timeoutIconStyle.copy(iconFontFamilyName = iconFontFamily.name)
                )
            )
        }
    }

    ItemCardView(modifier = modifier, itemPosition = itemPosition) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRowClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = iconFontFamily.name == timeoutIconStyle.iconFontFamilyName,
                onClick = null
            )
            Text(
                modifier = Modifier.padding(horizontal = 42.dp),
                text = iconFontFamily.displayName,
                fontFamily = fontFamily
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
            .padding(top = 28.dp, bottom = 12.dp),
    ) {
        CardHeaderView(
            iconVector = Icons.Rounded.FormatColorText,
            title = stringResource(R.string.font_style_tile),
            descText = stringResource(R.string.font_style_text),
        )
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(alignment = Alignment.Start),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.font_style_parameters_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Start),
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    StyleCheckboxRow(
                        text = stringResource(R.string.font_style_parameters_bold),
                        checked = timeoutIconStyle.iconStyleFontBold,
                        onCheckedChange = { onBoldChange(!timeoutIconStyle.iconStyleFontBold) }
                    )
                    StyleCheckboxRow(
                        text = stringResource(R.string.font_style_parameters_italic),
                        checked = timeoutIconStyle.iconStyleFontItalic,
                        onCheckedChange = { onItalicChange(!timeoutIconStyle.iconStyleFontItalic) }
                    )
                    StyleCheckboxRow(
                        text = stringResource(R.string.font_style_parameters_underline),
                        checked = timeoutIconStyle.iconStyleFontUnderline,
                        onCheckedChange = { onUnderlineChange(!timeoutIconStyle.iconStyleFontUnderline) }
                    )
                }

                Text(
                    text = stringResource(R.string.font_style_parameters_appearance_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp, end = 8.dp, top = 12.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { onOutlinedChange(!timeoutIconStyle.iconStyleTextOutlined) })
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = timeoutIconStyle.iconStyleTextOutlined,
                        onCheckedChange = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 18.dp),
                        text = stringResource(R.string.font_style_parameters_appearance_outlined)
                    )
                }
            }
        }
    }
}

@Composable
private fun StyleCheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp)
        )
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
            .padding(top = 28.dp, bottom = 12.dp),
    ) {
        CardHeaderView(
            iconVector = Icons.Rounded.LocationSearching,
            title = stringResource(R.string.font_options_title),
            descText = stringResource(R.string.font_options_text),
        )
        Card(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
                .align(alignment = Alignment.Start),
            shape = RoundedCornerShape(24.dp),
        ) {
            val nbStep = remember { 10f }
            val range = remember(nbStep) { ceil(nbStep / 2).unaryMinus()..ceil(nbStep / 2) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
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
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = Modifier.padding(top = topPadding, bottom = bottomPadding)) {
        Text(
            text = label,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { rawValue ->
                onValueChange(rawValue.toInt())
            },
            steps = steps,
            valueRange = valueRange,
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
                        interactionSource = interactionSource
                    )
                }
            }
        )
    }
}
