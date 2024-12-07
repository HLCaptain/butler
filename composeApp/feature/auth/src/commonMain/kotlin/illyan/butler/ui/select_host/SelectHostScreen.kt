package illyan.butler.ui.select_host

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.MenuButton
import illyan.butler.core.ui.components.SmallCircularProgressIndicator
import illyan.butler.core.ui.components.smallDialogWidth
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.select_host
import illyan.butler.generated.resources.test_connection
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SelectHost(onSelectHostSuccessful: () -> Unit) {
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

    SelectHostDialogContent(
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
fun SelectHostDialogContent(
    modifier: Modifier = Modifier,
    state: SelectHostState,
    testAndSelectHost: (String) -> Unit,
    testHost: (String) -> Unit = {},
) {
    var hostUrl by rememberSaveable { mutableStateOf(state.currentHost) }
    ButlerDialogContent(
        modifier = modifier.smallDialogWidth(),
        title = {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(Res.string.select_host)
            )
        },
        text = {
            SelectHost(
                state = state,
                hostUrlChanged = { hostUrl = it }
            )
        },
        buttons = {
            SelectHostButtons(
                selectHost = { testAndSelectHost(hostUrl ?: "") },
                testConnection = { testHost(hostUrl ?: "") }
            )
        },
        containerColor = Color.Transparent,
    )
}

@Composable
fun SelectHost(
    modifier: Modifier = Modifier,
    state: SelectHostState,
    hostUrlChanged: (String) -> Unit = {}
) {
    var hostUrl by rememberSaveable { mutableStateOf(state.currentHost) }
    OutlinedTextField(
        modifier = modifier,
        value = hostUrl ?: "",
        enabled = true,
        onValueChange = {
            hostUrl = it
            hostUrlChanged(it)
        },
        trailingIcon = {
            Crossfade(targetState = state) {
                if (it.isConnecting) {
                    SmallCircularProgressIndicator()
                } else {
                    val icon = when (it.isConnected) {
                        true -> Icons.Rounded.Check to MaterialTheme.colorScheme.primary
                        false -> Icons.Rounded.Close to MaterialTheme.colorScheme.error
                        null -> null
                    }
                    icon?.let { (icon, tint) ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun SelectHostButtons(
    modifier: Modifier = Modifier,
    selectHost: () -> Unit,
    testConnection: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ButlerMediumSolidButton(
            onClick = selectHost,
            enabled = true
        ) {
            Text(text = stringResource(Res.string.select_host))
        }
        MenuButton(
            onClick = testConnection,
            enabled = true,
            text = stringResource(Res.string.test_connection)
        )
    }
}
