package illyan.butler.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isExpanded: Boolean = false,
    expandedContent: @Composable () -> Unit = {},
    color: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column {
            content()
            AnimatedVisibility(visible = isExpanded) {
                expandedContent()
            }
        }
    }
}

@Composable
fun DescriptionCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    fontWeight: FontWeight = FontWeight.Normal,
    showDescription: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    color: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
    content: @Composable () -> Unit = {},
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = modifier.padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            content()
            AnimatedVisibility(visible = showDescription) {
                Text(
                    modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 8.dp),
                    text = text,
                    color = textColor,
                    style = style,
                    fontWeight = fontWeight
                )
            }
        }
    }
}