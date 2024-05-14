package illyan.butler.ui.welcome

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.config.BuildConfig
import io.github.aakira.napier.Napier

class WelcomeScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<WelcomeScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        // Make your Compose Multiplatform UI
        val onDone = LocalWelcomeScreenDone.current
        LaunchedEffect(Unit) {
            Napier.d("WelcomeScreen: LaunchedEffect")
        }

        Column {
            Text(text = "Welcome to Butler!")
            Button(onClick = {
                Napier.d("WelcomeScreen: Next")
                onDone()
            }) {
                Text(text = "Next")
            }
            if (BuildConfig.DEBUG) {
                Button(onClick = {
                    screenModel.skipTutorialAndLogin()
                }) {
                    Text(text = "Skip Tutorial and Login")
                }
            }
        }
    }
}

val LocalWelcomeScreenDone = compositionLocalOf { {} }