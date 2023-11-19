package illyan.butler.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
    icon: @Composable (BoxScope.() -> Unit)? = null,
    title: @Composable (BoxScope.() -> Unit)? = null,
    text: @Composable (BoxScope.() -> Unit)? = null,
    buttons: @Composable (BoxScope.() -> Unit)? = null,
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
    ButlerDialogContent(
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
            AnimatedVisibility(visible = icon != null) {
                icon?.let {
                    CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(iconPaddingValues)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            icon()
                        }
                    }
                }
            }
            AnimatedVisibility(visible = title != null) {
                title?.let {
                    CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                        val textStyle = MaterialTheme.typography.headlineSmall
                        ProvideTextStyle(textStyle) {
                            Box(
                                // Align the title to the center when an icon is present.
                                Modifier
                                    .fillMaxWidth()
                                    .padding(titlePaddingValues)
                                    .align(
                                        if (icon == null) {
                                            Alignment.Start
                                        } else {
                                            Alignment.CenterHorizontally
                                        }
                                    )
                            ) {
                                title()
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(visible = text != null) {
                text?.let {
                    CompositionLocalProvider(LocalContentColor provides textContentColor) {
                        val textStyle = MaterialTheme.typography.bodyMedium
                        ProvideTextStyle(textStyle) {
                            Box(
                                textModifier
                                    .fillMaxWidth()
                                    .weight(weight = 1f, fill = false)
                                    .padding(textPaddingValues)
                                    .align(Alignment.Start)
                            ) {
                                text()
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(visible = buttons != null) {
                buttons?.let {
                    CompositionLocalProvider(LocalContentColor provides buttonContentColor) {
                        val textStyle = MaterialTheme.typography.labelLarge
                        ProvideTextStyle(value = textStyle) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.End)
                            ) {
                                buttons()
                            }
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
        modifier = modifier.dialogWidth(),
        shape = shape,
        color = color,
        tonalElevation = tonalElevation,
        content = content
    )
}

@Composable
fun ButlerDialogContent(
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
val DialogMaxWidth = 420.dp
val DialogMargin = 64.dp

fun Modifier.dialogWidth(screenWidthDp: Dp = 400.dp) = widthIn(
    min = DialogMinWidth,
    max = minOf(maxOf(DialogMinWidth, screenWidthDp - DialogMargin), DialogMaxWidth),
)

// Paddings for each of the dialog's parts.
val ButlerDialogContentPadding = PaddingValues(all = 24.dp)
val ButlerDialogIconPadding = PaddingValues(bottom = 16.dp)
val ButlerDialogTitlePadding = PaddingValues(bottom = 16.dp)
val ButlerDialogTextPadding = PaddingValues(bottom = 16.dp)
