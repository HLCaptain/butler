package illyan.butler.ui.select_host_tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.largeDialogWidth
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.select_host
import illyan.butler.generated.resources.select_host_description
import illyan.butler.ui.select_host.SelectHostDialogContent
import illyan.butler.ui.select_host.SelectHostState
import illyan.butler.ui.select_host.SelectHostViewModel
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SelectHostTutorial(onSelectHostSuccessful: () -> Unit) {
    val viewModel = koinViewModel<SelectHostViewModel>()
    val state by viewModel.state.collectAsState()

    var triedToConnect by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.isConnecting) {
        Napier.d("isConnecting: ${state.isConnecting}")
        if (state.isConnecting) {
            triedToConnect = true
        }
    }

    var isTestingOnly by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state.isConnected == true && triedToConnect) {
            triedToConnect = false // Tried to connect from last successful connection
            Napier.d("Connected to host: ${state.currentHost}")
            if (!isTestingOnly) onSelectHostSuccessful()
        }
    }
    
    SelectHostTutorialContent(
        state = state,
        testAndSelectHost = {
            isTestingOnly = false
            viewModel.testAndSelectHost(it)
        },
        testHost = {
            isTestingOnly = true
            viewModel.testHost(it)
        }
    )
}

@Composable
fun SelectHostTutorialContent(
    state: SelectHostState,
    testAndSelectHost: (String) -> Unit,
    testHost: (String) -> Unit
) {
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
        SelectHostDialogContent(
            state = state,
            testAndSelectHost = testAndSelectHost,
            testHost = testHost
        )
    }
}
