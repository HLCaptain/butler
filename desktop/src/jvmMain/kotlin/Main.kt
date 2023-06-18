import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.onDrag
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import illyan.butler.common.App
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import org.jetbrains.skia.impl.Log
import org.lighthousegames.logging.KmLogging
import javax.swing.JFrame


@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    val state = rememberWindowState()
    Window(
        onCloseRequest = ::exitApplication,
        state = state,
        title = "Butler",
        undecorated = true,
        alwaysOnTop = true,
        transparent = true,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = RoundedCornerShape(8.dp)),
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                val localDensity = LocalDensity.current
                val density by remember { derivedStateOf { localDensity.density } }
                var dragStartX by remember { mutableStateOf(0.dp) }
                var dragStartY by remember { mutableStateOf(0.dp) }
                Log.info("density: $density")
                WindowDraggableArea(
                    modifier = Modifier.height(50.dp)
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(color = Color.Magenta)
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.End)
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
                App()
            }
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
