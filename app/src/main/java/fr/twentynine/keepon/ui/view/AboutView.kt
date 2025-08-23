package fr.twentynine.keepon.ui.view

import android.content.Intent
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import fr.twentynine.keepon.data.enums.CreditInfoType
import fr.twentynine.keepon.data.enums.ItemPosition
import fr.twentynine.keepon.data.local.CreditInfo // Assuming CreditInfo has a stable 'id' or unique property
import fr.twentynine.keepon.data.model.AppInfo
import fr.twentynine.keepon.data.repo.AppInfoRepository
import fr.twentynine.keepon.data.repo.CreditInfoRepository
import fr.twentynine.keepon.ui.util.KeepOnNavigationType
import fr.twentynine.keepon.ui.util.MAX_SCREEN_CONTENT_WIDTH_IN_DP

@Composable
fun AboutView(
    navType: KeepOnNavigationType,
) {
    val context = LocalContext.current
    val appInfo = remember {
        AppInfoRepository().getKeepOnAppInfo(context)
    }
    val creditInfoMap = remember {
        CreditInfoRepository.creditInfoMap
    }

    AboutScreen(
        appInfo = appInfo,
        creditInfoMap = creditInfoMap,
        navType = navType,
    )
}

@Composable
fun AboutScreen(
    appInfo: AppInfo,
    creditInfoMap: Map<CreditInfoType, List<CreditInfo>>,
    navType: KeepOnNavigationType,
) {
    val maxWidthModifier = remember {
        Modifier
            .fillMaxHeight()
            .width(MAX_SCREEN_CONTENT_WIDTH_IN_DP.dp)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = maxWidthModifier
                    .padding(top = 28.dp),
            ) {
                CardHeaderView(
                    iconVector = Icons.Rounded.FavoriteBorder,
                    title = stringResource(R.string.credit_info_title),
                )
            }
        }

        creditInfoMap.forEach { (creditInfoType, creditInfoForType) ->
            item(key = creditInfoType) {
                Column(modifier = maxWidthModifier) {
                    Text(
                        text = stringResource(creditInfoType.typeNameId),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier
                            .padding(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 8.dp),
                    )
                }
            }

            itemsIndexed(
                items = creditInfoForType,
                key = { _, creditInfo -> creditInfo.url }
            ) { index, creditInfo ->
                val itemPosition = remember(index, creditInfoForType.size) {
                    ItemPosition.getItemPosition(index, creditInfoForType.size)
                }

                CreditInfoCardRow(
                    itemPosition = itemPosition,
                    creditInfo = creditInfo,
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
fun AppInfoCard(
    appInfo: AppInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(top = 28.dp, bottom = 12.dp),
    ) {
        CardHeaderView(
            iconVector = Icons.Outlined.Info,
            title = stringResource(R.string.app_info_title),
        )
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .align(alignment = Alignment.Start),
            shape = RoundedCornerShape(24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, top = 16.dp, bottom = 16.dp),
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
                        .padding(end = 8.dp, top = 16.dp, bottom = 16.dp),
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val context = LocalContext.current
                        val focusRequester = remember { FocusRequester() }
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .basicMarquee(animationMode = MarqueeAnimationMode.WhileFocused)
                                .focusRequester(focusRequester)
                                .focusable()
                                .clickable { focusRequester.requestFocus() },
                            text = appInfo.sourceCodeUrl,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                            contentDescription = stringResource(R.string.about_open_link_text),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(16.dp)
                                .clickable {
                                    val webIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        appInfo.sourceCodeUrl.toUri()
                                    )
                                    context.startActivity(webIntent)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreditInfoCardRow(
    creditInfo: CreditInfo,
    itemPosition: ItemPosition,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    ItemCardView(
        itemPosition = itemPosition,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(R.string.credit_info_name_text),
                    fontWeight = FontWeight.SemiBold,
                )

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
                    text = creditInfo.name,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = creditInfo.author,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val focusRequester = remember { FocusRequester() }
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .basicMarquee(animationMode = MarqueeAnimationMode.WhileFocused)
                            .focusRequester(focusRequester)
                            .focusable()
                            .clickable { focusRequester.requestFocus() },
                        text = creditInfo.url,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                        contentDescription = stringResource(R.string.about_open_link_text),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(16.dp)
                            .clickable {
                                val webIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    creditInfo.url.toUri()
                                )
                                context.startActivity(webIntent)
                            }
                    )
                }
            }
        }
    }
}
