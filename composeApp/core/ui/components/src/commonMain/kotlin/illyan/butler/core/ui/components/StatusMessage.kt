package illyan.butler.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.utils.ensureContrastWith

@Composable
fun ButlerWarningStatusMessage(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) = ButlerStatusMessage(
    modifier = modifier,
    colors = ButlerStatusMessageDefaults.statusMessageWarningColors(),
    imageVector = ButlerStatusMessageDefaults.WarningImageVector,
    title = title,
    description = description,
    actions = actions
)

@Composable
fun ButlerStatusMessage(
    modifier: Modifier = Modifier,
    colors: StatusMessageColors,
    imageVector: ImageVector,
    title: @Composable () -> Unit,
    border: BorderStroke? = ButlerStatusMessageDefaults.statusMessageBorder(colors),
    onClose: (() -> Unit)? = null,
    description: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) = ButlerStatusMessage(
    modifier = modifier,
    colors = colors,
    border = border,
    onClose = onClose,
    icon = { ButlerStatusMessageDefaults.StatusMessageIcon(imageVector = imageVector) },
    title = { ProvideTextStyle(ButlerStatusMessageDefaults.titleStyle) { title() } },
    description = description?.let { { ProvideTextStyle(ButlerStatusMessageDefaults.descriptionStyle) { it.invoke() } } },
    actions = actions
)

@Composable
fun ButlerStatusMessage(
    modifier: Modifier = Modifier,
    colors: StatusMessageColors,
    painter: Painter,
    title: @Composable () -> Unit,
    border: BorderStroke? = ButlerStatusMessageDefaults.statusMessageBorder(colors),
    onClose: (() -> Unit)? = null,
    description: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) = ButlerStatusMessage(
    modifier = modifier,
    colors = colors,
    border = border,
    onClose = onClose,
    icon = { ButlerStatusMessageDefaults.StatusMessageIcon(painter = painter) },
    title = { ProvideTextStyle(ButlerStatusMessageDefaults.titleStyle) { title() } },
    description = description?.let { { ProvideTextStyle(ButlerStatusMessageDefaults.descriptionStyle) { it.invoke() } } },
    actions = actions
)

@Composable
fun ButlerStatusMessage(
    modifier: Modifier = Modifier,
    colors: StatusMessageColors,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    border: BorderStroke? = ButlerStatusMessageDefaults.statusMessageBorder(colors),
    onClose: (() -> Unit)? = null,
    description: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    val content: @Composable ColumnScope.() -> Unit = {
        Box {
            Column(
                modifier = Modifier
                    .animateContentSize()
                    .padding(ButlerCardDefaults.ContentPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CompositionLocalProvider(LocalContentColor provides colors.primaryColor) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ButlerStatusMessageDefaults.TitlePadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        icon()
                        title()
                    }
                }
                AnimatedVisibility(visible = description != null) {
                    Column(
                        Modifier.padding(start = ButlerStatusMessageDefaults.DescriptionStartPadding)
                    ) { description?.invoke() }
                }
                AnimatedVisibility(visible = actions != null) {
                    CompositionLocalProvider(LocalContentColor provides colors.primaryColor) {
                        Column(
                            Modifier.padding(start = ButlerStatusMessageDefaults.DescriptionStartPadding)
                        ) { actions?.invoke() }
                    }
                }
            }
            CompositionLocalProvider(LocalContentColor provides colors.primaryColor) {
                ButlerStatusMessageDefaults.CloseIconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClose = onClose
                )
            }
        }
    }
    if (border != null) {
        ButlerOutlinedCard(
            modifier = modifier,
            border = border,
            colors = CardDefaults.elevatedCardColors().copy(
                containerColor = colors.containerColor,
                contentColor = colors.contentColor
            ),
            content = content,
            contentPadding = PaddingValues(0.dp)
        )
    } else {
        ButlerCard(
            modifier = modifier,
            colors = CardDefaults.elevatedCardColors().copy(
                containerColor = colors.containerColor,
                contentColor = colors.contentColor
            ),
            content = content,
            contentPadding = PaddingValues(0.dp)
        )
    }
}

@Composable
fun ButlerInvertedStatusMessage(
    modifier: Modifier = Modifier,
    colors: StatusMessageColors,
    imageVector: ImageVector,
    title: @Composable () -> Unit,
    onClose: (() -> Unit)? = null,
    description: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) = ButlerInvertedStatusMessage(
    modifier = modifier,
    colors = colors,
    icon = { ButlerStatusMessageDefaults.StatusMessageIcon(imageVector = imageVector) },
    title = { ProvideTextStyle(ButlerStatusMessageDefaults.titleStyle) { title() } },
    onClose = onClose,
    description = description,
    actions = actions
)

@Composable
fun ButlerInvertedStatusMessage(
    modifier: Modifier = Modifier,
    colors: StatusMessageColors,
    painter: Painter,
    title: @Composable () -> Unit,
    onClose: (() -> Unit)? = null,
    description: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) = ButlerInvertedStatusMessage(
    modifier = modifier,
    colors = colors,
    icon = { ButlerStatusMessageDefaults.StatusMessageIcon(painter = painter) },
    title = { ProvideTextStyle(ButlerStatusMessageDefaults.titleStyle) { title() } },
    onClose = onClose,
    description = description,
    actions = actions
)

@Composable
fun ButlerInvertedStatusMessage(
    modifier: Modifier = Modifier,
    colors: StatusMessageColors,
    onClose: (() -> Unit)? = null,
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    description: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) = ButlerCard(
    modifier = modifier,
    colors = CardDefaults.elevatedCardColors().copy(
        containerColor = colors.containerColor,
        contentColor = colors.contentColor
    ),
    contentPadding = PaddingValues(0.dp)
) {
    Box {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(ButlerCardDefaults.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ButlerStatusMessageDefaults.TitlePadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                title()
            }
            AnimatedVisibility(visible = description != null) {
                Column(
                    Modifier.padding(start = ButlerStatusMessageDefaults.DescriptionStartPadding)
                ) { description?.invoke() }
            }
            AnimatedVisibility(visible = actions != null) {
                Column(
                    Modifier.padding(start = ButlerStatusMessageDefaults.DescriptionStartPadding)
                ) { actions?.invoke() }
            }
        }
        CompositionLocalProvider(LocalContentColor provides colors.primaryColor) {
            ButlerStatusMessageDefaults.CloseIconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClose = onClose
            )
        }
    }
}

object ButlerStatusMessageDefaults {
    val IconSize = 24.dp
    val TitlePadding = 8.dp
    val DescriptionStartPadding get() = IconSize + TitlePadding

    val titleStyle @Composable get() = MaterialTheme.typography.titleMedium
    val descriptionStyle @Composable get() = MaterialTheme.typography.bodyMedium

    @Composable
    fun CloseIconButton(
        modifier: Modifier = Modifier,
        onClose: (() -> Unit)? = null
    ) {
        AnimatedVisibility(
            modifier = modifier,
            visible = onClose != null
        ) {
            IconButton(onClick = onClose ?: {}) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
            }
        }
    }

    @Composable
    fun StatusMessageIcon(
        modifier: Modifier = Modifier,
        imageVector: ImageVector,
        contentDescription: String? = null,
        tint: Color = LocalContentColor.current
    ) {
        Icon(
            modifier = modifier.size(IconSize),
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint
        )
    }

    @Composable
    fun StatusMessageIcon(
        modifier: Modifier = Modifier,
        painter: Painter,
        contentDescription: String? = null,
        tint: Color = LocalContentColor.current
    ) {
        Icon(
            modifier = modifier.size(IconSize),
            painter = painter,
            contentDescription = contentDescription,
            tint = tint
        )
    }

    val WarningImageVector = Icons.Rounded.Warning

    @Composable
    fun WarningIcon() = StatusMessageIcon(
        imageVector = WarningImageVector,
        contentDescription = "Warning"
    )

    @Composable
    fun StatusMessageButtons(
        colors: StatusMessageColors,
        primaryButtonText: String,
        onPrimaryClick: () -> Unit,
        secondaryButtonText: String? = null,
        onSecondaryClick: (() -> Unit)? = null,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ButlerMediumSolidButton(
                onClick = onPrimaryClick,
                colors = ButlerButtonDefaults.solidButtonColors(
                    containerColor = colors.primaryColor,
                    contentColor = colors.containerColor
                )
            ) { Text(primaryButtonText) }
            AnimatedVisibility(onSecondaryClick != null && secondaryButtonText != null) {
                ButlerMediumTextButton(
                    onClick = onSecondaryClick ?: {},
                    colors = ButlerButtonDefaults.textButtonColors(
                        contentColor = colors.primaryColor
                    ),
                    textDecoration = TextDecoration.Underline
                ) { Text(secondaryButtonText ?: "") }
            }
        }
    }

    @Composable
    fun InvertedStatusMessageButtons(
        colors: StatusMessageColors,
        primaryButtonText: String,
        onPrimaryClick: () -> Unit,
        secondaryButtonText: String? = null,
        onSecondaryClick: (() -> Unit)? = null,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ButlerMediumSolidButton(
                onClick = onPrimaryClick,
                colors = ButlerButtonDefaults.solidButtonInvertedColors(
                    contentColor = colors.containerColor,
                    containerColor = colors.primaryColor
                )
            ) { Text(primaryButtonText) }
            AnimatedVisibility(onSecondaryClick != null && secondaryButtonText != null) {

                ButlerMediumTextButton(
                    onClick = onSecondaryClick ?: {},
                    colors = ButlerButtonDefaults.textButtonInvertedColors(
                        contentColor = colors.primaryColor,
                    ),
                    textDecoration = TextDecoration.Underline
                ) { Text(secondaryButtonText ?: "") }
            }
        }
    }

    @Composable
    fun statusMessageBorder(colors: StatusMessageColors) = BorderStroke(
        width = 1.dp,
        brush = SolidColor(colors.primaryColor)
    )

    @Composable
    fun statusMessageColors(
        primaryColor: Color,
        containerColor: Color
    ) = StatusMessageColors(
        primaryColor = primaryColor,
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onBackground
    )

    @Composable
    fun statusMessagePrimaryColors() = statusMessageColors(
        primaryColor = MaterialTheme.colorScheme.primary,
        containerColor = MaterialTheme.colorScheme.primaryContainer.ensureContrastWith(MaterialTheme.colorScheme.primary)
    )

    @Composable
    fun statusMessageSecondaryColors() = statusMessageColors(
        primaryColor = MaterialTheme.colorScheme.secondary,
        containerColor = MaterialTheme.colorScheme.secondaryContainer.ensureContrastWith(MaterialTheme.colorScheme.secondary)
    )

    @Composable
    fun statusMessageGrayColors() = statusMessageColors(
        primaryColor = MaterialTheme.colorScheme.onBackground,
        containerColor = MaterialTheme.colorScheme.background
    )

    @Composable
    fun statusMessageWarningColors() = statusMessageColors(
        primaryColor = MaterialTheme.colorScheme.error,
        containerColor = MaterialTheme.colorScheme.errorContainer
    )

    @Composable
    fun invertedStatusMessageColors(
        containerColor: Color = MaterialTheme.colorScheme.primary
    ) = StatusMessageColors(
        primaryColor = MaterialTheme.colorScheme.background,
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.background
    )

    @Composable
    fun invertedStatusMessagePrimaryColors() = invertedStatusMessageColors(containerColor = MaterialTheme.colorScheme.primary)

    @Composable
    fun invertedStatusMessageSecondaryColors() = invertedStatusMessageColors(containerColor = MaterialTheme.colorScheme.secondary)

    @Composable
    fun invertedStatusMessageWarningColors() = invertedStatusMessageColors(containerColor = MaterialTheme.colorScheme.error)

    @Composable
    fun invertedStatusMessageGrayColors() = invertedStatusMessageColors(containerColor = MaterialTheme.colorScheme.onSurface)
}

data class StatusMessageColors(
    val primaryColor: Color,
    val containerColor: Color,
    val contentColor: Color
)


