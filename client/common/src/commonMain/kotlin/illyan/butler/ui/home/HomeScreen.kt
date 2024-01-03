package illyan.butler.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.Res
import illyan.butler.ui.chat_list.ChatListScreen
import illyan.butler.ui.components.MenuButton
import illyan.butler.ui.dialog.ButlerDialog
import illyan.butler.ui.model_list.ModelListScreen
import illyan.butler.ui.profile.ProfileDialogScreen
import io.github.skeptick.libres.compose.painterResource

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        HomeScreen()
    }

    @Composable
    internal fun HomeScreen() {
        val screenModel = getScreenModel<HomeScreenModel>()
        Surface {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Res.string.app_name,
                        style = MaterialTheme.typography.headlineLarge
                    )

                    var isProfileDialogShowing by rememberSaveable { mutableStateOf(false) }
                    ButlerDialog(
                        startScreen = ProfileDialogScreen(),
                        isDialogOpen = isProfileDialogShowing,
                        onDialogClosed = { isProfileDialogShowing = false }
                    )

                    Button(onClick = { isProfileDialogShowing = true }) {
                        Text(Res.string.profile)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    item {
                        Image(
                            painter = Res.image.butler_logo.painterResource(),
                            contentDescription = "Butler logo",
                            modifier = Modifier
                                .widthIn(max = 480.dp)
                                .padding(8.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    item {
                        val signedInUser by screenModel.signedInUser.collectAsState()
                        val navigator = LocalNavigator.currentOrThrow
                        AnimatedVisibility(
                            visible = signedInUser != null
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = Res.string.hello_x.format(signedInUser?.uid?.take(8) ?: Res.string.anonymous_user),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                MenuButton(
                                    text = Res.string.chats,
                                    onClick = { navigator.push(ChatListScreen()) }
                                )
                                MenuButton(
                                    text = Res.string.new_chat,
                                    onClick = { navigator.push(ModelListScreen()) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}