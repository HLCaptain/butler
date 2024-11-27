package illyan.butler.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.largeDialogWidth
import illyan.butler.core.ui.components.mediumDialogSize
import illyan.butler.core.ui.components.mediumDialogWidth
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.butler_logo
import illyan.butler.generated.resources.next
import illyan.butler.generated.resources.welcome_to_butler
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.largeDialogWidth().padding(16.dp).safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            modifier = Modifier.clip(RoundedCornerShape(16.dp)).weight(1f, fill = false),
            contentScale = ContentScale.Fit,
            painter = painterResource(Res.drawable.butler_logo),
            contentDescription = "Butler Logo"
        )
        Column(
            modifier = Modifier.padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.welcome_to_butler),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Button(onClick = onNext) {
                Text(text = stringResource(Res.string.next))
            }
        }
    }
}