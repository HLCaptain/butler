package illyan.butler.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import illyan.butler.core.ui.components.ButlerDialogSurface
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.ui.permission.PermissionRequestScreen

@PreviewLightDark
@Composable
fun PermissionRequestPreview() {
    ButlerTheme {
        ButlerDialogSurface {
            PermissionRequestScreen(
                title = "Permission Request",
                description = "This app requires the following permissions to function properly.",
                requestPermissionText = "Request Permission",
                onDismiss = {},
                onRequestPermission = {},
            )
        }
    }
}