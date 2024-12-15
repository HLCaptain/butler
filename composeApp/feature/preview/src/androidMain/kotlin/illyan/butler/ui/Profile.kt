package illyan.butler.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import illyan.butler.core.ui.components.ButlerDialogSurface
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.domain.model.DomainPreferences
import illyan.butler.domain.model.Theme
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.dark
import illyan.butler.generated.resources.day_night_cycle
import illyan.butler.generated.resources.light
import illyan.butler.generated.resources.system
import illyan.butler.generated.resources.theme
import illyan.butler.ui.profile.dialog.ProfileDialogContent
import illyan.butler.ui.profile.settings.AnalyticsRequestDialogContent
import illyan.butler.ui.profile.settings.DropdownSetting
import illyan.butler.ui.profile.settings.UserSettingsDialogContent
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

private fun generateRandomUserPreferences(): DomainPreferences {
    return DomainPreferences(
        analyticsEnabled = Random.nextBoolean(),
        theme = Theme.entries.random(),
        dynamicColorEnabled = Random.nextBoolean(),
//        clientId = UUID.randomUUID().toString(),
//        lastUpdate = ZonedDateTime.now()
    )
}

@PreviewLightDark
@Composable
fun ProfilePreview() {
    ButlerTheme {
        ButlerDialogSurface {
            ProfileDialogContent(
                userUUID = "1234",
                isUserSignedIn = true,
                isUserSigningOut = false,
                confidentialInfo = listOf(
                    "Email" to "illyan@butler.com",
                    "Phone" to "+1 234 567 890"
                ),
                showConfidentialInfoInitially = false,
            )
        }
    }
}

@PreviewLightDark
@Composable
fun UserSettingsDialogScreenPreview() {
    ButlerTheme {
        ButlerDialogSurface {
            val preferences = generateRandomUserPreferences()
            val canSyncPreferences = Random.nextBoolean()
            val arePreferencesSynced = if (canSyncPreferences) Random.nextBoolean() else false
            val shouldSyncPreferences = if (arePreferencesSynced) true else Random.nextBoolean()
            UserSettingsDialogContent(
                preferences = preferences,
                canSyncPreferences = canSyncPreferences,
                arePreferencesSynced = arePreferencesSynced,
                shouldSyncPreferences = shouldSyncPreferences
            )
        }
    }
}

@PreviewLightDark
@Composable
fun AnalyticsRequestDialogContentPreview() {
    ButlerTheme {
        ButlerDialogSurface {
            val preferences = generateRandomUserPreferences()
            AnalyticsRequestDialogContent(
                analyticsEnabled = preferences.analyticsEnabled,
            )
        }
    }
}

// DropdownSetting when opened likes to shift to the start unintentionally.
// Wrap it inside a Row or layour to prevent this.
@PreviewLightDark
@Composable
fun DropdownSettingPreview() {
    ButlerTheme {
        ButlerDialogSurface {
            val isDropdownOpen by remember { mutableStateOf(true) }
            DropdownSetting(
                settingName = stringResource(Res.string.theme),
                isDropdownOpen = isDropdownOpen,
                selectValue = {},
                selectedValue = Theme.entries.random(),
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
    }
}
