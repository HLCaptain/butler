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

package illyan.butler.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.copied_to_clipboard
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Duration

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
    val tooltipState = remember { TooltipState() }
    val coroutineScope = rememberCoroutineScope()
    val tryShowTooltip = {
        coroutineScope.launch {
            if (enabled || disabledTooltip != null) tooltipState.show()
        }
    }
    RichTooltipWithContent(
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
                content = content
            )
        }
    }
}

sealed class GestureType {
    data object Click : GestureType()
    data object LongClick : GestureType()
    data class Hover(val delay: Duration = Duration.ZERO) : GestureType()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlainTooltipWithContent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    tooltip: @Composable () -> Unit,
    disabledTooltip: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    enabledGestures: List<GestureType> = emptyList(),
    onShowTooltip: () -> Unit = {},
    onDismissTooltip: () -> Unit = {},
    content: @Composable (gestureAreaModifier: Modifier) -> Unit
) {
    val tooltipState = remember { TooltipState() }
    var willShowTooltip by rememberSaveable { mutableStateOf(false) }
    var gestureType by rememberSaveable { mutableStateOf<GestureType?>(null) }
    val tryToShowTooltip = { gesture: GestureType ->
        willShowTooltip = true
        gestureType = gesture
    }
    val tryToDismissTooltip = {
        willShowTooltip = false
        gestureType = null
    }
    LaunchedEffect(willShowTooltip) {
        if (willShowTooltip && (enabled || disabledTooltip != null)) {
            if (gestureType is GestureType.Hover && (gestureType as GestureType.Hover).delay > Duration.ZERO) {
                delay((gestureType as GestureType.Hover).delay.inWholeMilliseconds)
            }
            tooltipState.show()
            willShowTooltip = false
        } else {
            tooltipState.dismiss()
        }
    }
    PlainTooltipWithContent(
        modifier = modifier,
        tooltipState = tooltipState,
        tooltip = tooltip,
        disabledTooltip = disabledTooltip,
        enabled = enabled,
        onShowTooltip = onShowTooltip,
        onDismissTooltip = onDismissTooltip
    ) {
        content(Modifier
            .then(
                if (GestureType.Click in enabledGestures || GestureType.LongClick in enabledGestures) Modifier.combinedClickable(
                    onClick = {
                        onClick()
                        if (GestureType.Click in enabledGestures) tryToShowTooltip(GestureType.Click)
                    },
                    onLongClick = {
                        onLongClick()
                        if (GestureType.LongClick in enabledGestures) tryToShowTooltip(GestureType.LongClick)
                    }
                ) else Modifier,
            )
            .then(
                if (enabledGestures.any { it is GestureType.Hover }) Modifier.handleGestures(
                    enabled = true,
                    state = tooltipState,
                    onShow = { priority -> tryToShowTooltip(enabledGestures.first { it is GestureType.Hover }) },
                    onDismiss = { tryToDismissTooltip() }
                ) else Modifier
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RichTooltipWithContent(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    tooltip: @Composable () -> Unit,
    disabledTooltip: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    enabledGestures: List<GestureType> = emptyList(),
    onShowTooltip: () -> Unit = {},
    onDismissTooltip: () -> Unit = {},
    content: @Composable (gestureAreaModifier: Modifier) -> Unit
) {
    val tooltipState = remember { TooltipState() }
    var willShowTooltip by rememberSaveable { mutableStateOf(false) }
    var gestureType by rememberSaveable { mutableStateOf<GestureType?>(null) }
    val tryToShowTooltip = { gesture: GestureType ->
        willShowTooltip = true
        gestureType = gesture
    }
    val tryToDismissTooltip = {
        willShowTooltip = false
        gestureType = null
    }
    LaunchedEffect(willShowTooltip) {
        if (willShowTooltip && (enabled || disabledTooltip != null)) {
            if (gestureType is GestureType.Hover && (gestureType as GestureType.Hover).delay > Duration.ZERO) {
                delay((gestureType as GestureType.Hover).delay.inWholeMilliseconds)
            }
            tooltipState.show()
            willShowTooltip = false
        } else {
            tooltipState.dismiss()
        }
    }
    RichTooltipWithContent(
        modifier = modifier,
        tooltipState = tooltipState,
        tooltip = tooltip,
        disabledTooltip = disabledTooltip,
        enabled = enabled,
        onShowTooltip = onShowTooltip,
        onDismissTooltip = onDismissTooltip
    ) {
        content(Modifier
            .then(
                if (GestureType.Click in enabledGestures || GestureType.LongClick in enabledGestures) Modifier.combinedClickable(
                    onClick = {
                        onClick()
                        if (GestureType.Click in enabledGestures) tryToShowTooltip(GestureType.Click)
                    },
                    onLongClick = {
                        onLongClick()
                        if (GestureType.LongClick in enabledGestures) tryToShowTooltip(GestureType.LongClick)
                    }
                ) else Modifier,
            )
            .then(
                if (enabledGestures.any { it is GestureType.Hover }) Modifier.handleGestures(
                    enabled = true,
                    state = tooltipState,
                    onShow = { priority -> tryToShowTooltip(enabledGestures.first { it is GestureType.Hover }) },
                    onDismiss = { tryToDismissTooltip() }
                ) else Modifier
            )
        )
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
    val tooltipState = remember { TooltipState() }
    val coroutineScope = rememberCoroutineScope()
    val tryShowTooltip = { coroutineScope.launch { tooltipState.show() } }
    RichTooltipWithContent(
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
fun RichTooltipWithContent(
    modifier: Modifier = Modifier,
    tooltipState: TooltipState = remember { TooltipState() },
    tooltip: @Composable () -> Unit,
    disabledTooltip: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    onShowTooltip: () -> Unit = {},
    onDismissTooltip: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var currentTooltip by remember {
        mutableStateOf(if (enabled || disabledTooltip == null) tooltip else disabledTooltip)
    }
    LaunchedEffect(tooltipState.isVisible) {
        currentTooltip = if (enabled || disabledTooltip == null) tooltip else disabledTooltip
        if (tooltipState.isVisible) onShowTooltip() else onDismissTooltip()
    }
    LaunchedEffect(enabled, tooltipState.isVisible) {
        if (!tooltipState.isVisible) {
            // Waiting for the fade out animation to end, then switch tooltip
            delay(TooltipFadeOutDuration)
            currentTooltip = if (enabled || disabledTooltip == null) tooltip else disabledTooltip
        }
    }
    TooltipBox(
        modifier = modifier,
        tooltip = { RichTooltip { Box(content = { currentTooltip() }) } },
        state = tooltipState,
        content = content,
        enableUserInput = false,
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlainTooltipWithContent(
    modifier: Modifier = Modifier,
    tooltipState: TooltipState = remember { TooltipState() },
    tooltip: @Composable () -> Unit,
    disabledTooltip: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    onShowTooltip: () -> Unit = {},
    onDismissTooltip: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var currentTooltip by remember {
        mutableStateOf(if (enabled || disabledTooltip == null) tooltip else disabledTooltip)
    }
    LaunchedEffect(tooltipState.isVisible) {
        currentTooltip = if (enabled || disabledTooltip == null) tooltip else disabledTooltip
        if (tooltipState.isVisible) onShowTooltip() else onDismissTooltip()
    }
    LaunchedEffect(enabled, tooltipState.isVisible) {
        if (!tooltipState.isVisible) {
            // Waiting for the fade out animation to end, then switch tooltip
            delay(TooltipFadeOutDuration)
            currentTooltip = if (enabled || disabledTooltip == null) tooltip else disabledTooltip
        }
    }
    TooltipBox(
        modifier = modifier,
        tooltip = { PlainTooltip { Box(content = { currentTooltip() }) } },
        state = tooltipState,
        content = content,
        enableUserInput = false,
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider()
    )
}

@OptIn(ExperimentalResourceApi::class)
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
        Text(text = stringResource(Res.string.copied_to_clipboard))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.handleGestures(
    enabled: Boolean,
    state: TooltipState,
    onShow: (MutatePriority) -> Unit,
    onDismiss: () -> Unit
): Modifier =
    if (enabled) {
        this.pointerInput(state) {
            coroutineScope {
                awaitEachGesture {
                    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
                    val pass = PointerEventPass.Initial

                    // wait for the first down press
                    val inputType = awaitFirstDown(pass = pass).type

                    if (inputType == PointerType.Touch || inputType == PointerType.Stylus) {
                        try {
                            // listen to if there is up gesture
                            // within the longPressTimeout limit
                            withTimeout(longPressTimeout) {
                                waitForUpOrCancellation(pass = pass)
                            }
                        } catch (_: PointerEventTimeoutCancellationException) {
                            // handle long press - Show the tooltip
                            launch { onShow(MutatePriority.UserInput) }

                            // consume the children's click handling
                            val changes = awaitPointerEvent(pass = pass).changes
                            for (element in changes) {
                                element.consume()
                            }
                        }
                    }
                }
            }
        }
            .pointerInput(state) {
                coroutineScope {
                    awaitPointerEventScope {
                        val pass = PointerEventPass.Main

                        while (true) {
                            val event = awaitPointerEvent(pass)
                            val inputType = event.changes[0].type
                            if (inputType == PointerType.Mouse) {
                                when (event.type) {
                                    PointerEventType.Enter -> {
                                        launch { onShow(MutatePriority.UserInput) }
                                    }

                                    PointerEventType.Exit -> {
                                        onDismiss()
                                    }
                                }
                            }
                        }
                    }
                }
            }
    } else this
