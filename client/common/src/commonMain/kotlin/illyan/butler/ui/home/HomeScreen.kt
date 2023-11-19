package illyan.butler.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import illyan.butler.Res
import illyan.butler.getPlatformName
import illyan.butler.ui.dialog.ButlerDialog
import illyan.butler.ui.profile.ProfileDialogScreen

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        HomeScreen()
    }

    @Composable
    internal fun HomeScreen() {
        val screenModel = getScreenModel<HomeScreenModel>()
        Surface {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .scrollable(
                        rememberScrollState(),
                        orientation = Orientation.Vertical
                    )
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = Res.string.hello_x.format(getPlatformName()))

                    var isProfileDialogShowing by rememberSaveable { mutableStateOf(false) }
                    ButlerDialog(
                        startScreen = ProfileDialogScreen(),
                        isDialogOpen = isProfileDialogShowing,
                        onDialogClosed = { isProfileDialogShowing = false }
                    )

                    Button(onClick = { isProfileDialogShowing = true }) {
                        Text(Res.string.profile)
                    }

                    val signedInUser by screenModel.signedInUser.collectAsState()
                    AnimatedVisibility(
                        visible = signedInUser != null
                    ) {
                        Text(text = Res.string.hello_x.format(signedInUser?.displayName ?: Res.string.anonymous_user))
                    }
                }
            }
        }
    }
}