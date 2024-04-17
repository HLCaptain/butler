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
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.email
import illyan.butler.generated.resources.hidden_field_string
import illyan.butler.generated.resources.locked
import illyan.butler.generated.resources.login
import illyan.butler.generated.resources.name
import illyan.butler.generated.resources.phone
import illyan.butler.generated.resources.profile
import illyan.butler.generated.resources.reset_tutorial_and_sign_out
import illyan.butler.generated.resources.sign_out
import illyan.butler.generated.resources.unknown
import illyan.butler.generated.resources.user_id
import illyan.butler.ui.components.ButlerDialogContent
import illyan.butler.ui.components.ButlerDialogSurface
import illyan.butler.ui.components.CopiedToKeyboardTooltip
import illyan.butler.ui.components.TooltipElevatedCard
import illyan.butler.ui.components.smallDialogWidth
import illyan.butler.ui.dialog.LocalDialogDismissRequest
import illyan.butler.ui.theme.ThemeScreen
import illyan.butler.util.log.randomUUID
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class ProfileDialogScreen : Screen {
    @Composable
    override fun Content() {
        ProfileDialogScreen(
            screenModel = koinScreenModel<ProfileScreenModel>()
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ProfileDialogScreen(
    screenModel: ProfileScreenModel
) {
    val userUUID by screenModel.userUUID.collectAsState()
    val isUserSignedIn by screenModel.isUserSignedIn.collectAsState()
    val isUserSigningOut by screenModel.isUserSigningOut.collectAsState()
    val userPhotoUrl by screenModel.userPhotoUrl.collectAsState()
    val email by screenModel.userEmail.collectAsState()
    val phone by screenModel.userPhoneNumber.collectAsState()
    val name by screenModel.userName.collectAsState()
    val confidentialInfo = listOf(
        stringResource(Res.string.name) to name,
        stringResource(Res.string.email) to email,
        stringResource(Res.string.phone) to phone
    )
    val navigator = LocalNavigator.currentOrThrow
    ProfileDialogContent(
        userUUID = userUUID,
        isUserSignedIn = isUserSignedIn,
        isUserSigningOut = isUserSigningOut,
        userPhotoUrl = userPhotoUrl,
        confidentialInfo = confidentialInfo,
        showConfidentialInfoInitially = false,
        onSignOut = screenModel::signOut,
//        onShowLoginScreen = { navigator.push(LoginScreen()) },
//        onShowAboutScreen = { navigator.push(AboutDialogScreen) },
//        onShowSettingsScreen = { navigator.push(UserSettingsDialogScreen) },
        resetTutorialAndSignOut = screenModel::resetTutorialAndSignOut
    )
}

@Composable
fun ProfileDialogContent(
    modifier: Modifier = Modifier,
    userUUID: String? = null,
    isUserSignedIn: Boolean? = null,
    isUserSigningOut: Boolean = false,
    userPhotoUrl: String? = null,
    confidentialInfo: List<Pair<String, String?>> = emptyList(),
    showConfidentialInfoInitially: Boolean = false,
    onSignOut: () -> Unit = {},
    onShowLoginScreen: () -> Unit = {},
    onShowAboutScreen: () -> Unit = {},
    onShowSettingsScreen: () -> Unit = {},
    resetTutorialAndSignOut: () -> Unit = {},
) {
    var showConfidentialInfo by remember { mutableStateOf(showConfidentialInfoInitially) }
    ButlerDialogContent(
        modifier = modifier.smallDialogWidth().fillMaxWidth(),
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
//                modifier = Modifier.align(Alignment.End),
                isUserSignedIn = isUserSignedIn,
                isUserSigningOut = isUserSigningOut,
                onLogin = onShowLoginScreen,
                onSignOut = onSignOut,
                onShowSettingsScreen = onShowSettingsScreen,
                onShowAboutScreen = onShowAboutScreen,
                resetTutorialAndSignOut = resetTutorialAndSignOut
            )
        },
        containerColor = Color.Transparent,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalResourceApi::class)
@Composable
fun ProfileButtons(
    modifier: Modifier = Modifier,
    isUserSignedIn: Boolean? = null,
    isUserSigningOut: Boolean = false,
    onShowSettingsScreen: () -> Unit = {},
    onShowAboutScreen: () -> Unit = {},
    onLogin: () -> Unit = {},
    onSignOut: () -> Unit = {},
    resetTutorialAndSignOut: () -> Unit = {},
) {
    val onDialogClosed = LocalDialogDismissRequest.current
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ProfileMenu(
            onShowSettingsScreen = onShowSettingsScreen,
            onShowAboutScreen = onShowAboutScreen,
        )
        Row(
            modifier = Modifier
//                .weight(1f)
                .align(Alignment.Bottom),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { onDialogClosed() }) {
                Text(text = stringResource(Res.string.close))
            }
            if (isUserSignedIn == true) {
                TextButton(
                    enabled = !isUserSigningOut,
                    onClick = onSignOut,
                ) {
                    Text(text = stringResource(Res.string.sign_out))
                }
                Button(
                    onClick = resetTutorialAndSignOut
                ) {
                    Text(text = stringResource(Res.string.reset_tutorial_and_sign_out))
                }
            } else if (isUserSignedIn == false) {
                Button(
                    onClick = onLogin
                ) {
                    Text(text = stringResource(Res.string.login))
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

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PreviewProfileDialogScreen(
    name: String = "Illyan",
    email: String = "illyan@google.com",
    phone: String = "+123456789",
) {
    ThemeScreen {
        Column {
            ButlerDialogSurface {
                ProfileDialogContent(
                    modifier = Modifier,
                    userUUID = randomUUID(),
                    userPhotoUrl = null,
                    confidentialInfo = listOf(
                        stringResource(Res.string.name) to name,
                        stringResource(Res.string.email) to email, // I wish one day :)
                        stringResource(Res.string.phone) to phone
                    ),
                    showConfidentialInfoInitially = true
                )
            }
        }
    }.Content()
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class,
    ExperimentalResourceApi::class
)
@Composable
fun ProfileTitleScreen(
    modifier: Modifier = Modifier,
    userUUID: String? = null,
    isUserSignedIn: Boolean? = null,
    showConfidentialInfo: Boolean = false,
    userPhotoUrl: String? = null,
) {
    val clipboard = LocalClipboardManager.current
    FlowRow {
        Column {
            Text(text = stringResource(Res.string.profile))
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
                                    text = stringResource(Res.string.locked),
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
                            infoName = stringResource(Res.string.user_id),
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
//            modifier = Modifier.fillMaxWidth(),
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
//                modifier = Modifier.fillMaxWidth(),
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

@OptIn(ExperimentalResourceApi::class)
@Composable
fun UserInfo(
    modifier: Modifier = Modifier,
    infoName: String = stringResource(Res.string.unknown),
    info: String = stringResource(Res.string.unknown),
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
                text = if (it) info else stringResource(Res.string.hidden_field_string),
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
