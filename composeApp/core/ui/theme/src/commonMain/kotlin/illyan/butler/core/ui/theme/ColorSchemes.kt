/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
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

package illyan.butler.core.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimary = md_theme_light_onPrimary,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondary = md_theme_light_onSecondary,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiary = md_theme_light_onTertiary,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    outline = md_theme_light_outline,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimary = md_theme_dark_onPrimary,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondary = md_theme_dark_onSecondary,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiary = md_theme_dark_onTertiary,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    outline = md_theme_dark_outline,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    inverseSurface = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)
@Composable
fun animateColorScheme(
    targetColorScheme: ColorScheme,
    animationSpec: AnimationSpec<Color> = spring(),
): State<ColorScheme> {
    val primary by animateColorAsState(targetValue = targetColorScheme.primary, animationSpec = animationSpec)
    val onPrimary by animateColorAsState(targetValue = targetColorScheme.onPrimary, animationSpec = animationSpec)
    val primaryContainer by animateColorAsState(targetValue = targetColorScheme.primaryContainer, animationSpec = animationSpec)
    val onPrimaryContainer by animateColorAsState(targetValue = targetColorScheme.onPrimaryContainer, animationSpec = animationSpec)
    val inversePrimary by animateColorAsState(targetValue = targetColorScheme.inversePrimary, animationSpec = animationSpec)
    val secondary by animateColorAsState(targetValue = targetColorScheme.secondary, animationSpec = animationSpec)
    val onSecondary by animateColorAsState(targetValue = targetColorScheme.onSecondary, animationSpec = animationSpec)
    val secondaryContainer by animateColorAsState(targetValue = targetColorScheme.secondaryContainer, animationSpec = animationSpec)
    val onSecondaryContainer by animateColorAsState(targetValue = targetColorScheme.onSecondaryContainer, animationSpec = animationSpec)
    val tertiary by animateColorAsState(targetValue = targetColorScheme.tertiary, animationSpec = animationSpec)
    val onTertiary by animateColorAsState(targetValue = targetColorScheme.onTertiary, animationSpec = animationSpec)
    val tertiaryContainer by animateColorAsState(targetValue = targetColorScheme.tertiaryContainer, animationSpec = animationSpec)
    val onTertiaryContainer by animateColorAsState(targetValue = targetColorScheme.onTertiaryContainer, animationSpec = animationSpec)
    val background by animateColorAsState(targetValue = targetColorScheme.background, animationSpec = animationSpec)
    val onBackground by animateColorAsState(targetValue = targetColorScheme.onBackground, animationSpec = animationSpec)
    val surface by animateColorAsState(targetValue = targetColorScheme.surface, animationSpec = animationSpec)
    val onSurface by animateColorAsState(targetValue = targetColorScheme.onSurface, animationSpec = animationSpec)
    val surfaceVariant by animateColorAsState(targetValue = targetColorScheme.surfaceVariant, animationSpec = animationSpec)
    val onSurfaceVariant by animateColorAsState(targetValue = targetColorScheme.onSurfaceVariant, animationSpec = animationSpec)
    val surfaceTint by animateColorAsState(targetValue = targetColorScheme.surfaceTint, animationSpec = animationSpec)
    val inverseSurface by animateColorAsState(targetValue = targetColorScheme.inverseSurface, animationSpec = animationSpec)
    val inverseOnSurface by animateColorAsState(targetValue = targetColorScheme.inverseOnSurface, animationSpec = animationSpec)
    val error by animateColorAsState(targetValue = targetColorScheme.error, animationSpec = animationSpec)
    val onError by animateColorAsState(targetValue = targetColorScheme.onError, animationSpec = animationSpec)
    val errorContainer by animateColorAsState(targetValue = targetColorScheme.errorContainer, animationSpec = animationSpec)
    val onErrorContainer by animateColorAsState(targetValue = targetColorScheme.onErrorContainer, animationSpec = animationSpec)
    val outline by animateColorAsState(targetValue = targetColorScheme.outline, animationSpec = animationSpec)
    val outlineVariant by animateColorAsState(targetValue = targetColorScheme.outlineVariant, animationSpec = animationSpec)
    val scrim by animateColorAsState(targetValue = targetColorScheme.scrim, animationSpec = animationSpec)
    val surfaceBright by animateColorAsState(targetValue = targetColorScheme.surfaceBright, animationSpec = animationSpec)
    val surfaceDim by animateColorAsState(targetValue = targetColorScheme.surfaceDim, animationSpec = animationSpec)
    val surfaceContainer by animateColorAsState(targetValue = targetColorScheme.surfaceContainer, animationSpec = animationSpec)
    val surfaceContainerHigh by animateColorAsState(targetValue = targetColorScheme.surfaceContainerHigh, animationSpec = animationSpec)
    val surfaceContainerHighest by animateColorAsState(targetValue = targetColorScheme.surfaceContainerHighest, animationSpec = animationSpec)
    val surfaceContainerLow by animateColorAsState(targetValue = targetColorScheme.surfaceContainerLow, animationSpec = animationSpec)
    val surfaceContainerLowest by animateColorAsState(targetValue = targetColorScheme.surfaceContainerLowest, animationSpec = animationSpec)
    val getCurrentColorScheme = {
        ColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = inversePrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = surfaceTint,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,
            surfaceBright = surfaceBright,
            surfaceDim = surfaceDim,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainerLowest = surfaceContainerLowest,
        )
    }
    return remember { derivedStateOf { getCurrentColorScheme() } }
}

val LocalTheme = compositionLocalOf<Theme?> { null }
