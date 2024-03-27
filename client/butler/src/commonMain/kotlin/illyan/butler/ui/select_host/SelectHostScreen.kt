package illyan.butler.ui.select_host

import androidx.compose.animation.Crossfade
//import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.select_host
import illyan.butler.generated.resources.test_connection
import illyan.butler.ui.SmallCircularProgressIndicator
import illyan.butler.ui.components.ButlerDialogContent
import illyan.butler.ui.components.MenuButton
import illyan.butler.ui.components.smallDialogWidth
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class SelectHostScreen(private val selectedHost: () -> Unit) : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<SelectHostScreenModel>()
        val state by screenModel.state.collectAsState()

        val navigator = LocalNavigator.currentOrThrow

        var triedToConnect by rememberSaveable { mutableStateOf(false)}
        LaunchedEffect(state.isConnecting) {
            Napier.d("isConnecting: ${state.isConnecting}")
            if (state.isConnecting) {
                triedToConnect = true
            }
        }
        LaunchedEffect(state) {
            if (state.isConnected == true && triedToConnect) {
                triedToConnect = false // Tried to connect from last successful connection
                Napier.d("Connected to host: ${state.currentHost}")
                selectedHost()
            }
        }

        SelectHostDialogContent(
            state = state,
            testAndSelectHost = screenModel::testAndSelectHost,
            testHost = screenModel::testHost
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
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
            SelectHostScreen(
                state = state,
                hostUrlChanged = { hostUrl = it }
            )
        },
        buttons = {
            SelectHostButtons(
                selectHost = { testAndSelectHost(hostUrl) },
                testConnection = { testHost(hostUrl) }
            )
        },
        containerColor = Color.Transparent,
    )
}

@Composable
fun SelectHostScreen(
    modifier: Modifier = Modifier,
    state: SelectHostState,
    hostUrlChanged: (String) -> Unit = {}
) {
    var hostUrl by rememberSaveable { mutableStateOf(state.currentHost) }
    OutlinedTextField(
        modifier = modifier,
        value = hostUrl,
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

@OptIn(ExperimentalResourceApi::class)
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
        Button(
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

//@Preview
@Composable
fun SelectHostScreenPreview() {
    SelectHostDialogContent(
        state = SelectHostState(
            isConnecting = false,
            isConnected = null,
            currentHost = "http://localhost:8080"
        ),
        testAndSelectHost = {}
    )
}
