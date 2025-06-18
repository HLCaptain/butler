package illyan.butler.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.utils.animatePaddingValuesAsState
import illyan.butler.core.ui.utils.ensureContrastWith
import illyan.butler.core.ui.utils.toCardColors

@Composable
fun ButlerTag(
    modifier: Modifier = Modifier,
    colors: CardColors = ButlerTagDefaults.primaryTagColors(),
    isCompact: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) = ButlerCard(
    modifier = modifier,
    colors = colors,
    shape = ButlerTagDefaults.shape,
    contentPadding = PaddingValues(0.dp),
    content = {
        val paddingValues by animatePaddingValuesAsState(
            targetValue = if (isCompact) ButlerTagDefaults.CompactContentPadding else ButlerTagDefaults.ContentPadding
        )
        Box(
            modifier = Modifier.padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            ProvideTextStyle(ButlerTagDefaults.textStyle) {
                content()
            }
        }
    }
)

@Composable
fun ButlerTagInverted(
    modifier: Modifier = Modifier,
    colors: CardColors = ButlerTagDefaults.primaryInvertedTagColors(),
    isCompact: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) = ButlerCard(
    modifier = modifier,
    colors = colors,
    shape = ButlerTagDefaults.shape,
    contentPadding = PaddingValues(0.dp),
    content = {
        val paddingValues by animatePaddingValuesAsState(
            targetValue = if (isCompact) ButlerTagDefaults.CompactContentPadding else ButlerTagDefaults.ContentPadding
        )
        Box(
            modifier = Modifier.padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            ProvideTextStyle(ButlerTagDefaults.textStyle) {
                content()
            }
        }
    }
)

object ButlerTagDefaults {
    // Use when there are a few tags
    val ContentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)

    // Use on smaller components
    val CompactContentPadding = PaddingValues(horizontal = 2.dp, vertical = 1.dp)

    // Both compact and normal are bodySmall
    val textStyle: TextStyle @Composable get() = MaterialTheme.typography.bodySmall

    val shape: Shape @Composable get() = MaterialTheme.shapes.extraSmall

    @Composable
    fun primaryTagColors() = ButlerButtonDefaults.solidButtonPrimaryColors().toCardColors()

    @Composable
    fun secondaryTagColors() = ButlerButtonDefaults.solidButtonSecondaryColors().toCardColors()

    @Composable
    fun tertiaryTagColors() = ButlerButtonDefaults.solidButtonColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer.ensureContrastWith(MaterialTheme.colorScheme.tertiary),
        contentColor = MaterialTheme.colorScheme.tertiary,
    ).toCardColors()

    @Composable
    fun greyTagColors() = ButlerButtonDefaults.solidButtonGrayColors().toCardColors()

    @Composable
    fun primaryInvertedTagColors() = ButlerButtonDefaults.solidButtonPrimaryInvertedColors().toCardColors()

    @Composable
    fun secondaryInvertedTagColors() = ButlerButtonDefaults.solidButtonSecondaryInvertedColors().toCardColors()

    @Composable
    fun tertiaryInvertedTagColors() = ButlerButtonDefaults.solidButtonInvertedColors(
        contentColor = MaterialTheme.colorScheme.primaryContainer,
        containerColor = MaterialTheme.colorScheme.tertiary.ensureContrastWith(MaterialTheme.colorScheme.primaryContainer, 4.5),
    ).toCardColors()
}
