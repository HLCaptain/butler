package illyan.butler.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.utils.animateCornerBasedShapeAsState
import illyan.butler.core.ui.utils.animatePaddingValuesAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButlerTextField(
    modifier: Modifier = Modifier,
    isOutlined: Boolean = true,
    isCompact: Boolean = false,
    value: String,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = if (!readOnly) {
        {
            AnimatedVisibility(
                visible = value.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear text"
                    )
                }
            }
        }
    } else null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    shape: Shape = if (isCompact) ButlerTextFieldDefaults.CompactShape else ButlerTextFieldDefaults.Shape,
    colors: TextFieldColors = if (isOutlined) ButlerTextFieldDefaults.outlinedTextFieldColors() else ButlerTextFieldDefaults.butlerTextFieldColors(),
    interactionSource: MutableInteractionSource? = null
) {
    // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/TextField.kt;bpv=0
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    // If color is not provided via the text style, use content color as a default
    val isFocused by interactionSource.collectIsFocusedAsState()
    val textColor = textStyle.color.takeOrElse {
        when {
            !enabled -> colors.disabledTextColor
            isError -> colors.errorTextColor
            isFocused -> colors.focusedTextColor
            else -> colors.unfocusedTextColor
        }
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))
    val minimumInteractiveComponentSize by animateDpAsState(
        targetValue = if (isCompact) ButlerTextFieldDefaults.CompactMinInteractiveComponentSize else LocalMinimumInteractiveComponentSize.current,
        animationSpec = tween(150, easing = LinearEasing)
    )
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val textHeightPx by remember { derivedStateOf { textLayoutResult?.size?.height ?: 0 } }
    val contentPadding by animatePaddingValuesAsState(
        targetValue = when {
            isCompact -> PaddingValues(
                top = ((minimumInteractiveComponentSize - with(LocalDensity.current) { textHeightPx.toDp() }) / 2).coerceAtLeast(0.dp),
                bottom = ((minimumInteractiveComponentSize - with(LocalDensity.current) { textHeightPx.toDp() }) / 2).coerceAtLeast(0.dp),
                start = if (leadingIcon != null) 0.dp else ButlerTextFieldDefaults.CompactHorizontalContentPadding,
                end = if (trailingIcon != null) 0.dp else ButlerTextFieldDefaults.CompactHorizontalContentPadding
            )
            label == null -> TextFieldDefaults.contentPaddingWithoutLabel()
            else -> TextFieldDefaults.contentPaddingWithLabel()
        }
    )

    val animatedShape by animateCornerBasedShapeAsState(
        targetValue = shape as? CornerBasedShape ?: RoundedCornerShape(0.dp),
        animationSpec = tween(150, easing = LinearEasing)
    )
    CompositionLocalProvider(
        LocalTextSelectionColors provides colors.textSelectionColors,
        LocalMinimumInteractiveComponentSize provides minimumInteractiveComponentSize
    ) {
        val basicTextFieldMinWidth = if (isOutlined) OutlinedTextFieldDefaults.MinWidth else TextFieldDefaults.MinWidth
        val regularMinHeight = if (isOutlined) OutlinedTextFieldDefaults.MinHeight else TextFieldDefaults.MinHeight
        val basicTextFieldMinHeight = if (isCompact) ButlerTextFieldDefaults.CompactMinInteractiveComponentSize else regularMinHeight
        BasicTextField(
            value = value,
            modifier = modifier.defaultMinSize(
                minWidth = basicTextFieldMinWidth,
                minHeight = basicTextFieldMinHeight
            ),
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(if (isError) colors.errorCursorColor else colors.cursorColor),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = { textLayoutResult = it },
            decorationBox = @Composable { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = visualTransformation,
                    innerTextField = innerTextField,
                    placeholder = if (isCompact) placeholder ?: label else placeholder,
                    label = if (isCompact) null else label, // no label in compact mode
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = if (isCompact) null else supportingText, // no supporting text in compact mode
                    shape = if (shape is CornerBasedShape) animatedShape else shape,
                    singleLine = singleLine,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    contentPadding = contentPadding,
                    colors = colors,
                    container = {
                        if (isOutlined) {
                            OutlinedTextFieldDefaults.Container(
                                enabled = enabled,
                                isError = isError,
                                focusedBorderThickness = ButlerTextFieldDefaults.FocusedBorderThickness,
                                unfocusedBorderThickness = ButlerTextFieldDefaults.UnfocusedBorderThickness,
                                colors = colors,
                                shape = if (shape is CornerBasedShape) animatedShape else shape,
                                interactionSource = interactionSource,
                            )
                        } else {
                            TextFieldDefaults.Container(
                                enabled = enabled,
                                isError = isError,
                                focusedIndicatorLineThickness = Dp.Unspecified,
                                unfocusedIndicatorLineThickness = Dp.Unspecified,
                                shape = if (shape is CornerBasedShape) animatedShape else shape,
                                colors = colors,
                                interactionSource = interactionSource,
                            )
                        }
                    }
                )
            }
        )
    }
}


@Composable
fun ButlerConfidentialTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    visibleConfidential: Boolean = false,
    onToggleVisibility: (Boolean) -> Unit,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = {
        IconToggleButton(
            modifier = Modifier.padding(end = 8.dp),
            checked = visibleConfidential,
            onCheckedChange = onToggleVisibility,
            colors = IconButtonDefaults.iconToggleButtonColors().copy(
                checkedContentColor = LocalContentColor.current,
            )
        ) {
            Icon(
                imageVector = if (visibleConfidential) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                contentDescription = "Toggle confidential visibility"
            )
        }
    },
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = if (visibleConfidential) VisualTransformation.None else PasswordVisualTransformation(),
    keyboardOptions: KeyboardOptions = PasswordKeyboardOptions,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = ButlerTextFieldDefaults.Shape
) = ButlerTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    prefix = prefix,
    suffix = suffix,
    supportingText = supportingText,
    isError = isError,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    shape = shape,
    singleLine = true
)

object ButlerTextFieldDefaults {
    val CompactMinInteractiveComponentSize = 48.dp // Minimum height due to BasicTextField
    val Shape = RoundedCornerShape(12.dp)
    val CompactShape = RoundedCornerShape(CompactMinInteractiveComponentSize / 2)
    val CompactHorizontalContentPadding = CompactMinInteractiveComponentSize / 3
    val UnfocusedBorderThickness = 1.dp
    val FocusedBorderThickness = 2.dp
    val BaseAlpha = 0.6f

    @Composable
    fun outlinedTextFieldColors(): TextFieldColors = TextFieldDefaults.colors().copy(
        cursorColor = MaterialTheme.colorScheme.primary,
        disabledContainerColor = Color.Transparent,
        disabledIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledTextColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledLabelColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledPlaceholderColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledSupportingTextColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledPrefixColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledSuffixColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledLeadingIconColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledTrailingIconColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = Color.Transparent,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedPrefixColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        unfocusedSuffixColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = Color.Transparent,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        focusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedSupportingTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        focusedPrefixColor = MaterialTheme.colorScheme.onSurface,
        focusedSuffixColor = MaterialTheme.colorScheme.onSurface,
        focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
        focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
        errorTextColor = MaterialTheme.colorScheme.error,
        errorIndicatorColor = MaterialTheme.colorScheme.error,
        errorContainerColor = Color.Transparent,
        errorCursorColor = MaterialTheme.colorScheme.surface,
        errorPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = BaseAlpha),
        errorPrefixColor = MaterialTheme.colorScheme.error,
        errorSuffixColor = MaterialTheme.colorScheme.error,
        errorSupportingTextColor = MaterialTheme.colorScheme.error,
        errorLeadingIconColor = MaterialTheme.colorScheme.error,
        errorTrailingIconColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        textSelectionColors = TextSelectionColors(
            handleColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        )
    )

    @Composable
    fun butlerTextFieldColors() = outlinedTextFieldColors().copy(
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = BaseAlpha),
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = BaseAlpha / 2),
        errorContainerColor = MaterialTheme.colorScheme.error.copy(alpha = BaseAlpha / 4),
        unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
    )
}

val PasswordKeyboardOptions = KeyboardOptions(
    autoCorrectEnabled = false,
    keyboardType = KeyboardType.Password,
)
