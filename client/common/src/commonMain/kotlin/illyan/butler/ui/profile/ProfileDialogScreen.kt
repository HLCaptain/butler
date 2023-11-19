package illyan.butler.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.Res
import illyan.butler.ui.components.ButlerDialogContent
import illyan.butler.ui.components.ButlerDialogSurface
import illyan.butler.ui.components.CopiedToKeyboardTooltip
import illyan.butler.ui.components.TooltipElevatedCard
import illyan.butler.ui.components.dialogWidth
import illyan.butler.ui.dialog.LocalDialogDismissRequest
import illyan.butler.ui.login.LoginScreen
import illyan.butler.ui.theme.ButlerTheme
import illyan.butler.util.log.randomUUID


class ProfileDialogScreen : Screen {
    @Composable
    override fun Content() {
        ProfileDialogScreen(
            screenModel = getScreenModel<ProfileScreenModel>(),
            navigator = LocalNavigator.currentOrThrow
        )
    }
}

@Composable
fun ProfileDialogScreen(
    screenModel: ProfileScreenModel,
    navigator: Navigator,
) {
    val userUUID by screenModel.userUUID.collectAsState()
    val isUserSignedIn by screenModel.isUserSignedIn.collectAsState()
    val isUserSigningOut by screenModel.isUserSigningOut.collectAsState()
    val userPhotoUrl by screenModel.userPhotoUrl.collectAsState()
    val email by screenModel.userEmail.collectAsState()
    val phone by screenModel.userPhoneNumber.collectAsState()
    val name by screenModel.userName.collectAsState()
    val confidentialInfo = listOf(
        Res.string.name to name,
        Res.string.email to email,
        Res.string.phone to phone
    )
    ProfileDialogContent(
        userUUID = userUUID,
        isUserSignedIn = isUserSignedIn,
        isUserSigningOut = isUserSigningOut,
        userPhotoUrl = userPhotoUrl,
        confidentialInfo = confidentialInfo,
        showConfidentialInfoInitially = false,
        onSignOut = screenModel::signOut,
        onShowLoginScreen = { navigator.push(LoginScreen()) },
//        onShowAboutScreen = { navigator.push(AboutDialogScreen) },
//        onShowSettingsScreen = { navigator.push(UserSettingsDialogScreen) },
    )
}

@Composable
fun ProfileDialogContent(
    modifier: Modifier = Modifier,
    userUUID: String? = null,
    isUserSignedIn: Boolean = true,
    isUserSigningOut: Boolean = false,
    userPhotoUrl: String? = null,
    confidentialInfo: List<Pair<String, String?>> = emptyList(),
    showConfidentialInfoInitially: Boolean = false,
    onSignOut: () -> Unit = {},
    onShowLoginScreen: () -> Unit = {},
    onShowAboutScreen: () -> Unit = {},
    onShowSettingsScreen: () -> Unit = {}
) {
    var showConfidentialInfo by remember { mutableStateOf(showConfidentialInfoInitially) }
    ButlerDialogContent(
        modifier = modifier,
        title = {
            ProfileTitleScreen(
                userUUID = userUUID,
                isUserSignedIn = isUserSignedIn,
                showConfidentialInfo = showConfidentialInfo,
                userPhotoUrl = userPhotoUrl
            )
        },
        text = {
            ProfileScreen(
                confidentialInfo = confidentialInfo
                    .filter { !it.second.isNullOrBlank() }
                    .map { it.first to it.second!! },
                onVisibilityChanged = { showConfidentialInfo = it },
                showConfidentialInfo = showConfidentialInfo
            )
        },
        buttons = {
            ProfileButtons(
                isUserSignedIn = isUserSignedIn,
                isUserSigningOut = isUserSigningOut,
                onLogin = onShowLoginScreen,
                onSignOut = onSignOut,
                onShowSettingsScreen = onShowSettingsScreen,
                onShowAboutScreen = onShowAboutScreen
            )
        },
        containerColor = Color.Transparent,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileButtons(
    onShowSettingsScreen: () -> Unit = {},
    onShowAboutScreen: () -> Unit = {},
    onLogin: () -> Unit = {},
    onSignOut: () -> Unit = {},
    isUserSignedIn: Boolean = false,
    isUserSigningOut: Boolean = false,
) {
    val onDialogClosed = LocalDialogDismissRequest.current
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ProfileMenu(
            onShowSettingsScreen = onShowSettingsScreen,
            onShowAboutScreen = onShowAboutScreen,
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.Bottom),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { onDialogClosed() }) {
                Text(text = Res.string.close)
            }
            // FIXME: find out why Crossfade does not work here
            if (isUserSignedIn) {
                TextButton(
                    enabled = !isUserSigningOut,
                    onClick = onSignOut,
                ) {
                    Text(text = Res.string.sign_out)
                }
            } else {
                Button(
                    onClick = onLogin
                ) {
                    Text(text = Res.string.login)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    showConfidentialInfo: Boolean = false,
    confidentialInfo: List<Pair<String, String>> = emptyList(),
    onVisibilityChanged: (Boolean) -> Unit = {}
) {
    var authenticated by remember { mutableStateOf(false) }
    ProfileDetailsScreen(
        modifier = modifier,
        confidentialInfo = confidentialInfo,
        showConfidentialInfo = showConfidentialInfo,
        onConfidentialInfoVisibilityChanged = {
            toggleConfidentialInfoVisibility(
                showConfidentialInfo = showConfidentialInfo,
                authenticated = authenticated,
//                        fragmentActivity = context as FragmentActivity,
                onAuthenticationChanged = { authenticated = it },
                onVisibilityChanged = onVisibilityChanged
            )
        }
    )
}

@Composable
private fun PreviewProfileDialogScreen(
    name: String = "Illyan",
    email: String = "illyan@google.com",
    phone: String = "+123456789",
) {
    ButlerTheme {
        Column {
            ButlerDialogSurface {
                ProfileDialogContent(
                    modifier = Modifier.dialogWidth(),
                    userUUID = randomUUID(),
                    userPhotoUrl = null,
                    confidentialInfo = listOf(
                        Res.string.name to name,
                        Res.string.email to email, // I wish one day :)
                        Res.string.phone to phone
                    ),
                    showConfidentialInfoInitially = true
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileTitleScreen(
    modifier: Modifier = Modifier,
    userUUID: String? = null,
    isUserSignedIn: Boolean = true,
    showConfidentialInfo: Boolean = false,
    userPhotoUrl: String? = null,
) {
    val clipboard = LocalClipboardManager.current
    FlowRow(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            Text(text = Res.string.profile)
            AnimatedVisibility(visible = userUUID != null) {
                if (userUUID != null) {
                    TooltipElevatedCard(
                        tooltip = { CopiedToKeyboardTooltip() },
                        disabledTooltip = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = Res.string.locked,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        enabled = showConfidentialInfo,
                        showTooltipOnClick = true,
                        onShowTooltip = {
                            if (showConfidentialInfo) {
                                clipboard.setText(AnnotatedString(text = userUUID))
                            }
                        }
                    ) {
                        UserInfo(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            infoName = Res.string.user_id,
                            info = userUUID.take(8),
                            show = showConfidentialInfo,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailsScreen(
    modifier: Modifier = Modifier,
    confidentialInfo: List<Pair<String, String>> = emptyList(),
    info: List<Pair<String, String>> = emptyList(),
    showConfidentialInfo: Boolean = false,
    onConfidentialInfoVisibilityChanged: (Boolean) -> Unit = {},
) {
    Column(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            ConfidentialInfoToggleButton(
                showConfidentialInfo = showConfidentialInfo,
                anyConfidentialInfo = confidentialInfo.isNotEmpty(),
                onVisibilityChanged = onConfidentialInfoVisibilityChanged
            )
            UserInfoList(
                confidentialInfo = confidentialInfo,
                info = info,
                showConfidentialInfo = showConfidentialInfo
            )
        }
    }
}

@Composable
fun UserInfoList(
    modifier: Modifier = Modifier,
    confidentialInfo: List<Pair<String, String>> = emptyList(),
    info: List<Pair<String, String>> = emptyList(),
    showConfidentialInfo: Boolean = false,
) {
    LazyRow(
        modifier = modifier,
    ) {
        item {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(confidentialInfo) {
                    UserInfo(
                        infoName = it.first,
                        info = it.second,
                        show = showConfidentialInfo
                    )
                }
                items(info) {
                    UserInfo(
                        infoName = it.first,
                        info = it.second,
                        show = true
                    )
                }
            }
        }
    }
}

@Composable
fun ConfidentialInfoToggleButton(
    modifier: Modifier = Modifier,
    showConfidentialInfo: Boolean = false,
    anyConfidentialInfo: Boolean = false,
    onVisibilityChanged: (Boolean) -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = anyConfidentialInfo
    ) {
        IconToggleButton(
            checked = showConfidentialInfo,
            onCheckedChange = onVisibilityChanged
        ) {
            Icon(
                imageVector = if (showConfidentialInfo) {
                    Icons.Rounded.LockOpen
                } else {
                    Icons.Rounded.Lock
                },
                contentDescription = ""
            )
        }
    }
}

@Composable
fun UserInfo(
    modifier: Modifier = Modifier,
    infoName: String = Res.string.unknown,
    info: String = Res.string.unknown,
    show: Boolean = false,
    style: TextStyle = LocalTextStyle.current,
    nameStyle: TextStyle = style.plus(TextStyle(fontWeight = FontWeight.SemiBold)),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = infoName,
            style = nameStyle
        )
        Crossfade(
            targetState = show,
            label = "User info"
        ) {
            Text(
                text = if (it) info else Res.string.hidden_field_string,
                style = style
            )
        }
    }
}

@Composable
fun ProfileMenu(
    modifier: Modifier = Modifier,
    onShowAboutScreen: () -> Unit = {},
    onShowSettingsScreen: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy((-12).dp)
    ) {
//        MenuButton(
//            onClick = onShowAboutScreen,
//            text = stringResource(R.string.about)
//        )
//        MenuButton(
//            onClick = onShowSettingsScreen,
//            text = stringResource(R.string.settings)
//        )
    }
}

fun toggleConfidentialInfoVisibility(
    showConfidentialInfo: Boolean,
    authenticated: Boolean,
//    fragmentActivity: FragmentActivity,
    onAuthenticationChanged: (Boolean) -> Unit,
    onVisibilityChanged: (Boolean) -> Unit,
) {
    if (showConfidentialInfo) {
        onVisibilityChanged(false)
    } else {
        if (authenticated) {
            onVisibilityChanged(true)
        } else {
            onVisibilityChanged(true)
            onAuthenticationChanged(true)
            // TODO: implement some kind of biometric authentication with multiple local users
            //  in mind. As a phone can be used by multiple people, using biometrics and other
            //  local authentication, authentication should be independent from the device itself.
            //  Though this problem is solved by Firebase Auth. Maybe simply hiding the fields is
            //  enough for now.
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                val biometricManager = BiometricManager.from(fragmentActivity)
//                when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
//                    BiometricManager.BIOMETRIC_SUCCESS ->
//                        Timber.d("App can authenticate using biometrics.")
//                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
//                        Timber.e("No biometric features available on this device.")
//                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
//                        Timber.e("Biometric features are currently unavailable.")
//                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
//                        // Prompts the user to create credentials that your app accepts.
//                        val enrollIntent = Intent(ACTION_BIOMETRIC_ENROLL).apply {
//                            putExtra(EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                                BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
//                        }
//                        fragmentActivity.startActivityForResult(enrollIntent, 0)
//                    }
//                }
//                val biometricPrompt = BiometricPrompt(
//                    fragmentActivity,
//                    object : BiometricPrompt.AuthenticationCallback() {
//                        override fun onAuthenticationSucceeded(
//                            result: BiometricPrompt.AuthenticationResult,
//                        ) {
//                            super.onAuthenticationSucceeded(result)
//                            onAuthenticationChanged(true)
//                            onInfoVisibilityChanged(true)
//                        }
//                        override fun onAuthenticationFailed() {
//                            super.onAuthenticationFailed()
//                            Timber.d("Authentication failed!")
//                        }
//                        override fun onAuthenticationError(
//                            errorCode: Int,
//                            errString: CharSequence
//                        ) {
//                            super.onAuthenticationError(errorCode, errString)
//                            Timber.d("Authentication error code: $errorCode\n" +
//                                    "Error message: $errString")
//                        }
//                    })
//                val promptTitle = fragmentActivity.getString(R.string.show_profile_info)
//                val promptSubtitle =
//                    fragmentActivity.getString(R.string.authenticate_to_view_account_information)
//                val promptInfo = BiometricPrompt.PromptInfo.Builder()
//                    .setTitle(promptTitle)
//                    .setSubtitle(promptSubtitle)
//                    .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
//                    .build()
//                biometricPrompt.authenticate(promptInfo)
//            } else {
//                // TODO: make this compatible down to API 21
//                onAuthenticationChanged(true)
//            }
        }
    }
}
