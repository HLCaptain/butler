package illyan.butler.ui.select_host_tutorial

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.ui.select_host.SelectHostScreen

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

//        LaunchedEffect(state) {
//            if (state.hostSelected) {
//                navigator.pop()
//            }
//        }
        Column {
            Text(text = "Select your host to use Butler!")
            Button(onClick = {
                navigator.push(SelectHostScreen())
            }) {
                Text(text = "Select Host")
            }
        }
    }
}

val LocalSelectHostCallback = compositionLocalOf { {} }