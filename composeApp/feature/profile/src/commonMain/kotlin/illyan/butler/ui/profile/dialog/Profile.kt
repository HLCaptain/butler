package illyan.butler.ui.profile.dialog

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.ButlerDialogSurface
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.ButlerMediumTextButton
import illyan.butler.core.ui.components.CopiedToKeyboardTooltip
import illyan.butler.core.ui.components.LocalDialogDismissRequest
import illyan.butler.core.ui.components.MenuButton
import illyan.butler.core.ui.components.TooltipElevatedCard
import illyan.butler.core.ui.components.mediumDialogWidth
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.about
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.email
import illyan.butler.generated.resources.hidden_field_string
import illyan.butler.generated.resources.locked
import illyan.butler.generated.resources.login
import illyan.butler.generated.resources.name
import illyan.butler.generated.resources.phone
import illyan.butler.generated.resources.profile
import illyan.butler.generated.resources.settings
import illyan.butler.generated.resources.sign_out
import illyan.butler.generated.resources.unknown
import illyan.butler.generated.resources.user_id
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ProfileDialog(
    onShowSettingsScreen: () -> Unit = {},
    onLogin: () -> Unit,
    onAbout: () -> Unit,
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val userUUID by viewModel.userUUID.collectAsState()
    val isUserSignedIn by viewModel.isUserSignedIn.collectAsState()
    val isUserSigningOut by viewModel.isUserSigningOut.collectAsState()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()
    val email by viewModel.userEmail.collectAsState()
    val phone by viewModel.userPhoneNumber.collectAsState()
    val name by viewModel.userName.collectAsState()
    val confidentialInfo = listOf(
        stringResource(Res.string.name) to name,
        stringResource(Res.string.email) to email,
        stringResource(Res.string.phone) to phone
    )
    ProfileDialogContent(
        userUUID = userUUID,
        isUserSignedIn = isUserSignedIn,
        isUserSigningOut = isUserSigningOut,
        userPhotoUrl = userPhotoUrl,
        confidentialInfo = confidentialInfo,
        showConfidentialInfoInitially = false,
        onSignOut = viewModel::signOut,
        onShowLoginScreen = onLogin,
        onShowAboutScreen = onAbout,
        onShowSettingsScreen = onShowSettingsScreen,
        resetTutorialAndSignOut = viewModel::resetTutorialAndSignOut
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
        modifier = modifier.mediumDialogWidth(),
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
                modifier = Modifier.fillMaxWidth(),
                isUserSignedIn = isUserSignedIn,
                isUserSigningOut = isUserSigningOut,
                onLogin = onShowLoginScreen,
                onSignOut = onSignOut,
                onShowSettingsScreen = onShowSettingsScreen,
                onShowAboutScreen = onShowAboutScreen,
                resetTutorialAndSignOut = resetTutorialAndSignOut
            )
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
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
                .weight(1f)
                .align(Alignment.Bottom),
            horizontalArrangement = Arrangement.End
        ) {
            ButlerMediumTextButton(onClick = { onDialogClosed() }) {
                Text(text = stringResource(Res.string.close))
            }
            if (isUserSignedIn == true) {
                ButlerMediumTextButton(
                    enabled = !isUserSigningOut,
                    onClick = onSignOut,
                ) {
                    Text(text = stringResource(Res.string.sign_out))
                }
//                if (BuildConfig.DEBUG) {
//                    Button(
//                        onClick = resetTutorialAndSignOut
//                    ) {
//                        Text(text = "Reset tutorial and sign out")
//                    }
//                }
            } else if (isUserSignedIn == false) {
                ButlerMediumSolidButton(
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

@OptIn(ExperimentalUuidApi::class)
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
                    modifier = Modifier,
                    userUUID = Uuid.random().toString(),
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileTitleScreen(
    modifier: Modifier = Modifier,
    userUUID: String? = null,
    isUserSignedIn: Boolean? = null,
    showConfidentialInfo: Boolean = false,
    userPhotoUrl: String? = null,
) {
    val clipboard = LocalClipboardManager.current
    FlowRow(
        modifier = modifier
    ) {
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        UserInfoList(
            confidentialInfo = confidentialInfo,
            info = info,
            showConfidentialInfo = showConfidentialInfo
        )
        ConfidentialInfoToggleButton(
            showConfidentialInfo = showConfidentialInfo,
            anyConfidentialInfo = confidentialInfo.isNotEmpty(),
            onVisibilityChanged = onConfidentialInfoVisibilityChanged
        )
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
        MenuButton(
            onClick = onShowAboutScreen,
            text = stringResource(Res.string.about)
        )
        MenuButton(
            onClick = onShowSettingsScreen,
            text = stringResource(Res.string.settings)
        )
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
