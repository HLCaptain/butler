package illyan.butler.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom dialog content styling used (based on Material 3 AlertDialogContent)
 */
@Composable
fun ButlerDialogContent(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier.heightIn(max = 350.dp),
    icon: @Composable (ColumnScope.() -> Unit)? = null,
    title: @Composable (ColumnScope.() -> Unit)? = null,
    text: @Composable (ColumnScope.() -> Unit)? = null,
    buttons: @Composable (ColumnScope.() -> Unit)? = null,
    dialogPaddingValues: PaddingValues = ButlerDialogContentPadding,
    iconPaddingValues: PaddingValues = ButlerDialogIconPadding,
    titlePaddingValues: PaddingValues = ButlerDialogTitlePadding,
    textPaddingValues: PaddingValues = ButlerDialogTextPadding,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    buttonContentColor: Color = MaterialTheme.colorScheme.primary,
) {
    ButlerDialogContentHolder(
        modifier = modifier,
        surface = {
            ButlerDialogSurface(
                modifier = modifier,
                shape = shape,
                color = containerColor,
                tonalElevation = tonalElevation,
                content = it
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(dialogPaddingValues)
        ) {
            AnimatedVisibility(
                modifier = Modifier
                    .padding(iconPaddingValues)
                    .align(Alignment.CenterHorizontally),
                visible = icon != null
            ) {
                icon?.let {
                    CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                        icon()
                    }
                }
            }
            AnimatedVisibility(
                modifier = Modifier
                    .align(if (icon == null) {
                        Alignment.Start
                    } else {
                        Alignment.CenterHorizontally
                    })
                    .padding(titlePaddingValues),
                visible = title != null
            ) {
                title?.let {
                    CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                        val textStyle = MaterialTheme.typography.headlineSmall
                        ProvideTextStyle(textStyle) {
                            title()
                        }
                    }
                }
            }
            AnimatedVisibility(
                modifier = Modifier.padding(textPaddingValues),
                visible = text != null
            ) {
                text?.let {
                    CompositionLocalProvider(LocalContentColor provides textContentColor) {
                        val textStyle = MaterialTheme.typography.bodyMedium
                        ProvideTextStyle(textStyle) {
                            text()
                        }
                    }
                }
            }
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.End),
                visible = buttons != null
            ) {
                buttons?.let {
                    CompositionLocalProvider(LocalContentColor provides buttonContentColor) {
                        val textStyle = MaterialTheme.typography.labelLarge
                        ProvideTextStyle(value = textStyle) {
                            buttons()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ButlerDialogSurface(
    modifier: Modifier = Modifier,
    shape: Shape = AlertDialogDefaults.shape,
    color: Color = AlertDialogDefaults.containerColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    content: @Composable () -> Unit
) {
    // Increase width to edge of the screen until reaching DialogMaxWidth
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        tonalElevation = tonalElevation,
        content = content
    )
}

@Composable
fun ButlerDialogContentHolder(
    modifier: Modifier = Modifier,
    surface: @Composable (@Composable () -> Unit) -> Unit = {
        ButlerDialogSurface(
            modifier = modifier,
            content = it
        )
    },
    content: @Composable () -> Unit = {},
) = surface(content)

val DialogMinWidth = 280.dp
val DialogMaxWidth = 800.dp
val DialogMargin = 64.dp

fun Modifier.dialogWidth(
    screenWidthDp: Dp = 400.dp,
    min: Dp = DialogMinWidth,
    max: Dp = DialogMaxWidth,
    margin: Dp = DialogMargin,
) = widthIn(
    min = min,
    max = minOf(maxOf(min, screenWidthDp - margin), max)
)

fun Modifier.smallDialogWidth() = dialogWidth(max = 320.dp)
fun Modifier.mediumDialogWidth() = dialogWidth(max = 360.dp)
fun Modifier.largeDialogWidth() = dialogWidth(max = DialogMaxWidth)

val DialogMinHeight = 200.dp
val DialogMaxHeight = 800.dp

fun Modifier.dialogHeight(
    screenHeightDp: Dp = 400.dp,
    min: Dp = DialogMinHeight,
    max: Dp = DialogMaxHeight,
    margin: Dp = DialogMargin,
) = heightIn(
    min = min,
    max = minOf(maxOf(min, screenHeightDp - margin), max)
)

fun Modifier.smallDialogHeight() = dialogHeight(max = 280.dp)
fun Modifier.mediumDialogHeight() = dialogHeight(max = 400.dp)
fun Modifier.largeDialogHeight() = dialogHeight(max = DialogMaxHeight)

fun Modifier.dialogSize(
    screenWidthDp: Dp = 400.dp,
    screenHeightDp: Dp = 400.dp,
    minWidth: Dp = DialogMinWidth,
    maxWidth: Dp = DialogMaxWidth,
    minHeight: Dp = DialogMinHeight,
    maxHeight: Dp = DialogMaxHeight,
    margin: Dp = DialogMargin,
) = dialogWidth(screenWidthDp, minWidth, maxWidth, margin).dialogHeight(screenHeightDp, minHeight, maxHeight, margin)

fun Modifier.smallDialogSize() = smallDialogWidth().smallDialogHeight()
fun Modifier.mediumDialogSize() = mediumDialogWidth().mediumDialogHeight()
fun Modifier.largeDialogSize() = largeDialogWidth().largeDialogHeight()

// Paddings for each of the dialog's parts.
val ButlerDialogContentPadding = PaddingValues(all = 24.dp)
val ButlerDialogIconPadding = PaddingValues(bottom = 16.dp)
val ButlerDialogTitlePadding = PaddingValues(bottom = 16.dp)
val ButlerDialogTextPadding = PaddingValues(bottom = 16.dp)
