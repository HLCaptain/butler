package illyan.butler.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun ButlerSmallSolidButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    colors: ButtonColors = ButlerButtonDefaults.solidButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    interactionSource: MutableInteractionSource? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    text: @Composable (RowScope.() -> Unit)
) {
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides ButlerButtonDefaults.SmallButtonTouchSize
    ) {
        ButlerSolidButton(
            modifier = modifier,
            onClick = onClick,
            enabled = enabled,
            shape = ButlerButtonDefaults.smallButtonShape,
            colors = colors,
            elevation = elevation,
            contentPadding = ButlerButtonDefaults.SmallContentPadding,
            interactionSource = interactionSource,
            content = {
                Row(
                    modifier = modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leadingIcon?.invoke()
                    Spacer(modifier = Modifier.width(ButlerButtonDefaults.SmallRowPadding))
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.labelLarge
                    ) { text() }
                    Spacer(modifier = Modifier.width(ButlerButtonDefaults.SmallRowPadding))
                    trailingIcon?.invoke()
                }
            }
        )
    }
}

@Composable
fun ButlerSmallOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButlerButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = ButlerButtonDefaults.outlinedButtonBorder(enabled),
    interactionSource: MutableInteractionSource? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    text: @Composable (RowScope.() -> Unit)
) {
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides ButlerButtonDefaults.SmallButtonTouchSize
    ) {
        ButlerOutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = ButlerButtonDefaults.smallButtonShape,
            colors = colors,
            elevation = elevation,
            border = border,
            contentPadding = ButlerButtonDefaults.SmallContentPadding,
            interactionSource = interactionSource,
            content = {
                Row(
                    modifier = modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leadingIcon?.invoke()
                    Spacer(modifier = Modifier.width(ButlerButtonDefaults.SmallRowPadding))
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.labelLarge
                    ) { text() }
                    Spacer(modifier = Modifier.width(ButlerButtonDefaults.SmallRowPadding))
                    trailingIcon?.invoke()
                }
            }
        )
    }
}

@Composable
fun ButlerMediumSolidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButlerButtonDefaults.solidButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    interactionSource: MutableInteractionSource? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    text: @Composable (RowScope.() -> Unit)
) = ButlerSolidButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = ButlerButtonDefaults.mediumButtonShape,
    colors = colors,
    elevation = elevation,
    contentPadding = ButlerButtonDefaults.MediumContentPadding,
    interactionSource = interactionSource,
    content = {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()
            Spacer(modifier = Modifier.width(ButlerButtonDefaults.MediumRowPadding))
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleSmall
            ) { text() }
            Spacer(modifier = Modifier.width(ButlerButtonDefaults.MediumRowPadding))
            trailingIcon?.invoke()
        }
    }
)

@Composable
fun ButlerMediumOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButlerButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = ButlerButtonDefaults.outlinedButtonBorder(enabled),
    interactionSource: MutableInteractionSource? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    text: @Composable (RowScope.() -> Unit)
) = ButlerOutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = ButlerButtonDefaults.mediumButtonShape,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = ButlerButtonDefaults.MediumContentPadding,
    interactionSource = interactionSource,
    content = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.invoke()
            Spacer(modifier = Modifier.width(ButlerButtonDefaults.MediumRowPadding))
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleSmall
            ) { text() }
            Spacer(modifier = Modifier.width(ButlerButtonDefaults.MediumRowPadding))
            trailingIcon?.invoke()
        }
    }
)

@Composable
fun ButlerOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButlerButtonDefaults.mediumButtonShape,
    colors: ButtonColors = ButlerButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = ButlerButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButlerButtonDefaults.MediumContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable (RowScope.() -> Unit)
) = OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@Composable
fun ButlerLargeSolidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButlerButtonDefaults.solidButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    interactionSource: MutableInteractionSource? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    text: @Composable (RowScope.() -> Unit)
) = ButlerSolidButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = ButlerButtonDefaults.largeButtonShape,
    colors = colors,
    elevation = elevation,
    contentPadding = ButlerButtonDefaults.LargeContentPadding,
    interactionSource = interactionSource,
    content = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.invoke()
            Spacer(modifier = Modifier.width(ButlerButtonDefaults.LargeRowPadding))
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleMedium
            ) { text() }
            Spacer(modifier = Modifier.width(ButlerButtonDefaults.LargeRowPadding))
            trailingIcon?.invoke()
        }
    }
)

@Composable
fun ButlerSolidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButlerButtonDefaults.mediumButtonShape,
    colors: ButtonColors = ButlerButtonDefaults.solidButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    contentPadding: PaddingValues = ButlerButtonDefaults.MediumContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable (RowScope.() -> Unit)
) = Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    elevation = elevation,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = content
)

@Composable
fun ButlerLargeOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButlerButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = ButlerButtonDefaults.outlinedButtonBorder(enabled),
    interactionSource: MutableInteractionSource? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    text: @Composable (RowScope.() -> Unit)
) {
    ButlerOutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = ButlerButtonDefaults.largeButtonShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = ButlerButtonDefaults.LargeContentPadding,
        interactionSource = interactionSource,
        content = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leadingIcon?.invoke()
                Spacer(modifier = Modifier.width(ButlerButtonDefaults.LargeRowPadding))
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleMedium
                ) { text() }
                Spacer(modifier = Modifier.width(ButlerButtonDefaults.LargeRowPadding))
                trailingIcon?.invoke()
            }
        }
    )
}

@Composable
fun ButlerSmallTextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    textDecoration: TextDecoration = TextDecoration.None,
    colors: ButtonColors = ButlerButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    text: @Composable (RowScope.() -> Unit)
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.labelLarge,
        LocalMinimumInteractiveComponentSize provides ButlerButtonDefaults.SmallButtonTouchSize
    ) {
        ButlerTextButton(
            modifier = modifier,
            onClick = onClick,
            enabled = enabled,
            textDecoration = textDecoration,
            shape = ButlerButtonDefaults.smallButtonShape,
            colors = colors,
            elevation = elevation,
            border = border,
            contentPadding = ButlerButtonDefaults.SmallContentPadding,
            interactionSource = interactionSource,
            content = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingIcon?.invoke()
                    Spacer(modifier = Modifier.width(ButlerButtonDefaults.SmallRowPadding))
                    text()
                    Spacer(modifier = Modifier.width(ButlerButtonDefaults.SmallRowPadding))
                    trailingIcon?.invoke()
                }
            }
        )
    }
}

@Composable
fun ButlerMediumTextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    textDecoration: TextDecoration = TextDecoration.None,
    colors: ButtonColors = ButlerButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    text: @Composable (RowScope.() -> Unit)
) {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleSmall) {
        ButlerTextButton(
            modifier = modifier,
            onClick = onClick,
            enabled = enabled,
            textDecoration = textDecoration,
            shape = ButlerButtonDefaults.mediumButtonShape,
            colors = colors,
            elevation = elevation,
            border = border,
            contentPadding = ButlerButtonDefaults.MediumContentPadding,
            interactionSource = interactionSource,
            content = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingIcon?.invoke()
                    Spacer(modifier = Modifier.width(ButlerButtonDefaults.MediumRowPadding))
                    text()
                    Spacer(modifier = Modifier.width(ButlerButtonDefaults.MediumRowPadding))
                    trailingIcon?.invoke()
                }
            }
        )
    }
}

@Composable
fun ButlerLargeTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textDecoration: TextDecoration = TextDecoration.None,
    colors: ButtonColors = ButlerButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    text: @Composable (RowScope.() -> Unit)
) {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium) {
        ButlerTextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            textDecoration = textDecoration,
            shape = ButlerButtonDefaults.largeButtonShape,
            colors = colors,
            elevation = elevation,
            border = border,
            contentPadding = ButlerButtonDefaults.LargeContentPadding,
            interactionSource = interactionSource,
            content = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingIcon?.invoke()
                    Spacer(modifier = Modifier.width(ButlerButtonDefaults.LargeRowPadding))
                    text()
                    Spacer(modifier = Modifier.width(ButlerButtonDefaults.LargeRowPadding))
                    trailingIcon?.invoke()
                }
            }
        )
    }
}

@Composable
fun ButlerTextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    textDecoration: TextDecoration = TextDecoration.None,
    shape: Shape = ButlerButtonDefaults.mediumButtonShape,
    colors: ButtonColors = ButlerButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButlerButtonDefaults.MediumContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable (RowScope.() -> Unit)
) = TextButton(
    modifier = modifier,
    onClick = onClick,
    enabled = enabled,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = border,
    contentPadding = contentPadding,
    interactionSource = interactionSource,
    content = {
        ProvideTextStyle(
            LocalTextStyle.current.copy(textDecoration = textDecoration)
        ) { content() }
    }
)

object ButlerButtonDefaults {
    val SmallContentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    val MediumContentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    val LargeContentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)

    val smallButtonShape @Composable get() = MaterialTheme.shapes.small
    val mediumButtonShape @Composable get() = MaterialTheme.shapes.medium
    val largeButtonShape @Composable get() = MaterialTheme.shapes.medium

    val SmallRowPadding = 4.dp
    val MediumRowPadding = 8.dp
    val LargeRowPadding = 12.dp

    val SmallButtonTouchSize = 32.dp
    val OutlineBorderWidth = 2.dp
    val DisabledAlpha = 0.25f

    @Composable
    fun outlinedButtonBorder(
        enabled: Boolean,
        borderColor: Color = MaterialTheme.colorScheme.primary
    ) = ButtonDefaults.outlinedButtonBorder().copy(
        width = OutlineBorderWidth,
        brush = SolidColor(if (enabled) borderColor else borderColor.copy(alpha = DisabledAlpha))
    )

    @Composable
    fun outlinedButtonPrimaryBorder(enabled: Boolean) = outlinedButtonBorder(enabled, MaterialTheme.colorScheme.primary)

    @Composable
    fun outlinedButtonSecondaryBorder(enabled: Boolean) = outlinedButtonBorder(enabled, MaterialTheme.colorScheme.secondary)

    @Composable
    fun outlinedButtonGrayBorder(enabled: Boolean) = outlinedButtonBorder(enabled, MaterialTheme.colorScheme.onSurface)

    @Composable
    fun outlinedButtonPrimaryInvertedBorder(enabled: Boolean) = outlinedButtonBorder(enabled, MaterialTheme.colorScheme.primaryContainer)

    @Composable
    fun outlinedButtonSecondaryInvertedBorder(enabled: Boolean) = outlinedButtonBorder(enabled, MaterialTheme.colorScheme.secondaryContainer)

    @Composable
    fun outlinedButtonGrayInvertedBorder(enabled: Boolean) = outlinedButtonBorder(enabled, MaterialTheme.colorScheme.surface)

    @Composable
    fun outlinedButtonColors(
        contentColor: Color = MaterialTheme.colorScheme.primary
    ) = ButtonDefaults.outlinedButtonColors().copy(
        contentColor = contentColor,
        containerColor = Color.Transparent,
        disabledContentColor = contentColor.copy(alpha = DisabledAlpha),
        disabledContainerColor = Color.Transparent,
    )

    @Composable
    fun outlinedButtonPrimaryColors() = outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)

    @Composable
    fun outlinedButtonSecondaryColors() = outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)

    @Composable
    fun outlinedButtonGrayColors() = outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)

    @Composable
    fun outlinedButtonInvertedColors(
        contentColor: Color = MaterialTheme.colorScheme.primaryContainer,
    ) = ButtonDefaults.outlinedButtonColors().copy(
        contentColor = contentColor,
        containerColor = Color.Transparent,
        disabledContentColor = contentColor.copy(alpha = DisabledAlpha),
        disabledContainerColor = Color.Transparent,
    )

    @Composable
    fun outlinedButtonPrimaryInvertedColors() = outlinedButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.primaryContainer,
    )

    @Composable
    fun outlinedButtonSecondaryInvertedColors() = outlinedButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.secondaryContainer,
    )

    @Composable
    fun outlinedButtonGrayInvertedColors() = outlinedButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.surface,
    )

    @Composable
    fun solidButtonColors(
        contentColor: Color = MaterialTheme.colorScheme.primaryContainer,
        containerColor: Color = MaterialTheme.colorScheme.primary
    ) = ButtonDefaults.buttonColors().copy(
        contentColor = contentColor,
        containerColor = containerColor,
        disabledContentColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = containerColor.copy(alpha = DisabledAlpha),
    )

    @Composable
    fun solidButtonPrimaryColors() = solidButtonColors(
        contentColor = MaterialTheme.colorScheme.primaryContainer,
        containerColor = MaterialTheme.colorScheme.primary
    )

    @Composable
    fun solidButtonSecondaryColors() = solidButtonColors(
        contentColor = MaterialTheme.colorScheme.secondaryContainer,
        containerColor = MaterialTheme.colorScheme.secondary
    )

    @Composable
    fun solidButtonGrayColors() = ButtonDefaults.buttonColors().copy(
        contentColor = MaterialTheme.colorScheme.surfaceBright,
        containerColor = MaterialTheme.colorScheme.onSurface,
        disabledContentColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = DisabledAlpha),
    )

    @Composable
    fun solidButtonInvertedColors(
        contentColor: Color = MaterialTheme.colorScheme.primary,
        containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    ) = ButtonDefaults.buttonColors().copy(
        contentColor = contentColor,
        containerColor = containerColor,
        disabledContentColor = contentColor,
        disabledContainerColor = containerColor.copy(alpha = DisabledAlpha)
    )

    @Composable
    fun solidButtonPrimaryInvertedColors() = solidButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.primary,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    )

    @Composable
    fun solidButtonSecondaryInvertedColors() = solidButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.secondary,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    )

    @Composable
    fun solidButtonGrayInvertedColors() = solidButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.onSurface,
        containerColor = MaterialTheme.colorScheme.inverseOnSurface,
    )

    @Composable
    fun textButtonColors(
        contentColor: Color = MaterialTheme.colorScheme.primary
    ) = ButtonDefaults.textButtonColors().copy(
        contentColor = contentColor,
        containerColor = Color.Transparent,
        disabledContentColor = contentColor.copy(alpha = DisabledAlpha),
        disabledContainerColor = Color.Transparent,
    )

    @Composable
    fun textButtonPrimaryColors() = textButtonColors(contentColor = MaterialTheme.colorScheme.primary)

    @Composable
    fun textButtonSecondaryColors() = textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)

    @Composable
    fun textButtonGrayColors() = textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)

    @Composable
    fun textButtonInvertedColors(
        contentColor: Color = MaterialTheme.colorScheme.primaryContainer,
    ) = ButtonDefaults.textButtonColors().copy(
        contentColor = contentColor,
        containerColor = Color.Transparent,
        disabledContentColor = contentColor.copy(alpha = DisabledAlpha),
        disabledContainerColor = Color.Transparent,
    )

    @Composable
    fun textButtonPrimaryInvertedColors() = textButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.primaryContainer,
    )

    @Composable
    fun textButtonSecondaryInvertedColors() = textButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.secondaryContainer,
    )

    @Composable
    fun textButtonGrayInvertedColors() = textButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}
