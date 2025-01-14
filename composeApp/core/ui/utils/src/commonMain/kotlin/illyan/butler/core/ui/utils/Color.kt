package illyan.butler.core.ui.utils

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.blend
import com.materialkolor.ktx.contrastRatio
import com.materialkolor.ktx.darken
import com.materialkolor.ktx.lighten

fun Color.ensureContrastWith(
    otherColor: Color,
    @FloatRange(from = 1.0) threshold: Double = 9.0
): Color {
    require(threshold > 1.0) { "Threshold must be greater than 1.0" }
    var modifiedColor = this
    while (modifiedColor.contrastRatio(otherColor) <= threshold) {
        val lighterColor = modifiedColor.lighten()
        val darkerColor = modifiedColor.darken()
        if (lighterColor == modifiedColor || darkerColor == modifiedColor) {
            break
        }
        modifiedColor = if (lighterColor.contrastRatio(otherColor) > darkerColor.contrastRatio(otherColor)) {
            lighterColor
        } else {
            darkerColor
        }
    }
    return modifiedColor
}

fun Color.lowerContrastTo(otherColor: Color, threshold: Double = 9.0): Color {
    require(threshold > 1.0) { "Threshold must be greater than 1.0" }
    var modifiedColor = this
    while (modifiedColor.contrastRatio(otherColor) > threshold) {
        val lighterColor = modifiedColor.lighten()
        val darkerColor = modifiedColor.darken()
        if (lighterColor == modifiedColor || darkerColor == modifiedColor) {
            break
        }
        modifiedColor = if (lighterColor.contrastRatio(otherColor) < darkerColor.contrastRatio(otherColor)) {
            lighterColor
        } else {
            darkerColor
        }
    }
    return modifiedColor
}

fun Color.lowerContrastWithBlendTo(otherColor: Color, threshold: Double = 9.0, step: Float = 0.01f): Color {
    require(threshold > 1.0) { "Threshold must be greater than 1.0" }
    var amount = 0.0f
    while ((blend(otherColor, amount).contrastRatio(otherColor)) > threshold) {
        amount += step
        if (amount >= 1.0f) break
    }
    return blend(otherColor, amount)
}
