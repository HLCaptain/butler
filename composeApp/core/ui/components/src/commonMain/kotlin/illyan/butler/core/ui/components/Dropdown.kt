package illyan.butler.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.window.PopupProperties
import illyan.butler.core.ui.theme.ButlerSmallShapeCornerDp
import illyan.butler.core.ui.utils.getWindowSizeInDp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> ButlerDropdownMenuBox(
    modifier: Modifier = Modifier,
    selectedValue: T? = null,
    expanded: Boolean = false,
    searchEnabled: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    enabled: Boolean = true,
    isError: Boolean = false,
    values: List<T> = emptyList(),
    getValueName: @Composable (T) -> String = { it.toString() },
    item: @Composable (T) -> Unit = { Text(getValueName(it)) },
    getValueLeadingIcon: (T) -> ImageVector? = { null },
    getValueTrailingIcon: (T) -> ImageVector? = { null },
    leadingIcon : @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = {
        val rotation by animateFloatAsState(if (expanded) 180f else 0f)
        Icon(
            modifier = Modifier.graphicsLayer { rotationZ = rotation },
            imageVector = Icons.Rounded.KeyboardArrowUp,
            contentDescription = "Dropdown expended icon"
        )
    },
    selectValue: (T) -> Unit,
    settingName: String
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = if (enabled) onExpandedChange else { _ -> },
    ) {
        var searchedText by rememberSaveable(selectedValue) { mutableStateOf("") }
        var searchFilter by rememberSaveable { mutableStateOf("") }
        val interactionSource = remember { MutableInteractionSource() }
        LaunchedEffect(searchedText) {
            delay(250) // Delay to avoid searching on every key stroke on large lists
            searchFilter = searchedText
        }
        ButlerTextField(
            modifier = modifier.menuAnchor(if (searchEnabled) MenuAnchorType.PrimaryEditable else MenuAnchorType.PrimaryNotEditable),
            enabled = enabled,
            value = if (searchEnabled && expanded) searchedText else selectedValue?.let { getValueName(it) } ?: "",
            onValueChange = { searchedText = it },
            isError = isError,
            label = { Text(text = settingName) },
            readOnly = !enabled || !searchEnabled || !expanded,
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon,
            interactionSource = interactionSource,
            colors = ButlerTextFieldDefaults.outlinedTextFieldColors().copy(
                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurface
            )
        )
        ButlerDropdownMenu(
            isDropdownOpen = expanded,
            onDismissRequest = { onExpandedChange(false) },
            values = values.filter {
                if (searchEnabled && searchFilter.isNotBlank()) {
                    getValueName(it).contains(searchFilter, ignoreCase = true)
                } else {
                    true
                }
            },
            getValueLeadingIcon = getValueLeadingIcon,
            getValueTrailingIcon = getValueTrailingIcon,
            selectedValue = selectedValue,
            valueText = { item(it) },
            selectValue = selectValue
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> ExposedDropdownMenuBoxScope.ButlerDropdownMenu(
    modifier: Modifier = Modifier,
    isDropdownOpen: Boolean,
    onDismissRequest: () -> Unit,
    values: List<T>,
    getValueLeadingIcon: (T) -> ImageVector?,
    getValueTrailingIcon: (T) -> ImageVector?,
    selectedValue: T?,
    valueText: @Composable (T) -> Unit,
    selectValue: (T) -> Unit
) {
    val scrollState = rememberScrollState()
    val (screenHeight, _) = getWindowSizeInDp()
    ExposedDropdownMenu(
        modifier = modifier.heightIn(max = max(240.dp, screenHeight - 240.dp)),
        expanded = isDropdownOpen,
        onDismissRequest = onDismissRequest,
        scrollState = scrollState,
        border = ButlerCardDefaults.outlinedCardBorder(),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
    ) {
        ButlerDropdownMenuDefaults.DropdownMenuList(
            values = values,
            getValueLeadingIcon = getValueLeadingIcon,
            getValueTrailingIcon = getValueTrailingIcon,
            selectedValue = selectedValue,
            valueText = valueText,
            selectValue = selectValue,
            onDismissRequest = onDismissRequest,
            scrollState = scrollState
        )
    }
}

@Composable
fun <T : Any> ButlerDropdownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    values: List<T>,
    getValueLeadingIcon: (T) -> ImageVector?,
    getValueTrailingIcon: (T) -> ImageVector?,
    selectedValue: T?,
    text: @Composable (T) -> Unit,
    selectValue: (T) -> Unit
) {
    val scrollState = rememberScrollState()
    val (height, _) = getWindowSizeInDp()
    DropdownMenu(
        modifier = modifier.heightIn(max = max(ButlerDropdownMenuDefaults.MinDropdownMenuHeight, height - ButlerDropdownMenuDefaults.DropdownMenuScreenVerticalPadding)),
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        scrollState = scrollState,
        border = ButlerCardDefaults.outlinedCardBorder(),
        containerColor = ButlerDropdownMenuDefaults.ContainerColor,
        shape = MaterialTheme.shapes.medium,
    ) {
        ButlerDropdownMenuDefaults.DropdownMenuList(
            values = values,
            getValueLeadingIcon = getValueLeadingIcon,
            getValueTrailingIcon = getValueTrailingIcon,
            selectedValue = selectedValue,
            valueText = text,
            selectValue = selectValue,
            onDismissRequest = onDismissRequest,
            scrollState = scrollState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuBoxScope.ButlerDropdownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    matchTextFieldWidth: Boolean = false,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val (height, _) = getWindowSizeInDp()
    ExposedDropdownMenu(
        modifier = modifier.heightIn(max = max(ButlerDropdownMenuDefaults.MinDropdownMenuHeight, height - ButlerDropdownMenuDefaults.DropdownMenuScreenVerticalPadding)),
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        matchTextFieldWidth = matchTextFieldWidth,
        scrollState = scrollState,
        border = ButlerCardDefaults.outlinedCardBorder(),
        containerColor = ButlerDropdownMenuDefaults.ContainerColor,
        shape = MaterialTheme.shapes.medium,
        content = content,
    )
}

@Composable
fun ButlerDropdownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    offset: DpOffset = DpOffset(
        y = -ButlerDropdownMenuDefaults.DropdownMenuTopPadding,
        x = 0.dp
    ),
    popupProperties: PopupProperties = PopupProperties(),
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val (height, _) = getWindowSizeInDp()
    DropdownMenu(
        modifier = modifier.heightIn(max = max(ButlerDropdownMenuDefaults.MinDropdownMenuHeight, height - ButlerDropdownMenuDefaults.DropdownMenuScreenVerticalPadding)),
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        scrollState = scrollState,
        properties = popupProperties,
        border = ButlerCardDefaults.outlinedCardBorder(),
        offset = offset,
        containerColor = ButlerDropdownMenuDefaults.ContainerColor,
        shape = MaterialTheme.shapes.medium,
        content = content
    )
}

object ButlerDropdownMenuDefaults {
    val DropdownMenuItemMinHeight = 32.dp
    val DropdownMenuItemContentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    val DropdownMenuItemBorderPadding = PaddingValues(horizontal = 8.dp)
    val MinDropdownMenuHeight = 240.dp
    val DropdownMenuScreenVerticalPadding = 240.dp
    val DropdownMenuVerticalPadding = 10.dp
    val DropdownMenuTopPadding = ButlerSmallShapeCornerDp + ButlerCardDefaults.OutlinedCardBorderWidth
    
    val ContainerColor @Composable get() = MaterialTheme.colorScheme.surface

    @Composable
    fun <T : Any> DropdownMenuList(
        values: List<T>,
        getValueLeadingIcon: (T) -> ImageVector?,
        getValueTrailingIcon: (T) -> ImageVector?,
        selectedValue: T?,
        getValueName: @Composable (T) -> String,
        selectValue: (T) -> Unit,
        scrollState: ScrollState? = null,
        onDismissRequest: () -> Unit
    ) = DropdownMenuList(
        values = values,
        getValueLeadingIcon = getValueLeadingIcon,
        getValueTrailingIcon = getValueTrailingIcon,
        selectedValue = selectedValue,
        valueText = { Text(text = getValueName(it)) },
        selectValue = selectValue,
        scrollState = scrollState,
        onDismissRequest = onDismissRequest
    )

    @JvmName("DropdownMenuListWithComposableText")
    @Composable
    fun <T : Any> DropdownMenuList(
        values: List<T>,
        getValueLeadingIcon: (T) -> ImageVector?,
        getValueTrailingIcon: (T) -> ImageVector?,
        selectedValue: T?,
        valueText: @Composable (T) -> Unit,
        selectValue: (T) -> Unit,
        scrollState: ScrollState? = null,
        onDismissRequest: () -> Unit
    ) {
        var selectedItemPosition by remember { mutableFloatStateOf(0f) }
        var previousItemSize by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(Unit) {
            // Offset needed to show part of the previous item if there is one.
            // This helps user know that there are more items to scroll upwards.
            val offset = previousItemSize.toInt() / 2
            val itemPositionWithOffset = (selectedItemPosition.toInt() - offset).coerceAtLeast(0)
            scrollState?.animateScrollTo(itemPositionWithOffset)
        }
        Column(modifier = Modifier.focusable(enabled = true)) {
            values.forEachIndexed { index, value ->
                val leadingIcon = remember { getValueLeadingIcon(value) }
                val trailingIcon = remember(selectedValue) {
                    val icon = getValueTrailingIcon(value)
                    if (value == selectedValue) Icons.Rounded.Check else icon
                }

                DropdownMenuItem(
                    modifier = Modifier
                        .then(if (value == selectedValue) {
                            Modifier.onGloballyPositioned { selectedItemPosition = it.positionInParent().y }
                        } else if (index + 1 < values.size && values[index + 1] == selectedValue) {
                            Modifier.onGloballyPositioned { previousItemSize = it.size.height.toFloat() }
                        } else {
                            Modifier
                        })
                        .fillMaxSize(),
                    content = { valueText(value) },
                    onClick = { selectValue(value); onDismissRequest() },
                    leadingIcon = leadingIcon?.let { {
                        Icon(
                            imageVector = it,
                            contentDescription = "Leading item icon",
                            tint = if (value == selectedValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    } },
                    trailingIcon = trailingIcon?.let { {
                        Icon(
                            imageVector = it,
                            contentDescription = "Trailing item icon",
                            tint = if (value == selectedValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    } },
                    colors = ButlerCardDefaults.cardColors().copy(
                        contentColor = if (value == selectedValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }

    @Composable
    fun DropdownMenuItem(
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null,
        colors: CardColors = ButlerCardDefaults.cardColors(),
        contentPadding: PaddingValues = DropdownMenuItemContentPadding,
        borderPadding: PaddingValues = DropdownMenuItemBorderPadding,
        leadingIcon: (@Composable () -> Unit)? = null,
        trailingIcon: (@Composable () -> Unit)? = null,
        interactionSource: MutableInteractionSource? = null,
        content: @Composable () -> Unit
    ) {
        @Suppress("NAME_SHADOWING")
        val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isPressed by interactionSource.collectIsPressedAsState()
        val isFocused by interactionSource.collectIsFocusedAsState()
        val containerAlpha by animateFloatAsState(
            targetValue = if (onClick != null && (isHovered || isPressed)) 1f else 0f,
            animationSpec = tween(80),
            label = "Selected item color"
        )
        Box(
            modifier = Modifier
                .clickable(
                    enabled = onClick != null,
                    indication = null,
                    interactionSource = interactionSource,
                    onClick = { onClick?.invoke() }
                ),
            contentAlignment = Alignment.Center
        ) {
            val borderWidth by animateDpAsState(if (isFocused) ButlerCardDefaults.OutlinedCardBorderWidth else 0.dp)
            val borderAlpha by animateFloatAsState(
                targetValue = if (isFocused) 1f else 0f,
                animationSpec = tween(80),
                label = "Selected item border"
            )
            val cardContent = movableContentOf {
                Row(
                    modifier = Modifier
                        .heightIn(min = DropdownMenuItemMinHeight)
                        .padding(contentPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    leadingIcon?.invoke()
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        content()
                    }
                    Spacer(Modifier.weight(1f))
                    trailingIcon?.invoke()
                }
            }
            if (onClick != null) {
                ButlerOutlinedCard(
                    modifier = modifier.padding(borderPadding),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(0.dp),
                    colors = colors.copy(containerColor = colors.containerColor.copy(alpha = containerAlpha)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        width = borderWidth,
                        brush = SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = borderAlpha))
                    ),
                    interactionSource = interactionSource
                ) {
                    cardContent()
                }
            } else {
                ButlerCard(
                    modifier = modifier.padding(borderPadding),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(0.dp),
                    colors = colors.copy(containerColor = colors.containerColor.copy(alpha = containerAlpha)),
                    interactionSource = interactionSource
                ) {
                    cardContent()
                }
            }
        }
    }
}