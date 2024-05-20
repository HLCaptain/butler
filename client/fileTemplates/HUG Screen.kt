#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
#parse("File Header.java")
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

class ${NAME}Screen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<${NAME}ScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI
    }
}