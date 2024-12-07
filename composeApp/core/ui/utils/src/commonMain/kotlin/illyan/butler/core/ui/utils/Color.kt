package illyan.butler.core.ui.utils

import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.contrastRatio
import com.materialkolor.ktx.darken
import com.materialkolor.ktx.lighten

fun Color.ensureContrastWith(otherColor: Color, threshold: Double = 9.0): Color {
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
