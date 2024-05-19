package illyan.butler.ui.usage_tutorial

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.ui.components.ButlerDialogContent
import illyan.butler.ui.components.smallDialogSize

class UsageTutorialScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<UsageTutorialScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        // Make your Compose Multiplatform UI
        val onDone = LocalUsageTutorialDone.current
        ButlerDialogContent(
            modifier = Modifier.smallDialogSize(),
            text = {
                Column {
                    Text(text = "This is how to use Butler!")
                    Button(onClick = {
                        onDone()
                    }) {
                        Text(text = "Next")
                    }
                }
            }
        )
    }
}

val LocalUsageTutorialDone = compositionLocalOf { {} }