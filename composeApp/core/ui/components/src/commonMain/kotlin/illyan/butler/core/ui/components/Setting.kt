package illyan.butler.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.off
import illyan.butler.generated.resources.on
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
                Checkbox(
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
    onDismissRequest: () -> Unit = {},
    values: Iterable<T> = emptyList(),
    getValueName: @Composable (T) -> String = { it.toString() },
    getValueLeadingIcon: (T) -> ImageVector? = { null },
    getValueTrailingIcon: (T) -> ImageVector? = { null },
    selectValue: (T) -> Unit,
    settingName: String,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    fontWeight: FontWeight = FontWeight.Normal,
    enabled: Boolean = true,
) {
    SettingItem(
        settingName = settingName,
        onClick = onDismissRequest,
        titleStyle = textStyle,
        titleWeight = fontWeight,
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
                state?.let {
                    Text(
                        text = getValueName(it),
                        style = textStyle,
                    )
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
            onDismissRequest = onDismissRequest,
            popupProperties = PopupProperties(focusable = true)
        ) {
            ButlerDropdownMenuDefaults.DropdownMenuList(
                values = values.toList(),
                selectedValue = selectedValue,
                selectValue = selectValue,
                getValueName = getValueName,
                getValueLeadingIcon = getValueLeadingIcon,
                getValueTrailingIcon = getValueTrailingIcon,
                onDismissRequest = onDismissRequest
            )
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
    onClick: () -> Unit = {},
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
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    title: @Composable RowScope.() -> Unit = {},
    content: @Composable RowScope.() -> Unit = {},
) {
    ButlerCard(
        modifier = modifier,
        colors = ButlerCardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 0f),
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