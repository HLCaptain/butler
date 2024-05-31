package illyan.butler.ui.select_host_tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.select_host
import illyan.butler.generated.resources.select_host_description
import illyan.butler.ui.components.largeDialogWidth
import illyan.butler.ui.select_host.SelectHostScreen
import org.jetbrains.compose.resources.stringResource

class SelectHostTutorialScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SelectHostTutorialScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI

        // 1. Show select host tutorial
        // 2. Show select host screen
        // 3. Go back
        val navigator = LocalNavigator.currentOrThrow
        // Make your Compose Multiplatform UI

        Column(
            modifier = Modifier.largeDialogWidth().safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                modifier = Modifier.size(128.dp),
                imageVector = Icons.Rounded.CloudSync,
                contentDescription = stringResource(Res.string.select_host),
            )
            Text(
                text = stringResource(Res.string.select_host_description),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Button(onClick = {
                navigator.push(SelectHostScreen())
            }) {
                Text(text = stringResource(Res.string.select_host))
            }
        }
    }
}

val LocalSelectHostCallback = compositionLocalOf { {} }