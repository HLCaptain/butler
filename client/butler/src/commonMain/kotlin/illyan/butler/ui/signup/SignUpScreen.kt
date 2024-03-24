package illyan.butler.ui.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import illyan.butler.ui.dialog.LocalDialogDismissRequest

class SignUpScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<SignUpScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI
        val onDialogClosed = LocalDialogDismissRequest.current
    }
}