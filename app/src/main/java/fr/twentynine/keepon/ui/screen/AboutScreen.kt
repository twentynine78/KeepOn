package fr.twentynine.keepon.ui.screen

import android.content.Intent
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import fr.twentynine.keepon.R
import fr.twentynine.keepon.ui.model.CreditInfoUI
import fr.twentynine.keepon.ui.model.CreditSectionUI
import fr.twentynine.keepon.ui.model.ItemPosition
import fr.twentynine.keepon.domain.model.AppInfo
import fr.twentynine.keepon.ui.util.KeepOnNavigationType
import fr.twentynine.keepon.ui.util.bottomSpacerHeight
import fr.twentynine.keepon.ui.util.screenContentModifier
import fr.twentynine.keepon.ui.theme.KeepOnCardShape
import fr.twentynine.keepon.ui.component.CardHeader
import fr.twentynine.keepon.ui.component.ItemCard
import fr.twentynine.keepon.ui.component.Subtitle

/** About destination, stateful wrapper: passes the app info and credits through to [AboutScreen]. */
@Composable
fun AboutRoute(
    appInfo: AppInfo,
    creditSections: List<CreditSectionUI>,
    navType: KeepOnNavigationType,
    paddingValue: PaddingValues,
) {
    AboutScreen(
        appInfo = appInfo,
        creditSections = creditSections,
        navType = navType,
        paddingValue = paddingValue,
    )
}

/**
 * About destination content (stateless): the app header (version), and the third-party credits
 * grouped into sections, each opening its project URL.
 */
@Composable
fun AboutScreen(
    appInfo: AppInfo,
    creditSections: List<CreditSectionUI>,
    navType: KeepOnNavigationType,
    paddingValue: PaddingValues,
) {
    val maxWidthModifier = screenContentModifier

    LazyColumn(
        modifier = Modifier
            .padding(paddingValue)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "headerCard") {
            Column(
                modifier = maxWidthModifier
                    .padding(top = 28.dp),
            ) {
                CardHeader(
                    iconVector = Icons.Rounded.FavoriteBorder,
                    title = stringResource(R.string.credit_info_title),
                )
            }
        }

        creditSections.forEachIndexed { sectionIndex, section ->
            val topPadding = if (sectionIndex == 0) 0.dp else 12.dp

            item(key = "section_${section.typeName}") {
                Column(modifier = maxWidthModifier) {
                    Subtitle(
                        text = section.typeName,
                        modifier = Modifier
                            .padding(start = 32.dp, end = 32.dp, top = topPadding, bottom = 8.dp),
                    )
                }
            }

            itemsIndexed(
                items = section.credits,
                key = { _, credit -> "credit_${section.typeName}_${credit.url}" }
            ) { index, credit ->
                val itemPosition = remember(index, section.credits.size) {
                    ItemPosition.getItemPosition(index, section.credits.size)
                }

                CreditInfoCardRow(
                    itemPosition = itemPosition,
                    credit = credit,
                    modifier = maxWidthModifier,
                )
            }
        }

        item(key = "appInfoCard") {
            AppInfoCard(
                appInfo = appInfo,
                modifier = maxWidthModifier,
            )
        }

        item(key = "bottomSpacer") {
            val spacerBottomHeight = bottomSpacerHeight(navType)
            Spacer(modifier = Modifier.padding(bottom = spacerBottomHeight))
        }
    }
}

@Composable
fun AppInfoCard(
    appInfo: AppInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(top = 28.dp, bottom = 12.dp),
    ) {
        CardHeader(
            iconVector = Icons.Outlined.Info,
            title = stringResource(R.string.app_info_title),
        )
        Card(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
                .align(alignment = Alignment.Start),
            shape = KeepOnCardShape,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 12.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(R.string.app_info_version_text),
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(R.string.app_info_author_text),
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(R.string.app_info_source_code_text),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp, top = 12.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = appInfo.version,
                    )

                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = appInfo.author,
                    )

                    MarqueeUrlRow(url = appInfo.sourceCodeUrl)
                }
            }
        }
    }
}

@Composable
fun CreditInfoCardRow(
    credit: CreditInfoUI,
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
) {
    ItemCard(
        itemPosition = itemPosition,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(R.string.credit_info_name_text),
                    fontWeight = FontWeight.SemiBold,
                )

                credit.version?.let {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(R.string.app_info_version_text),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(R.string.credit_info_author_text),
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(R.string.credit_info_source_text),
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = credit.name,
                    fontWeight = FontWeight.SemiBold,
                )

                credit.version?.let { version ->
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = version,
                    )
                }

                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = credit.author,
                )

                MarqueeUrlRow(url = credit.url)
            }
        }
    }
}

/**
 * A URL that marquee-scrolls while focused (tapping the text focuses it), with a trailing icon that
 * opens it in the browser. Shared by the app-info card and the credit rows.
 */
@Composable
private fun MarqueeUrlRow(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .basicMarquee(animationMode = MarqueeAnimationMode.WhileFocused)
                .focusRequester(focusRequester)
                .focusable()
                .clickable { focusRequester.requestFocus() },
            text = url,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
            contentDescription = stringResource(R.string.about_open_link_text),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(16.dp)
                .clickable {
                    val webIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(webIntent)
                }
        )
    }
}
