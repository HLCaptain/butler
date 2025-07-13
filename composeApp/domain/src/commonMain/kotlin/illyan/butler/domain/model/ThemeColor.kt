package illyan.butler.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ThemeColor(
    val seedArgb: Int? = null,
    val primaryArgb: Int? = null,
    val secondaryArgb: Int? = null,
    val tertiaryArgb: Int? = null,
    val errorArgb: Int? = null,
    val neutralArgb: Int? = null,
    val neutralVariantArgb: Int? = null,
) {
    companion object {
        val Default = ThemeColor()
    }
}
