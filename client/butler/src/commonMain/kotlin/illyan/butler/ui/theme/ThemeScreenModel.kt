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

package illyan.butler.ui.theme

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.manager.SettingsManager
import illyan.butler.utils.calculateSunriseSunsetTimes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory

@Factory
class ThemeScreenModel(
    settingsManager: SettingsManager,
) : ScreenModel {
    private val theme = settingsManager.userPreferences.map { it?.theme }
        .stateIn(screenModelScope, SharingStarted.Eagerly, null)

    private val dynamicColorEnabled = settingsManager.userPreferences
        .map { it?.dynamicColorEnabled == true }
        .stateIn(screenModelScope, SharingStarted.Eagerly, false)

    private val isNight = MutableStateFlow(isNight())

    val state = combine(
        theme,
        dynamicColorEnabled,
        isNight,
    ) { theme, dynamicColorEnabled, isNight ->
        ThemeScreenState(
            theme = theme,
            dynamicColorEnabled = dynamicColorEnabled,
            isNight = isNight,
        )
    }.stateIn(
        screenModelScope,
        SharingStarted.Eagerly,
        ThemeScreenState()
    )

    init {
        screenModelScope.launch {
            while (true) {
                isNight.update { isNight() }
                delay(1000)
            }
        }
    }

    fun isNight(): Boolean {
        val systemTimeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val systemDateTime = now.toLocalDateTime(systemTimeZone)

        val (sunrise, sunset) = calculateSunriseSunsetTimes(
            systemDateTime.dayOfYear,
            366,
            systemTimeZone
        )
        return systemDateTime.run { hour * 3600 + minute * 60 + second } !in (sunrise..sunset)
    }
}
