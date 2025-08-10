package fr.twentynine.keepon.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.twentynine.keepon.R

@Composable
fun ErrorView(
    modifier: Modifier = Modifier,
    errorMessage: String,
) {
    val errorMessagePrefix = stringResource(R.string.error_view_message_prefix)
    val errorMessageWithPrefix = remember(errorMessage) {
        buildString {
            append(errorMessagePrefix)
            append(" ")
            append(errorMessage)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.padding(8.dp),
            imageVector = Icons.Rounded.Warning,
            contentDescription = stringResource(R.string.error_view_title),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(R.string.error_view_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(R.string.error_view_text),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.outline
        )

        Text(
            modifier = Modifier.padding(8.dp),
            text = errorMessageWithPrefix,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
