package illyan.butler.ui.usage_tutorial

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class UsageTutorialScreen(private val onDone: () -> Unit) : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<UsageTutorialScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        // Make your Compose Multiplatform UI
        Column {
            Text(text = "This is how to use Butler!")
            Button(onClick = {
                onDone()
            }) {
                Text(text = "Next")
            }
        }
    }
}