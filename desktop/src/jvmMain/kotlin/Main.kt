import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowCornerPreference
import com.mayakapps.compose.windowstyler.WindowFrameStyle
import com.mayakapps.compose.windowstyler.WindowStyle
import illyan.butler.common.App
import io.kanro.compose.jetbrains.expui.control.ActionButton
import io.kanro.compose.jetbrains.expui.control.Icon
import io.kanro.compose.jetbrains.expui.control.Tooltip
import io.kanro.compose.jetbrains.expui.window.JBWindow
import theme.ButlerDarkTheme
import theme.ButlerLightTheme
import java.awt.Desktop
import java.net.URI
import javax.swing.JFrame


@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    val state = rememberWindowState()
    var isDark by remember { mutableStateOf(false) }
    val theme = if (isDark) {
        ButlerDarkTheme
    } else {
        ButlerLightTheme
    }
    JBWindow(
        onCloseRequest = ::exitApplication,
        state = state,
        title = "Butler",
        theme = theme,
        mainToolBar = {
            Row(Modifier.mainToolBarItem(Alignment.End)) {
                Tooltip("Open GitHub link in browser") {
                    ActionButton(
                        {
                            Desktop.getDesktop()
                                .browse(URI.create("https://github.com/ButterCam/compose-jetbrains-theme"))
                        }, Modifier.size(40.dp), shape = RectangleShape
                    ) {
                        Icon("icons/github.svg")
                    }
                }
                Tooltip("Switch between dark and light mode,\ncurrently is ${if (isDark) "dark" else "light"} mode") {
                    ActionButton(
                        { isDark = !isDark }, Modifier.size(40.dp), shape = RectangleShape
                    ) {
                        Icon("icons/${if (isDark) "dark" else "light"}Theme.svg")
                    }
                }
            }
        }
    ) {
//        WindowStyle(
//            isDarkTheme = isDark,
//            backdropType = WindowBackdrop.Mica,
//            frameStyle = WindowFrameStyle(
//                cornerPreference = WindowCornerPreference.ROUNDED,
//            ),
//        )
        var isWindowFocused by remember { mutableStateOf(window.isFocused) }
        LaunchedEffect(window.isFocused) { isWindowFocused = window.isFocused }
        val backgroundOpacity by animateFloatAsState(if (isWindowFocused) 0f else 1f)
        val colorShift by animateIntAsState(if (isDark) 22 else 255)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(colorShift, colorShift, colorShift).copy(alpha = backgroundOpacity))
        ) {

//            SwingPanel(
//                background = Color.Magenta,
//                modifier = Modifier.fillMaxWidth(),
//                factory = {
//                    JInternalFrame().apply {
//
//                    }
//                    JToolBar().apply {
//
//                    }
//                }
//            )
            WindowDraggableArea(
                modifier = Modifier.height(50.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(8.dp)
                    ) {
                        val windowInfo = LocalWindowInfo.current
                        Button(
                            onClick = {
                                val current = windowInfo.isWindowFocused
                                if (current) {
                                    window.extendedState = JFrame.ICONIFIED
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Button(
                            onClick = {
                                val current = windowInfo.isWindowFocused
                                if (current) {
                                    if (state.placement == WindowPlacement.Fullscreen) {
                                        state.placement = WindowPlacement.Floating
                                    } else {
                                        state.placement = WindowPlacement.Fullscreen
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Button(
                            onClick = {
                                exitApplication()
                            }
                        )
                    }
                }
            }
            App()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Button(
    text: String = "",
    onClick: () -> Unit = {},
    color: Color = Color(210, 210, 210),
    size: Int = 32
) {
    val buttonHover = remember { mutableStateOf(false) }
    Surface(
        color = if (buttonHover.value)
            Color(color.red / 1.3f, color.green / 1.3f, color.blue / 1.3f)
        else
            color,
        shape = RoundedCornerShape((size / 8).dp)
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onClick)
                .size(size.dp, size.dp)
                .onPointerEvent(PointerEventType.Move) {}
                .onPointerEvent(PointerEventType.Enter) {
                    buttonHover.value = true
                }
                .onPointerEvent(PointerEventType.Exit) {
                    buttonHover.value = false
                }
        ) {
            Text(text = text)
        }
    }
}
