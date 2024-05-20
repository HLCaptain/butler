package illyan.butler.ui.usage_tutorial

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import illyan.butler.ui.components.ButlerDialogContent
import illyan.butler.ui.components.smallDialogSize

class UsageTutorialScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<UsageTutorialScreenModel>()
        ButlerDialogContent(
            modifier = Modifier.smallDialogSize(),
            text = {
                Column {
                    Text(text = "This is how to use Butler!")
                    Button(onClick = screenModel::setTutorialDone) {
                        Text(text = "Next")
                    }
                }
            }
        )
    }
}
