package illyan.butler.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import illyan.butler.core.ui.utils.ReverseLayoutDirection
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.argb_hex
import illyan.butler.generated.resources.off
import illyan.butler.generated.resources.on
import illyan.butler.generated.resources.unspecified_color
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.stringResource

@Composable
fun BooleanSetting(
    modifier: Modifier = Modifier,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    title: String,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    fontWeight: FontWeight = FontWeight.Normal,
    enabledText: String = stringResource(Res.string.on),
    disabledText: String = stringResource(Res.string.off),
    enabled: Boolean = true,
    withCheckbox: Boolean = false,
) {
    SettingItem(
        modifier = modifier,
        settingName = title,
        onClick = { onValueChange(!value) },
        titleStyle = textStyle,
        titleWeight = fontWeight,
        enabled = enabled,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Crossfade(
                targetState = value,
                label = "Boolean setting text"
            ) { enabled ->
                Text(
                    text = if (enabled) enabledText else disabledText,
                    style = textStyle,
                )
            }
            if (withCheckbox) {
                ButlerCheckbox(
                    checked = value,
                    onCheckedChange = onValueChange,
                    enabled = enabled
                )
            } else {
                Switch(
                    checked = value,
                    onCheckedChange = onValueChange,
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
fun <T : Any> DropdownSetting(
    selectedValue: T? = null,
    isDropdownOpen: Boolean = false,
    onToggleDropdown: () -> Unit = {},
    values: Collection<T> = emptyList(),
    text: @Composable (T) -> Unit = { Text(it.toString()) },
    getValueLeadingIcon: (T) -> ImageVector? = { null },
    getValueTrailingIcon: (T) -> ImageVector? = { null },
    selectValue: (T) -> Unit,
    settingName: String,
    enabled: Boolean = true,
) {
    SettingItem(
        settingName = settingName,
        onClick = onToggleDropdown,
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier.heightIn(min = LocalMinimumInteractiveComponentSize.current),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Crossfade(
                targetState = selectedValue,
                label = "Dropdown setting text",
            ) { state ->
                ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                    state?.let { text(it) }
                }
            }
            Icon(
                imageVector = if (isDropdownOpen) {
                    Icons.Rounded.ExpandLess
                } else {
                    Icons.Rounded.ExpandMore
                },
                contentDescription = ""
            )
        }
        ButlerDropdownMenu(
            expanded = isDropdownOpen,
            onDismissRequest = onToggleDropdown,
            popupProperties = PopupProperties(focusable = true)
        ) {
            ButlerDropdownMenuDefaults.DropdownMenuList(
                values = values.toList(),
                selectedValue = selectedValue,
                selectValue = selectValue,
                valueText = text,
                getValueLeadingIcon = getValueLeadingIcon,
                getValueTrailingIcon = getValueTrailingIcon,
                onDismissRequest = onToggleDropdown
            )
        }
    }
}

@Composable
fun SliderSetting(
    modifier: Modifier = Modifier,
    value: Float,
    valueString: String = value.toString(),
    onValueChange: (Float) -> Unit,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    title: String,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    SettingItem(
        modifier = modifier,
        settingName = title,
        onClick = null,
        enabled = enabled
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Slider(
                modifier = Modifier.weight(1f),
                value = value,
                onValueChange = onValueChange,
                steps = steps,
                onValueChangeFinished = onValueChangeFinished,
                enabled = enabled,
                valueRange = valueRange
            )
            Text(
                text = valueString,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChipSettings(
    modifier: Modifier = Modifier,
    size: Int,
    selectedIndex: Int?,
    title: String,
    enabled: Boolean = true,
    chipLabel: @Composable (Int) -> Unit,
    onClick: ((Int) -> Unit)?,
) {
    SettingItem(
        modifier = modifier,
        settingName = title,
        enabled = enabled
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        ReverseLayoutDirection {
            SharedTransitionLayout {
                AnimatedContent(
                    targetState = selectedIndex,
                ) { selectedIndex ->
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        ReverseLayoutDirection {
                            repeat(size) { index ->
                                val label = @Composable {
                                    Box(
                                        modifier = Modifier.sharedBounds(
                                            sharedContentState = rememberSharedContentState(key = "$index-label"),
                                            animatedVisibilityScope = this@AnimatedContent,
                                        )
                                    ) {
                                        chipLabel(index)
                                    }
                                }
                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides AssistChipDefaults.Height) {
                                    if (index == selectedIndex) {
                                        ElevatedAssistChip(
                                            modifier = Modifier.sharedElement(
                                                sharedContentState = rememberSharedContentState(key = index),
                                                animatedVisibilityScope = this@AnimatedContent
                                            ),
                                            onClick = { if (enabled) onClick?.invoke(index) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Rounded.Done,
                                                    contentDescription = null
                                                )
                                            },
                                            colors = AssistChipDefaults.elevatedAssistChipColors()
                                                .copy(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.5f
                                                    ),
                                                ),
                                            label = { label() }
                                        )
                                    } else {
                                        AssistChip(
                                            modifier = Modifier.sharedElement(
                                                sharedContentState = rememberSharedContentState(key = index),
                                                animatedVisibilityScope = this@AnimatedContent,
                                            ),
                                            onClick = { if (enabled) onClick?.invoke(index) },
                                            label = { label() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerSetting(
    modifier: Modifier = Modifier,
    title: String,
    selectedColor: Color,
    onColorChanged: (Color) -> Unit,
    enabled: Boolean = true,
) {
    var colorPickerOpen by rememberSaveable { mutableStateOf(false) }
    SettingItem(
        modifier = modifier,
        settingName = title,
        enabled = enabled,
        onClick = { colorPickerOpen = !colorPickerOpen },
    ) {
        // TODO: Implement ClipEntry for each platform and use LocalClipboard (not Manager)
//        val clipboard = LocalClipboard.current
//        IconButton(
//            onClick = {
//
//            }
//        ) {
//            Icon(
//                imageVector = Icons.Rounded.ContentCopy,
//                contentDescription = null
//            )
//        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (selectedColor == Color.Unspecified)
                    stringResource(Res.string.unspecified_color)
                else
                    "#${selectedColor.toArgb().toHexString().uppercase() }",
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (selectedColor == Color.Unspecified) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                        .size(32.dp)
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = { colorPickerOpen = !colorPickerOpen })
                        .size(32.dp)
                ) {
                    drawRect(
                        brush = SolidColor(selectedColor),
                        size = size,
                    )
                }
            }
            IconButton(
                onClick = {
                    if (selectedColor != Color.Unspecified) {
                        onColorChanged(Color.Unspecified)
                    }
                },
                enabled = selectedColor != Color.Unspecified && enabled
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = null,
                )
            }

            ButlerDropdownMenu(
                expanded = colorPickerOpen,
                onDismissRequest = { colorPickerOpen = false },
                popupProperties = PopupProperties(focusable = true),
                offset = DpOffset(
                    x = 0.dp,
                    y = 8.dp // Adjust the offset to position the dropdown below the button
                )
            ) {
                val colorPickerController = rememberColorPickerController()
                LaunchedEffect(selectedColor) {
                    if (selectedColor != Color.Unspecified) {
                        colorPickerController.selectByColor(selectedColor, fromUser = true)
                    }
                }
                HsvColorPicker(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(180.dp)
                        .padding(8.dp),
                    controller = colorPickerController,
                    initialColor = if (selectedColor == Color.Unspecified) Color.White else selectedColor,
                    onColorChanged = { colorEnvelope -> onColorChanged(colorEnvelope.color) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(35.dp),
                    controller = colorPickerController,
                    initialColor = if (selectedColor == Color.Unspecified) Color.White else selectedColor,
                )
                var textFieldColorHex by rememberSaveable(selectedColor.toArgb().toHexString()) { mutableStateOf("#${selectedColor.toArgb().toHexString()}") }
                val isValidHex = { hex: String ->
                    Napier.v {
                        "Checking hex validity: $hex"
                    }
                    hex.matches(Regex("^#?([A-Fa-f0-9]{8})$")) &&
                            hex.trim('#', ' ').length == 8
                }
                ButlerTextField(
                    modifier = Modifier.padding(8.dp),
                    isCompact = true,
                    label = {
                        Text(
                            text = stringResource(Res.string.argb_hex),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                    value = textFieldColorHex,
                    onValueChange = { newValue ->
                        if (isValidHex(newValue)) {
                            Napier.v { "Valid hex color: $newValue" }
                            newValue.trim('#', ' ').uppercase().hexToInt().let { colorInt ->
                                Napier.v { "Parsed color: $colorInt from $textFieldColorHex" }
                                onColorChanged(Color(colorInt))
                            }
                        }
                        textFieldColorHex = newValue
                    },
                    isError = !isValidHex(textFieldColorHex),
                    isOutlined = true
                )
            }
        }
    }
}

@Composable
fun BasicSetting(
    modifier: Modifier = Modifier,
    screenName: String,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) = BasicSetting(
    modifier = modifier,
    title = screenName,
    label = {
        ButlerSmallTextButton(onClick = onClick) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = ""
            )
        }
    },
    enabled = enabled,
    onClick = onClick
)

@Composable
fun BasicSetting(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.labelLarge,
    titleWeight: FontWeight = FontWeight.Normal,
    enabled: Boolean = true,
    label: @Composable RowScope.() -> Unit = {},
) {
    SettingItem(
        modifier = modifier,
        onClick = onClick,
        title = {
            Text(
                text = title,
                style = titleStyle,
                fontWeight = titleWeight,
            )
        },
        content = label,
        enabled = enabled
    )
}

@Composable
fun BasicSetting(
    modifier: Modifier = Modifier,
    title: @Composable RowScope.() -> Unit = {},
    label: @Composable RowScope.() -> Unit = {},
    onClick: () -> Unit = {}
) {
    SettingItem(
        modifier = modifier,
        onClick = onClick,
        title = title,
        content = label,
    )
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    settingName: String,
    titleStyle: TextStyle = MaterialTheme.typography.labelLarge,
    titleWeight: FontWeight = FontWeight.Normal,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit = {},
) = SettingItem(
    modifier = modifier,
    onClick = onClick,
    enabled = enabled,
    title = {
        Text(
            text = settingName,
            style = titleStyle,
            fontWeight = titleWeight,
        )
    },
    content = content
)

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    title: @Composable RowScope.() -> Unit = {},
    content: @Composable RowScope.() -> Unit = {},
) {
    ButlerCard(
        modifier = modifier,
        colors = ButlerCardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                .copy(alpha = 0f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            title()
            Row { content() }
        }
    }
}