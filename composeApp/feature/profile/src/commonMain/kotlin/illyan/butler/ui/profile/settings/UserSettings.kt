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

package illyan.butler.ui.profile.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.BooleanSetting
import illyan.butler.core.ui.components.ButlerCard
import illyan.butler.core.ui.components.ButlerCardDefaults
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.ButlerMediumTextButton
import illyan.butler.core.ui.components.CopiedToKeyboardTooltip
import illyan.butler.core.ui.components.DropdownSetting
import illyan.butler.core.ui.components.LoadingIndicator
import illyan.butler.core.ui.components.MediumCircularProgressIndicator
import illyan.butler.core.ui.components.MenuButton
import illyan.butler.core.ui.components.SmallCircularProgressIndicator
import illyan.butler.core.ui.components.TooltipElevatedCard
import illyan.butler.core.ui.components.mediumDialogWidth
import illyan.butler.core.ui.theme.canUseDynamicColors
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.domain.model.Theme
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.analytics
import illyan.butler.generated.resources.dark
import illyan.butler.generated.resources.data_settings
import illyan.butler.generated.resources.day_night_cycle
import illyan.butler.generated.resources.disabled
import illyan.butler.generated.resources.dismiss
import illyan.butler.generated.resources.dynamic_color
import illyan.butler.generated.resources.enabled
import illyan.butler.generated.resources.light
import illyan.butler.generated.resources.not_synced
import illyan.butler.generated.resources.not_syncing
import illyan.butler.generated.resources.settings
import illyan.butler.generated.resources.synced
import illyan.butler.generated.resources.syncing
import illyan.butler.generated.resources.system
import illyan.butler.generated.resources.theme
import illyan.butler.generated.resources.turn_on
import illyan.butler.generated.resources.turn_on_analytics
import illyan.butler.generated.resources.turn_on_analytics_description
import illyan.butler.generated.resources.user_id
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UserSettings() {
    val viewModel = koinViewModel<UserSettingsViewModel>()
    val state by viewModel.state.collectAsState()
    UserSettings(
        state = state,
        onThemeChange = viewModel::setTheme,
        setDynamicColorEnabled = viewModel::setDynamicColorEnabled
    )
}

@Composable
fun UserSettings(
    state: UserSettingsState,
    onThemeChange: (Theme) -> Unit = {},
    setDynamicColorEnabled: (Boolean) -> Unit = {},
) {
    UserSettingsDialogContent(
        preferences = state.userPreferences,
//        arePreferencesSynced = arePreferencesSynced,
//        canSyncPreferences = canSyncPreferences,
//        shouldSyncPreferences = shouldSyncPreferences,
//        showAnalyticsRequestDialog = showAnalyticsRequestDialog,
//        onShouldSyncChanged = screenModel::setPreferencesSync,
        onThemeChange = onThemeChange,
//        setAnalytics = screenModel::setAnalytics,
//        setAdVisibility = screenModel::setAdVisibility,
        setDynamicColorEnabled = setDynamicColorEnabled,
//        navigateToDataSettings = { destinationsNavigator.navigate(DataSettingsDialogScreenDestination) },
    )
}

@Composable
fun UserSettingsDialogContent(
    modifier: Modifier = Modifier,
    preferences: DomainPreferences?,
    arePreferencesSynced: Boolean = false,
    canSyncPreferences: Boolean = false,
    shouldSyncPreferences: Boolean = false,
    showAnalyticsRequestDialog: Boolean = false,
    onShouldSyncChanged: (Boolean) -> Unit = {},
    setAnalytics: (Boolean) -> Unit = {},
    setAdVisibility: (Boolean) -> Unit = {},
    setDynamicColorEnabled: (Boolean) -> Unit = {},
    navigateToDataSettings: () -> Unit = {},
    onThemeChange: (Theme) -> Unit = {},
) {
    Crossfade(
        modifier = modifier.mediumDialogWidth(),
        targetState = showAnalyticsRequestDialog,
        label = "User Settings Dialog Content",
    ) {
        if (it) {
            AnalyticsRequestDialogContent(
                analyticsEnabled = preferences?.analyticsEnabled,
                setAnalytics = setAnalytics
            )
        } else {
            ButlerDialogContent(
                title = {
                    UserSettingsTitle(
                        arePreferencesSynced = arePreferencesSynced,
                        preferences = preferences
                    )
                },
                text = {
                    UserSettings(
                        preferences = preferences,
                        setAnalytics = setAnalytics,
                        setAdVisibility = setAdVisibility,
                        setDynamicColorEnabled = setDynamicColorEnabled,
                        onThemeChange = onThemeChange,
                    )
                },
                buttons = {
                    UserSettingsButtons(
                        canSyncPreferences = canSyncPreferences,
                        shouldSyncPreferences = shouldSyncPreferences,
                        onShouldSyncChanged = onShouldSyncChanged,
                        navigateToDataSettings = navigateToDataSettings,
                    )
                },
                containerColor = Color.Transparent,
            )
        }
    }
}

@Composable
fun AnalyticsRequestDialogContent(
    modifier: Modifier = Modifier,
    analyticsEnabled: Boolean? = null,
    setAnalytics: (Boolean) -> Unit = {},
) {
    var analyticsSet by rememberSaveable { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            if (!analyticsSet) setAnalytics(analyticsEnabled ?: false)
        }
    }
    ButlerDialogContent(
        modifier = modifier,
        icon = {
            Icon(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally),
                imageVector = Icons.Rounded.Insights,
                contentDescription = ""
            )
        },
        title = {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(Res.string.turn_on_analytics),
                textAlign = TextAlign.Center
            )
        },
        text = {
            LazyColumn {
                item {
                    Text(text = stringResource(Res.string.turn_on_analytics_description))
                }
            }
        },
        buttons = {
            AnalyticsRequestButtons(
//                modifier = Modifier.align(Alignment.End),
                onDismiss = {
                    analyticsSet = true
                    setAnalytics(false)
                },
                onTurnOnAnalytics = {
                    analyticsSet = true
                    setAnalytics(true)
                }
            )
        }
    )
}

@Composable
fun AnalyticsRequestButtons(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onTurnOnAnalytics: () -> Unit = {}
) {
    Row(
        modifier = modifier
    ) {
        ButlerMediumTextButton(onClick = onDismiss) {
            Text(text = stringResource(Res.string.dismiss))
        }
        ButlerMediumSolidButton(onClick = onTurnOnAnalytics) {
            Text(text = stringResource(Res.string.turn_on))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserSettingsTitle(
    arePreferencesSynced: Boolean = false,
    preferences: DomainPreferences? = null,
) {
    FlowRow(
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = stringResource(Res.string.settings))
            SyncPreferencesLabel(arePreferencesSynced = arePreferencesSynced)
        }
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            Crossfade(
                targetState = preferences != null,
                label = "User Settings Title",
            ) {
                if (it && preferences != null) {
//                    Column(
//                        horizontalAlignment = Alignment.End
//                    ) {
//                        ClientLabel(clientUUID = preferences.clientId)
//                        LastUpdateLabel(lastUpdate = preferences.lastUpdate)
//                    }
                } else {
                    MediumCircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun SyncPreferencesLabel(
    arePreferencesSynced: Boolean,
) {
    Crossfade(
        targetState = arePreferencesSynced,
        label = "Sync Preferences Label",
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (it) {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(Res.string.synced),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(Res.string.not_synced),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun UserSettingsButtons(
    modifier: Modifier = Modifier,
    canSyncPreferences: Boolean = false,
    shouldSyncPreferences: Boolean = false,
    onShouldSyncChanged: (Boolean) -> Unit = {},
    navigateToDataSettings: () -> Unit = {}
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MenuButton(
            text = stringResource(Res.string.data_settings),
            onClick = navigateToDataSettings
        )
        SyncPreferencesButton(
            canSyncPreferences = canSyncPreferences,
            onShouldSyncChanged = onShouldSyncChanged,
            shouldSyncPreferences = shouldSyncPreferences,
        )
    }
}

@Composable
private fun SyncPreferencesButton(
    canSyncPreferences: Boolean,
    onShouldSyncChanged: (Boolean) -> Unit,
    shouldSyncPreferences: Boolean
) {
    ButlerCard(
        colors = ButlerCardDefaults.cardColors(
            containerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        enabled = canSyncPreferences,
        onClick = { onShouldSyncChanged(!shouldSyncPreferences) },
        contentPadding = PaddingValues(start = 8.dp, end = 2.dp, top = 2.dp, bottom = 2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    if (shouldSyncPreferences) {
                        Res.string.syncing
                    } else {
                        Res.string.not_syncing
                    }
                )
            )
            FilledIconToggleButton(
                checked = shouldSyncPreferences,
                onCheckedChange = onShouldSyncChanged,
                enabled = canSyncPreferences
            ) {
                Icon(
                    imageVector = if (shouldSyncPreferences) {
                        Icons.Rounded.Cloud
                    } else {
                        Icons.Rounded.CloudOff
                    },
                    contentDescription = ""
                )
            }
        }
    }
}

//@OptIn(ExperimentalResourceApi::class)
//@Composable
//private fun LastUpdateLabel(
//    lastUpdate: ZonedDateTime,
//) {
//    val time = lastUpdate
//        .withZoneSameInstant(ZoneId.systemDefault())
//        .minusNanos(lastUpdate.nano.toLong()) // No millis in formatted time
//        .format(DateTimeFormatter.ISO_LOCAL_TIME)
//    val date = lastUpdate
//        .withZoneSameInstant(ZoneId.systemDefault())
//        .minusNanos(lastUpdate.nano.toLong()) // No millis in formatted time
//        .format(DateTimeFormatter.ISO_LOCAL_DATE)
//    val isDateVisible by remember {
//        derivedStateOf {
//            lastUpdate.toEpochSecond().seconds.inWholeDays !=
//                    ZonedDateTime.now().toEpochSecond().seconds.inWholeDays
//        }
//    }
//    val textStyle = MaterialTheme.typography.bodyMedium
//    SettingLabel(
//        settingName = stringResource(Res.string.last_update),
//        settingNameStyle = textStyle.plus(TextStyle(fontWeight = FontWeight.SemiBold)),
//        settingIndicator = {
//            Column(
//                horizontalAlignment = Alignment.End
//            ) {
//                AnimatedVisibility(visible = isDateVisible) {
//                    Text(
//                        text = date,
//                        style = textStyle,
//                    )
//                }
//                Text(
//                    text = time,
//                    style = textStyle
//                )
//            }
//        },
//    )
//}

@Composable
fun ClientLabel(
    clientUUID: String?,
) {
    val clipboard = LocalClipboardManager.current
    AnimatedVisibility(visible = clientUUID != null) {
        TooltipElevatedCard(
            tooltip = { CopiedToKeyboardTooltip() },
            showTooltipOnClick = true,
            onShowTooltip = { clientUUID?.let { clipboard.setText(AnnotatedString(text = it)) } }
        ) {
            SettingLabel(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                settingName = stringResource(Res.string.user_id),
                settingText = clientUUID?.take(8),
                settingTextStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SettingLabel(
    modifier: Modifier = Modifier,
    settingText: String? = null,
    settingName: String,
    settingTextStyle: TextStyle = LocalTextStyle.current,
    settingNameStyle: TextStyle = settingTextStyle.plus(TextStyle(fontWeight = FontWeight.SemiBold)),
    settingValueTextAlign: TextAlign? = null,
) {
    SettingLabel(
        modifier = modifier,
        settingIndicator = {
            Crossfade(
                targetState = settingText,
                label = "Setting label text loading indicator",
            ) {
                if (it != null) {
                    Text(
                        text = it,
                        style = settingTextStyle,
                        textAlign = settingValueTextAlign,
                    )
                } else {
                    SmallCircularProgressIndicator()
                }
            }
        },
        settingName = settingName,
        settingNameStyle = settingNameStyle
    )
}

@Composable
fun SettingLabel(
    modifier: Modifier = Modifier,
    settingName: String,
    settingNameStyle: TextStyle = LocalTextStyle.current.plus(TextStyle(fontWeight = FontWeight.SemiBold)),
    settingIndicator: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = settingName,
            style = settingNameStyle,
        )
        settingIndicator()
    }
}

@Composable
fun UserSettings(
    modifier: Modifier = Modifier,
    preferences: DomainPreferences? = null,
    setAnalytics: (Boolean) -> Unit = {},
    setAdVisibility: (Boolean) -> Unit = {},
    setDynamicColorEnabled: (Boolean) -> Unit = {},
    onThemeChange: (Theme) -> Unit = {},
) {
    Crossfade(
        modifier = modifier,
        targetState = preferences != null,
        label = "User Settings Screen"
    ) {
        if (it && preferences != null) {
            LazyColumn(
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                item {
                    BooleanSetting(
                        title = stringResource(Res.string.analytics),
                        onValueChange = setAnalytics,
                        value = preferences.analyticsEnabled
                    )
                }
                item {
                    var isDropdownOpen by rememberSaveable { mutableStateOf(false) }
                    DropdownSetting(
                        settingName = stringResource(Res.string.theme),
                        isDropdownOpen = isDropdownOpen,
                        onDismissRequest = { isDropdownOpen = !isDropdownOpen },
                        selectValue = onThemeChange,
                        selectedValue = preferences.theme,
                        values = Theme.entries.toList(),
                        getValueName = { theme ->
                            when (theme) {
                                Theme.System -> stringResource(Res.string.system)
                                Theme.Light -> stringResource(Res.string.light)
                                Theme.Dark -> stringResource(Res.string.dark)
                                Theme.DayNightCycle -> stringResource(Res.string.day_night_cycle)
                            }
                        },
                        getValueLeadingIcon = { theme ->
                            when (theme) {
                                Theme.System -> Icons.Rounded.Settings
                                Theme.Light -> Icons.Rounded.LightMode
                                Theme.Dark -> Icons.Rounded.DarkMode
                                Theme.DayNightCycle -> Icons.Rounded.Schedule
                            }
                        }
                    )
                }
                item {
                    BooleanSetting(
                        value = preferences.dynamicColorEnabled,
                        onValueChange = setDynamicColorEnabled,
                        title = stringResource(Res.string.dynamic_color),
                        enabledText = stringResource(Res.string.enabled),
                        disabledText = stringResource(Res.string.disabled),
                        enabled = canUseDynamicColors()
                    )
                }
            }
        } else {
            LoadingIndicator()
        }
    }
}
