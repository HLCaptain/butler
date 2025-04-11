package illyan.butler.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp

@Composable
fun ButlerCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    colors: CardColors = ButlerCheckboxDefaults.colors(checked, false),
    border: BorderStroke = ButlerCheckboxDefaults.border(if (checked) ToggleableState.On else ToggleableState.Off, false, enabled),
) {
    ButlerTriStateCheckbox(
        modifier = modifier,
        state = if (checked) ToggleableState.On else ToggleableState.Off,
        onClick = onCheckedChange?.let { { it(!checked) } },
        enabled = enabled,
        colors = colors,
        border = border
    )
}

@Composable
fun ButlerTriStateCheckbox(
    modifier: Modifier = Modifier,
    state: ToggleableState,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: CardColors = ButlerCheckboxDefaults.colors(state, false),
    border: BorderStroke = ButlerCheckboxDefaults.border(state, false, enabled),
) {
    // TODO: add MinimumInteractiveComponentSize and indication
    ButlerOutlinedCard(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        shape = ButlerCheckboxDefaults.shape,
        border = border,
        colors = colors,
        onClick = onClick,
        enabled = enabled
    ) {
        when (state) {
            ToggleableState.Off -> {
                Box(modifier = Modifier
                    .size(ButlerCheckboxDefaults.Size)
                    .background(MaterialTheme.colorScheme.surface))
            }
            ToggleableState.On -> {
                Icon(
                    modifier = Modifier.size(ButlerCheckboxDefaults.Size),
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                )
            }
            ToggleableState.Indeterminate -> {
                Icon(
                    modifier = Modifier.size(ButlerCheckboxDefaults.Size),
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = null,
                )
            }
        }
    }
}

object ButlerCheckboxDefaults {
    val Size = 24.dp

    val shape @Composable get() = RoundedCornerShape(6.dp)

    @Composable
    fun border(
        state: ToggleableState,
        error: Boolean,
        enabled: Boolean = true
    ) = if (error) errorBorder(enabled) else when (state) {
        ToggleableState.Off -> uncheckedBorder(enabled)
        ToggleableState.On -> checkedBorder(enabled)
        ToggleableState.Indeterminate -> checkedBorder(enabled)
    }

    @Composable
    fun uncheckedBorder(enabled: Boolean) = CardDefaults.outlinedCardBorder(enabled).copy(
        width = 1.dp,
        brush = SolidColor(MaterialTheme.colorScheme.surfaceVariant),
    )

    @Composable
    fun checkedBorder(enabled: Boolean) = CardDefaults.outlinedCardBorder(enabled).copy(
        width = 2.dp,
        brush = SolidColor(MaterialTheme.colorScheme.primary),
    )

    @Composable
    fun errorBorder(enabled: Boolean) = CardDefaults.outlinedCardBorder(enabled).copy(
        width = 2.dp,
        brush = SolidColor(MaterialTheme.colorScheme.error),
    )

    @Composable
    fun colors(
        state: ToggleableState,
        error: Boolean
    ) = if (error) errorColors() else when (state) {
        ToggleableState.Off -> uncheckedColors()
        ToggleableState.On -> checkedColors()
        ToggleableState.Indeterminate -> checkedColors()
    }

    @Composable
    fun colors(checked: Boolean, error: Boolean = false) = if (error) errorColors() else if (checked) checkedColors() else uncheckedColors()

    @Composable
    fun uncheckedColors() = CardDefaults.outlinedCardColors(
        contentColor = MaterialTheme.colorScheme.surface,
        containerColor = MaterialTheme.colorScheme.surface,
        disabledContentColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceDim,
    )

    @Composable
    fun checkedColors() = CardDefaults.outlinedCardColors(
        contentColor = MaterialTheme.colorScheme.surface,
        containerColor = MaterialTheme.colorScheme.primary,
        disabledContentColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceDim,
    )

    @Composable
    fun errorColors() = uncheckedColors().copy(
        contentColor = MaterialTheme.colorScheme.error,
        containerColor = MaterialTheme.colorScheme.errorContainer,
    )
}
