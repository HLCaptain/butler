package illyan.butler.core.ui.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.ButlerCard
import illyan.butler.core.ui.components.ButlerElevatedCard
import illyan.butler.core.ui.components.ButlerSolidButton
import illyan.butler.core.ui.theme.ButlerTheme

@Composable
private fun ButlerCardPreviewContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier
                        .size(36.dp),
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                )
            }
            Column {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    text = "Title",
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "More info",
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Text(text = "Cubilia ultricies turpis quisque, leo senectus fames, eu quisque lobortis velit tempus nullam vestibulum egestas. Etiam pretium nullam, torquent platea, aliquam sapien lacinia tincidunt suscipit. Ante elit, praesent quisque neque. Adipiscing ante lacus eleifend, platea cras sociosqu, morbi ut augue ornare primis ad.")
        ButlerSolidButton(onClick = {}) {
            Text("Button")
        }
    }
}

@PreviewLightDark
@Composable
private fun ButlerElevatedCardPreview() {
    ButlerTheme {
        Surface {
            ButlerElevatedCard(
                modifier = Modifier.padding(32.dp).widthIn(max = 400.dp),
            ) {
                ButlerCardPreviewContent()
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ButlerCardPreview() {
    ButlerTheme {
        Surface {
            ButlerCard(
                modifier = Modifier.padding(32.dp).widthIn(max = 400.dp),
            ) {
                ButlerCardPreviewContent()
            }
        }
    }
}
