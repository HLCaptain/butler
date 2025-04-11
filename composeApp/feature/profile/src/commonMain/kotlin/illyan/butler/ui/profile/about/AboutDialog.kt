package illyan.butler.ui.profile.about

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import illyan.butler.config.BuildConfig
import illyan.butler.core.ui.components.ButlerCard
import illyan.butler.core.ui.components.ButlerCardDefaults
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.MediumMenuButton
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.about
import illyan.butler.generated.resources.app_description_brief
import illyan.butler.generated.resources.app_name
import illyan.butler.generated.resources.libraries
import illyan.butler.generated.resources.support_app
import illyan.butler.generated.resources.support_app_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutDialogContent(
    modifier: Modifier = Modifier,
    onNavigateToLibraries: (() -> Unit)? = null,
) {
    ButlerDialogContent(
        modifier = modifier,
        title = { AboutTitle() },
        textPaddingValues = PaddingValues(),
        text = { AboutScreen(onNavigateToLibraries = onNavigateToLibraries) },
        buttons = { AboutButtons() },
        containerColor = Color.Transparent,
    )
}

@Composable
fun AboutTitle(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(Res.string.about))
            Text(text = stringResource(Res.string.app_name))
        }
        Text(
            text = stringResource(Res.string.app_description_brief),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onNavigateToLibraries: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy((-12).dp)
        ) {
            AnimatedContent(targetState = onNavigateToLibraries) { onClick ->
                onClick?.let {
                    MediumMenuButton(
                        text = stringResource(Res.string.libraries),
                        onClick = it
                    )
                }
            }
        }
        // TODO: show main developers
        // Place new sections here
        DonationScreen(
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
fun DonationScreen(
    modifier: Modifier = Modifier,
) {
    ButlerCard(
        modifier = modifier,
        colors = ButlerCardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        contentPadding = ButlerCardDefaults.CompactContentPadding
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Rounded.VolunteerActivism, contentDescription = null)
            Text(text = stringResource(Res.string.support_app_description))
        }
    }
}

@Composable
fun AboutButtons(
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = "${BuildConfig.VERSION_NAME} v${BuildConfig.VERSION_CODE}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        ButlerMediumSolidButton(
            onClick = { uriHandler.openUri("https://ko-fi.com/illyan") }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = Icons.Rounded.Favorite, contentDescription = null)
                Text(text = stringResource(Res.string.support_app))
            }
        }
    }
}
