package illyan.butler.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp

@Composable
fun ButlerOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButlerCardDefaults.ContentPadding,
    colors: CardColors = ButlerCardDefaults.outlinedCardColors(),
    border: BorderStroke = if (enabled) ButlerCardDefaults.outlinedCardBorder() else ButlerCardDefaults.disabledOutlinedCardBorder(),
    shape: Shape = ButlerCardDefaults.shape,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    onClick?.let {
        OutlinedCard(
            modifier = modifier,
            colors = colors,
            border = border,
            enabled = enabled,
            shape = shape,
            onClick = it,
            interactionSource = interactionSource,
            content = { Column(modifier = Modifier.padding(contentPadding)) { content() } }
        )
    } ?: OutlinedCard(
        modifier = modifier,
        colors = colors,
        border = border,
        shape = shape,
        content = { Column(modifier = Modifier.padding(contentPadding)) { content() } }
    )
}

@Composable
fun ButlerCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButlerCardDefaults.ContentPadding,
    elevation: CardElevation = CardDefaults.cardElevation(),
    shape: Shape = ButlerCardDefaults.shape,
    colors: CardColors = ButlerCardDefaults.cardColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    onClick?.let {
        Card(
            modifier = modifier,
            colors = colors,
            enabled = enabled,
            onClick = it,
            interactionSource = interactionSource,
            elevation = elevation,
            shape = shape,
            content = { Column(modifier = Modifier.focusable(enabled = false).padding(contentPadding)) { content() } }
        )
    } ?: Card(
        modifier = modifier,
        colors = colors,
        elevation = elevation,
        shape = shape,
        content = { Column(modifier = Modifier.padding(contentPadding)) { content() } }
    )
}

@Composable
fun ButlerElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButlerCardDefaults.ContentPadding,
    colors: CardColors = ButlerCardDefaults.elevatedCardColors(),
    elevation: CardElevation = ButlerCardDefaults.elevatedCardElevation(),
    shape: Shape = ButlerCardDefaults.shape,
    content: @Composable ColumnScope.() -> Unit
) {
    onClick?.let {
        ElevatedCard(
            modifier = modifier,
            colors = colors,
            enabled = enabled,
            onClick = it,
            shape = shape,
            elevation = elevation,
            content = { Column(modifier = Modifier.padding(contentPadding)) { content() } }
        )
    } ?: ElevatedCard(
        modifier = modifier,
        colors = colors,
        shape = shape,
        elevation = elevation,
        content = { Column(modifier = Modifier.padding(contentPadding)) { content() } }
    )
}

object ButlerCardDefaults {
    val shape @Composable get() = MaterialTheme.shapes.large
    val CompactContentPadding = PaddingValues(8.dp)
    val ContentPadding = PaddingValues(16.dp)
    val ExtendedContentPadding = PaddingValues(24.dp)
    val SmallCardElevation = 1.dp
    val MediumCardElevation = 2.dp
    val LargeCardElevation = 16.dp

    val OutlinedCardBorderWidth = 1.dp

    @Composable
    fun outlinedCardColors(): CardColors = CardDefaults.outlinedCardColors()

    @Composable
    fun outlinedCardBorder(): BorderStroke = CardDefaults.outlinedCardBorder().copy(
        width = OutlinedCardBorderWidth,
        brush = SolidColor(MaterialTheme.colorScheme.surfaceVariant)
    )

    @Composable
    fun disabledOutlinedCardBorder(): BorderStroke = CardDefaults.outlinedCardBorder().copy(
        width = OutlinedCardBorderWidth,
        brush = SolidColor(MaterialTheme.colorScheme.surfaceVariant)
    )

    @Composable
    fun elevatedCardColors(
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        disabledContentColor: Color = MaterialTheme.colorScheme.surfaceVariant,
        disabledContainerColor: Color = containerColor.copy(alpha = 0.5f)
    ): CardColors = CardDefaults.elevatedCardColors().copy(
        contentColor = contentColor,
        containerColor = containerColor,
        disabledContentColor = disabledContentColor,
        disabledContainerColor = disabledContainerColor
    )

    @Composable
    fun cardColors(
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        disabledContentColor: Color = contentColor.copy(alpha = 0.5f),
        disabledContainerColor: Color = containerColor.copy(alpha = 0.5f)
    ): CardColors = CardDefaults.cardColors().copy(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContentColor = disabledContentColor,
        disabledContainerColor = disabledContainerColor
    )

    @Composable
    fun elevatedCardElevation(): CardElevation = CardDefaults.elevatedCardElevation(
        defaultElevation = MediumCardElevation
    )
}
