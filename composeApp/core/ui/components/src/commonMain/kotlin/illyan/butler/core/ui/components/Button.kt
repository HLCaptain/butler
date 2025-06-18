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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.utils.ensureContrastWith

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
        LocalTextStyle provides ButlerButtonDefaults.smallTextStyle,
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
                ButlerButtonDefaults.ButtonRow(
                    rowPadding = ButlerButtonDefaults.SmallRowPadding,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    text = text
                )
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
        LocalTextStyle provides ButlerButtonDefaults.smallTextStyle,
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
                ButlerButtonDefaults.ButtonRow(
                    rowPadding = ButlerButtonDefaults.SmallRowPadding,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    text = text
                )
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
) {
    CompositionLocalProvider(LocalTextStyle provides ButlerButtonDefaults.mediumTextStyle) {
        ButlerSolidButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = ButlerButtonDefaults.mediumButtonShape,
            colors = colors,
            elevation = elevation,
            contentPadding = ButlerButtonDefaults.MediumContentPadding,
            interactionSource = interactionSource,
            content = {
                ButlerButtonDefaults.ButtonRow(
                    rowPadding = ButlerButtonDefaults.MediumRowPadding,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    text = text
                )
            }
        )
    }
}

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
) {
    CompositionLocalProvider(LocalTextStyle provides ButlerButtonDefaults.mediumTextStyle) {
        ButlerOutlinedButton(
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
                ButlerButtonDefaults.ButtonRow(
                    rowPadding = ButlerButtonDefaults.MediumRowPadding,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    text = text
                )
            }
        )
    }
}

@Composable
private fun ButlerOutlinedButton(
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
) {
    CompositionLocalProvider(LocalTextStyle provides ButlerButtonDefaults.largeTextStyle) {
        ButlerLargeSolidButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            elevation = elevation,
            interactionSource = interactionSource,
            content = {
                ButlerButtonDefaults.ButtonRow(
                    rowPadding = ButlerButtonDefaults.LargeRowPadding,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    text = text
                )
            }
        )
    }
}

/**
 * Only *Large* buttons can be wide with Modifier.fillMaxWidth(),
 * so content: @Composable (RowScope.() -> Unit) is exposed to
 * allow embedding custom wide content.
 */
@Composable
fun ButlerLargeSolidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButlerButtonDefaults.solidButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable (RowScope.() -> Unit)
) {
    CompositionLocalProvider(LocalTextStyle provides ButlerButtonDefaults.largeTextStyle) {
        ButlerSolidButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = ButlerButtonDefaults.largeButtonShape,
            colors = colors,
            elevation = elevation,
            contentPadding = ButlerButtonDefaults.LargeContentPadding,
            interactionSource = interactionSource,
            content = content
        )
    }
}

@Composable
private fun ButlerSolidButton(
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
    CompositionLocalProvider(LocalTextStyle provides ButlerButtonDefaults.largeTextStyle) {
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
                ButlerButtonDefaults.ButtonRow(
                    rowPadding = ButlerButtonDefaults.LargeRowPadding,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    text = text
                )
            }
        )
    }
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
        LocalTextStyle provides ButlerButtonDefaults.smallTextStyle,
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
                ButlerButtonDefaults.ButtonRow(
                    rowPadding = ButlerButtonDefaults.SmallRowPadding,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    text = text
                )
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
    CompositionLocalProvider(LocalTextStyle provides ButlerButtonDefaults.mediumTextStyle) {
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
                ButlerButtonDefaults.ButtonRow(
                    rowPadding = ButlerButtonDefaults.MediumRowPadding,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    text = text
                )
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
    CompositionLocalProvider(LocalTextStyle provides ButlerButtonDefaults.largeTextStyle) {
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
                ButlerButtonDefaults.ButtonRow(
                    rowPadding = ButlerButtonDefaults.LargeRowPadding,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    text = text
                )
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
    textStyle: TextStyle = LocalTextStyle.current,
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
            textStyle.copy(textDecoration = textDecoration)
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

    val smallTextStyle @Composable get() = MaterialTheme.typography.labelLarge
    val mediumTextStyle @Composable get() = MaterialTheme.typography.titleSmall
    val largeTextStyle @Composable get() = MaterialTheme.typography.titleMedium

    val SmallRowPadding = 4.dp
    val MediumRowPadding = 8.dp
    val LargeRowPadding = 12.dp

    val SmallButtonTouchSize = 32.dp
    val OutlineBorderWidth = 2.dp
    val DisabledAlpha = 0.25f

    @Composable
    fun ButtonRow(
        modifier: Modifier = Modifier,
        rowPadding: Dp = MediumRowPadding,
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null,
        text: @Composable (RowScope.() -> Unit)
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.invoke()
            Spacer(modifier = Modifier.width(rowPadding))
            text()
            Spacer(modifier = Modifier.width(rowPadding))
            trailingIcon?.invoke()
        }
    }

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
        contentColor: Color = MaterialTheme.colorScheme.primaryContainer.ensureContrastWith(MaterialTheme.colorScheme.primary),
        containerColor: Color = MaterialTheme.colorScheme.primary
    ) = ButtonDefaults.buttonColors().copy(
        contentColor = contentColor,
        containerColor = containerColor,
        disabledContentColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = containerColor.copy(alpha = DisabledAlpha),
    )

    @Composable
    fun solidButtonPrimaryColors() = solidButtonColors(
        contentColor = MaterialTheme.colorScheme.primaryContainer.ensureContrastWith(MaterialTheme.colorScheme.primary),
        containerColor = MaterialTheme.colorScheme.primary
    )

    @Composable
    fun solidButtonSecondaryColors() = solidButtonColors(
        contentColor = MaterialTheme.colorScheme.secondaryContainer.ensureContrastWith(MaterialTheme.colorScheme.secondary),
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
        containerColor: Color = MaterialTheme.colorScheme.primaryContainer.ensureContrastWith(MaterialTheme.colorScheme.primary),
    ) = ButtonDefaults.buttonColors().copy(
        contentColor = contentColor,
        containerColor = containerColor,
        disabledContentColor = contentColor,
        disabledContainerColor = containerColor.copy(alpha = DisabledAlpha)
    )

    @Composable
    fun solidButtonPrimaryInvertedColors() = solidButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.primary,
        containerColor = MaterialTheme.colorScheme.primaryContainer.ensureContrastWith(MaterialTheme.colorScheme.primary),
    )

    @Composable
    fun solidButtonSecondaryInvertedColors() = solidButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.secondary,
        containerColor = MaterialTheme.colorScheme.secondaryContainer.ensureContrastWith(MaterialTheme.colorScheme.secondary),
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
