package illyan.butler.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ThemeColor {

    @Serializable
    data class Accent(val argb: Int) : ThemeColor()

    @Serializable
    data class Material(
        val primaryArgb: Int,
        val secondaryArgb: Int,
        val tertiaryArgb: Int,
        val neutralArgb: Int,
        val neutralVariantArgb: Int,
    ) : ThemeColor()
}
