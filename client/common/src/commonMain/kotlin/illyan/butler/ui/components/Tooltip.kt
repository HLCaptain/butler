/*
 * Copyright (c) 2023 Balázs Püspök-Kiss (Illyan)
 *
 * Jay is a driver behaviour analytics app.
 *
 * This file is part of Jay.
 *
 * Jay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jay.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.butler.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import illyan.butler.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Values got from material3/Tooltip.kt
private const val TooltipFadeInDuration = 150L
private const val TooltipFadeOutDuration = 75L

/**
 * @param showTooltipOnClick if true, toggles tooltip visibility when
 * card is clicked instead of long clicked
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TooltipElevatedCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    tooltip: @Composable () -> Unit,
    disabledTooltip: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    showTooltipOnClick: Boolean = false,
    onShowTooltip: () -> Unit = {},
    onDismissTooltip: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val tooltipState = remember { PlainTooltipState() }
    val coroutineScope = rememberCoroutineScope()
    val tryShowTooltip = {
        coroutineScope.launch {
            if (enabled || disabledTooltip != null) tooltipState.show()
        }
    }
    ContentWithTooltip(
        modifier = modifier,
        tooltipState = tooltipState,
        tooltip = tooltip,
        disabledTooltip = disabledTooltip,
        enabled = enabled,
        onShowTooltip = onShowTooltip,
        onDismissTooltip = onDismissTooltip
    ) {
        OutlinedCard(
            enabled = enabled,
            onClick = {
                onClick()
                if (showTooltipOnClick) tryShowTooltip()
            }
        ) {
            Surface(
                modifier = Modifier
                    .animateContentSize()
                    .combinedClickable(
                        onLongClick = {
                            onLongClick()
                            if (!showTooltipOnClick) tryShowTooltip()
                        },
                        onClick = {
                            onClick()
                            if (showTooltipOnClick) tryShowTooltip()
                        }
                    ),
                color = Color.Transparent,
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TooltipButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    tooltip: @Composable () -> Unit,
    disabledTooltip: @Composable (() -> Unit)? = null,
    showTooltipOnClick: Boolean = false,
    onShowTooltip: () -> Unit = {},
    onDismissTooltip: () -> Unit = {},
    content: @Composable RowScope.() -> Unit
) {
    val tooltipState = remember { PlainTooltipState() }
    val coroutineScope = rememberCoroutineScope()
    val tryShowTooltip = { coroutineScope.launch { tooltipState.show() } }
    ContentWithTooltip(
        modifier = modifier,
        tooltipState = tooltipState,
        tooltip = tooltip,
        disabledTooltip = disabledTooltip,
        onShowTooltip = onShowTooltip,
        onDismissTooltip = onDismissTooltip
    ) {
        Surface(
            modifier = Modifier
                .animateContentSize()
                .combinedClickable(
                    onLongClick = {
                        onLongClick()
                        if (!showTooltipOnClick) tryShowTooltip()
                    },
                    onClick = {
                        onClick()
                        if (showTooltipOnClick) tryShowTooltip()
                    }
                ),
            shape = ButtonDefaults.shape,
            color = MaterialTheme.colorScheme.primary
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onPrimary,
                LocalTextStyle provides MaterialTheme.typography.labelLarge
            ) {
                Row(
                    modifier = Modifier.padding(ButtonDefaults.ContentPadding),
                    content = content
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentWithTooltip(
    modifier: Modifier = Modifier,
    tooltipState: PlainTooltipState = remember { PlainTooltipState() },
    tooltip: @Composable () -> Unit,
    disabledTooltip: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    onShowTooltip: () -> Unit = {},
    onDismissTooltip: () -> Unit = {},
    content: @Composable () -> Unit
) {
    LaunchedEffect(tooltipState.isVisible) {
        if (tooltipState.isVisible) onShowTooltip() else onDismissTooltip()
    }
    var currentTooltip by remember {
        mutableStateOf(if (enabled || disabledTooltip == null) tooltip else disabledTooltip)
    }
    LaunchedEffect(enabled, tooltipState.isVisible) {
        if (!tooltipState.isVisible) {
            // Waiting for the fade out animation to end, then switch tooltip
            delay(TooltipFadeOutDuration)
            currentTooltip = if (enabled || disabledTooltip == null) tooltip else disabledTooltip
        }
    }
    PlainTooltipBox(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tooltip = currentTooltip,
        tooltipState = tooltipState,
    ) {
        content()
    }
}

@Composable
fun CopiedToKeyboardTooltip() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Done,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.primary
        )
        Text(text = Res.string.copied_to_clipboard)
    }
}