package illyan.butler.ui.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import coil3.compose.AsyncImage
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import illyan.butler.core.ui.components.BooleanSetting
import illyan.butler.core.ui.components.ButlerDialogSurface
import illyan.butler.core.ui.components.ButlerDropdownMenu
import illyan.butler.core.ui.components.ButlerDropdownMenuDefaults
import illyan.butler.core.ui.components.ButlerLargeSolidButton
import illyan.butler.core.ui.components.ButlerLargeTextButton
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.ButlerMediumTextButton
import illyan.butler.core.ui.components.ButlerScrollableTabRow
import illyan.butler.core.ui.components.ButlerStatusMessage
import illyan.butler.core.ui.components.ButlerStatusMessageDefaults
import illyan.butler.core.ui.components.ButlerTextField
import illyan.butler.core.ui.components.DropdownSetting
import illyan.butler.core.ui.components.MediumMenuButton
import illyan.butler.core.ui.components.SmallMenuButton
import illyan.butler.core.ui.components.butlerUriHandler
import illyan.butler.core.ui.components.largeDialogSize
import illyan.butler.core.ui.theme.canUseDynamicColors
import illyan.butler.core.ui.utils.plus
import illyan.butler.domain.model.AppSettings
import illyan.butler.domain.model.Theme
import illyan.butler.domain.model.User
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.about
import illyan.butler.generated.resources.about_x
import illyan.butler.generated.resources.account
import illyan.butler.generated.resources.add_filter
import illyan.butler.generated.resources.analytics
import illyan.butler.generated.resources.app_description_brief
import illyan.butler.generated.resources.app_name
import illyan.butler.generated.resources.auth
import illyan.butler.generated.resources.customization
import illyan.butler.generated.resources.dark
import illyan.butler.generated.resources.dashboard
import illyan.butler.generated.resources.day_night_cycle
import illyan.butler.generated.resources.device
import illyan.butler.generated.resources.disabled
import illyan.butler.generated.resources.display_name
import illyan.butler.generated.resources.dynamic_color
import illyan.butler.generated.resources.email
import illyan.butler.generated.resources.enabled
import illyan.butler.generated.resources.filters
import illyan.butler.generated.resources.full_name
import illyan.butler.generated.resources.history
import illyan.butler.generated.resources.libraries
import illyan.butler.generated.resources.light
import illyan.butler.generated.resources.login
import illyan.butler.generated.resources.no_logged_in_users_status_message_description
import illyan.butler.generated.resources.no_logged_in_users_status_message_title
import illyan.butler.generated.resources.phone
import illyan.butler.generated.resources.photo_url
import illyan.butler.generated.resources.provider_url
import illyan.butler.generated.resources.reset
import illyan.butler.generated.resources.save
import illyan.butler.generated.resources.select_user
import illyan.butler.generated.resources.support_app_description
import illyan.butler.generated.resources.system
import illyan.butler.generated.resources.theme
import illyan.butler.generated.resources.username
import illyan.butler.shared.model.chat.FilterOption
import kotlinx.collections.immutable.PersistentSet
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

enum class DashboardTab(
    val titleRes: StringResource,
    val imageVector: ImageVector,
) {
    Account(
        titleRes = Res.string.account,
        imageVector = Icons.Rounded.AccountCircle
    ),
    Customization(
        titleRes = Res.string.customization,
        imageVector = Icons.Rounded.Palette
    ),
    History(
        titleRes = Res.string.history,
        imageVector = Icons.Rounded.History
    ),
    Analytics(
        titleRes = Res.string.analytics,
        imageVector = Icons.Rounded.BarChart
    ),
    About(
        titleRes = Res.string.about,
        imageVector = Icons.Rounded.Info
    ),
    Auth(
        titleRes = Res.string.auth,
        imageVector = Icons.Rounded.Key
    )
}

@Composable
fun Dashboard(
    onBack: () -> Unit,
    onAddAuth: () -> Unit,
    libraries: State<Libs?>
) {
    val viewModel = koinViewModel<DashboardViewModel>()
    val state by viewModel.state.collectAsState()
    DashboardScaffold(
        state = state,
        onUserSelected = viewModel::selectUser,
        onBack = onBack,
        saveUser = viewModel::saveUser,
        onAddAuth = onAddAuth,
        onChangeAppSettings = viewModel::changeAppSettings,
        libraries = libraries
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun DashboardScaffold(
    state: DashboardState,
    onUserSelected: (User?) -> Unit,
    onBack: () -> Unit,
    saveUser: (User) -> Unit,
    onChangeAppSettings: (AppSettings) -> Unit,
    onAddAuth: () -> Unit,
    libraries: State<Libs?>
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isCompact = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.dashboard),) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val hazeState = remember { HazeState() }
        Row(modifier = Modifier.then(if (!isCompact) Modifier.padding(horizontal = 32.dp) else Modifier)) {
            if (!isCompact) {
                Column(
                    modifier = Modifier.padding(innerPadding + PaddingValues(top = 56.dp))
                ) {
                    ProfileDisplayAndSelector(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        selectedUser = state.selectedUser,
                        users = state.users,
                        onUserSelected = onUserSelected,
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding + PaddingValues(top = 56.dp)), // Adjust for TabRow height
                ) {
                    if (isCompact) {
                        ProfileDisplayAndSelector(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            selectedUser = state.selectedUser,
                            users = state.users,
                            onUserSelected = onUserSelected,
                        )
                    }
                    // Cannot use HorizontalPager yet, because it straight up crashes on window layout changes
                    Crossfade(selectedTabIndex) { selectedTabIndex ->
                        when (DashboardTab.entries[selectedTabIndex]) {
                            DashboardTab.Account -> AccountTabContent(
                                selectedUser = state.selectedUser,
                                onUserDataSaved = saveUser,
                                onLogin = onAddAuth,
                                appSettings = state.appSettings,
                                onChangeAppSettings = onChangeAppSettings,
                            )
                            DashboardTab.Customization -> CustomizationTabContent(
                                appSettings = state.appSettings,
                                onChangeAppSettings = onChangeAppSettings
                            )
                            DashboardTab.History -> HistoryTabContent()
                            DashboardTab.Analytics -> AnalyticsTabContent()
                            DashboardTab.About -> AboutTabContent(
                                libraries = libraries
                            )
                            DashboardTab.Auth -> AuthTabContent()
                        }
                    }
                }
                Row(
                    modifier = Modifier.padding(innerPadding + PaddingValues(8.dp))
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    ButlerScrollableTabRow(
                        modifier = Modifier.padding(6.dp).clip(CircleShape).hazeEffect(hazeState, style = HazeMaterials.thin()),
                        selectedIndex = selectedTabIndex,
                        onIndexChanged = { selectedTabIndex = it },
                        tabContent = {
                            Text(text = stringResource(DashboardTab.entries[it].titleRes))
                        },
                        numberOfTabs = DashboardTab.entries.size
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AccountTabContent(
    selectedUser: User?,
    appSettings: AppSettings?,
    onUserDataSaved: (User) -> Unit,
    onChangeAppSettings: (AppSettings) -> Unit,
    onLogin: () -> Unit,
) {
    Column {
        if (selectedUser != null) {
            AccountUserTabContent(
                selectedUser = selectedUser,
                onUserDataSaved = onUserDataSaved
            )
        } else {
            AccountDeviceTabContent(
                onLogin = onLogin,
                appSettings = appSettings,
                onChangeAppSettings = onChangeAppSettings
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun AccountUserTabContent(
    selectedUser: User,
    onUserDataSaved: (User) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.account),
            style = MaterialTheme.typography.titleLarge,
        )
        var displayName by rememberSaveable { mutableStateOf(selectedUser.displayName) }
        ButlerTextField(
            value = displayName ?: "",
            onValueChange = { displayName = it },
            label = { Text(stringResource(Res.string.display_name)) },
            isOutlined = true,
        )
        var email by rememberSaveable { mutableStateOf(selectedUser.email) }
        ButlerTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(Res.string.email)) },
            isOutlined = true,
            readOnly = true
        )
        var endpoint by rememberSaveable { mutableStateOf(selectedUser.endpoint) }
        ButlerTextField(
            value = endpoint,
            onValueChange = { endpoint = it },
            label = { Text(stringResource(Res.string.provider_url)) },
            isOutlined = true,
            readOnly = true
        )
        var photoUrl by rememberSaveable { mutableStateOf(selectedUser.photoUrl) }
        ButlerTextField(
            value = photoUrl ?: "",
            onValueChange = { photoUrl = it },
            label = { Text(stringResource(Res.string.photo_url)) },
            isOutlined = true,
        )
        var phone by rememberSaveable { mutableStateOf(selectedUser.phone) }
        ButlerTextField(
            value = phone ?: "",
            onValueChange = { phone = it },
            label = { Text(stringResource(Res.string.phone)) },
            isOutlined = true,
        )
        var fullName by rememberSaveable { mutableStateOf(selectedUser.fullName) }
        ButlerTextField(
            value = fullName ?: "",
            onValueChange = { fullName = it },
            label = { Text(stringResource(Res.string.full_name)) },
            isOutlined = true,
        )
        var username by rememberSaveable { mutableStateOf(selectedUser.username) }
        ButlerTextField(
            value = username ?: "",
            onValueChange = { username = it },
            label = { Text(stringResource(Res.string.username)) },
            isOutlined = true,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ButlerLargeSolidButton(
                onClick = { onUserDataSaved(selectedUser) },
                enabled = selectedUser.displayName != displayName ||
                        selectedUser.email != email ||
                        selectedUser.endpoint != endpoint ||
                        selectedUser.photoUrl != photoUrl ||
                        selectedUser.phone != phone ||
                        selectedUser.fullName != fullName ||
                        selectedUser.username != username
            ) {
                Text(text = stringResource(Res.string.save))
            }
            ButlerLargeTextButton(
                onClick = {
                    // Reset the user data to the original values
                    displayName = selectedUser.displayName
                    email = selectedUser.email
                    endpoint = selectedUser.endpoint
                    photoUrl = selectedUser.photoUrl
                    phone = selectedUser.phone
                    fullName = selectedUser.fullName
                    username = selectedUser.username
                }
            ) {
                Text(text = stringResource(Res.string.reset))
            }
        }
        Text(stringResource(Res.string.filters))
        val filters = selectedUser.filters.toList()
        val regexFilters = remember(filters) {
            filters.mapIndexedNotNull { index, filter ->
                (filter as? FilterOption.RegexFilter)?.let { index to it }
            }
        }
        val patterns = remember(regexFilters) {
            mutableStateListOf(*regexFilters.map { it.second.pattern }.toTypedArray())
        }

        patterns.forEachIndexed { index, pattern ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ButlerTextField(
                    value = pattern,
                    onValueChange = { patterns[index] = it },
                    isOutlined = true,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = {
                        // Remove the filter option
                        patterns.removeAt(index)
                        // Update the filters with the new pattern
                        val newFilters = filters.toMutableList()
                        newFilters.removeAt(regexFilters[index].first)
                        onUserDataSaved(
                            selectedUser.copy(
                                filters = newFilters.toSet()
                            )
                        )
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                    )
                }
            }
        }

        ButlerMediumSolidButton(
            onClick = {
                // Add a new filter option
                patterns.add("")
                // Update the filters with the new pattern
                val newFilters = filters.toMutableList()
                newFilters.add(FilterOption.RegexFilter(""))
                onUserDataSaved(
                    selectedUser.copy(
                        filters = newFilters.toSet()
                    )
                )
            }
        ) {
            Text(text = stringResource(Res.string.add_filter))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ButlerMediumSolidButton(
                onClick = {
                    val newOptionsList = filters.toMutableList()
                    patterns.forEachIndexed { i, newPattern ->
                        val originalListIndex = regexFilters[i].first
                        newOptionsList[originalListIndex] = FilterOption.RegexFilter(newPattern)
                    }
                    onUserDataSaved(
                        selectedUser.copy(
                            filters = newOptionsList.toSet()
                        )
                    )
                },
                enabled = patterns.toList() != regexFilters.map { it.second.pattern }
            ) {
                Text(text = stringResource(Res.string.save))
            }
            ButlerMediumTextButton(
                onClick = {
                    patterns.clear()
                    patterns.addAll(regexFilters.map { it.second.pattern })
                }
            ) {
                Text(text = stringResource(Res.string.reset))
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                // Save the updated filters when the composable is disposed
                val newFilters = filters.toMutableList()
                patterns.forEachIndexed { i, newPattern ->
                    val originalListIndex = regexFilters[i].first
                    newFilters[originalListIndex] = FilterOption.RegexFilter(newPattern)
                }
                onUserDataSaved(
                    selectedUser.copy(
                        filters = newFilters.toSet()
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun AccountDeviceTabContent(
    appSettings: AppSettings?,
    onChangeAppSettings: (AppSettings) -> Unit,
    onLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val isCompact = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
        ButlerStatusMessage(
            modifier = Modifier
                .then(if (isCompact) Modifier.fillMaxWidth() else Modifier.widthIn(max = 480.dp)),
            imageVector = Icons.Rounded.Info,
            title = { Text(stringResource(Res.string.no_logged_in_users_status_message_title)) },
            description = { Text(stringResource(Res.string.no_logged_in_users_status_message_description)) },
            actions = {
                ButlerStatusMessageDefaults.StatusMessageButtons(
                    colors = ButlerStatusMessageDefaults.statusMessagePrimaryColors(),
                    primaryButtonText = stringResource(Res.string.login),
                    onPrimaryClick = onLogin
                )
            },
            colors = ButlerStatusMessageDefaults.statusMessagePrimaryColors()
        )
        appSettings?.let {
            Text(stringResource(Res.string.filters))
            val filters = appSettings.filterConfiguration.filterOptions.toList()
            val regexFilters = remember(filters) {
                filters.mapIndexedNotNull { index, (filter, _) ->
                    (filter as? FilterOption.RegexFilter)?.let { index to it }
                }
            }
            val patterns = remember(regexFilters) {
                mutableStateListOf(*regexFilters.map { it.second.pattern }.toTypedArray())
            }

            patterns.forEachIndexed { index, pattern ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ButlerTextField(
                        value = pattern,
                        onValueChange = { patterns[index] = it },
                        isOutlined = true,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            // Remove the filter option
                            patterns.removeAt(index)
                            onChangeAppSettings(
                                appSettings.copy(
                                    filterConfiguration = appSettings.filterConfiguration.copy(
                                        filterOptions = appSettings.filterConfiguration.filterOptions - regexFilters[index].second
                                    )
                                )
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ButlerMediumSolidButton(
                    onClick = {
                        val newOptionsList = filters.toMutableList()
                        patterns.forEachIndexed { i, newPattern ->
                            val originalListIndex = regexFilters[i].first
                            val (_, checked) = newOptionsList[originalListIndex]
                            newOptionsList[originalListIndex] = FilterOption.RegexFilter(newPattern) to checked
                        }
                        onChangeAppSettings(
                            appSettings.copy(
                                filterConfiguration = appSettings.filterConfiguration.copy(
                                    filterOptions = newOptionsList.toMap()
                                )
                            )
                        )
                    },
                    enabled = patterns.toList() != regexFilters.map { it.second.pattern }
                ) {
                    Text(text = stringResource(Res.string.save))
                }
                ButlerMediumTextButton(
                    onClick = {
                        patterns.clear()
                        patterns.addAll(regexFilters.map { it.second.pattern })
                    }
                ) {
                    Text(text = stringResource(Res.string.reset))
                }
            }
            ButlerMediumSolidButton(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null
                    )
                },
                onClick = {
                    onChangeAppSettings(
                        appSettings.copy(
                            filterConfiguration = appSettings.filterConfiguration.copy(
                                filterOptions = appSettings.filterConfiguration.filterOptions +
                                        (FilterOption.RegexFilter("") to true)
                            )
                        )
                    )
                }
            ) {
                Text(text = stringResource(Res.string.add_filter))
            }
            DisposableEffect(Unit) {
                onDispose {
                    onChangeAppSettings(
                        appSettings.copy(
                            filterConfiguration = appSettings.filterConfiguration.copy(
                                filterOptions = appSettings.filterConfiguration.filterOptions.filter {
                                    if (it.key is FilterOption.RegexFilter) {
                                        val regexFilter = it.key as FilterOption.RegexFilter
                                        regexFilter.pattern.isNotBlank()
                                    } else {
                                        true
                                    }
                                }
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CustomizationTabContent(
    appSettings: AppSettings?,
    onChangeAppSettings: (AppSettings) -> Unit
) {
    // Display customization options
    Column {
        UserSettings(
            modifier = Modifier.padding(32.dp),
            appSettings = appSettings,
            onChangeAppSettings = onChangeAppSettings
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun UserSettings(
    modifier: Modifier = Modifier,
    appSettings: AppSettings?,
    onChangeAppSettings: (AppSettings) -> Unit,
) {
    appSettings?.let { appSettings ->
        val preferences = appSettings.preferences
        Column(modifier = modifier) {
            var isDropdownOpen by rememberSaveable { mutableStateOf(false) }
            DropdownSetting(
                settingName = stringResource(Res.string.theme),
                isDropdownOpen = isDropdownOpen,
                onToggleDropdown = { isDropdownOpen = !isDropdownOpen },
                selectValue = { theme ->
                    onChangeAppSettings(
                        appSettings.copy(
                            preferences = preferences.copy(theme = theme)
                        )
                    )
                    isDropdownOpen = false
                },
                selectedValue = preferences.theme,
                values = Theme.entries.toList(),
                text = { theme ->
                    Text(
                        text = when (theme) {
                            Theme.System -> stringResource(Res.string.system)
                            Theme.Light -> stringResource(Res.string.light)
                            Theme.Dark -> stringResource(Res.string.dark)
                            Theme.DayNightCycle -> stringResource(Res.string.day_night_cycle)
                        }
                    )
                },
                getValueLeadingIcon = { theme ->
                    when (theme) {
                        Theme.System -> Icons.Rounded.Settings
                        Theme.Light -> Icons.Rounded.LightMode
                        Theme.Dark -> Icons.Rounded.DarkMode
                        Theme.DayNightCycle -> Icons.Rounded.Schedule
                    }
                }
            )
            BooleanSetting(
                value = canUseDynamicColors() && preferences.dynamicColorEnabled,
                onValueChange = {
                    onChangeAppSettings(
                        appSettings.copy(
                            preferences = preferences.copy(dynamicColorEnabled = it)
                        )
                    )
                },
                title = stringResource(Res.string.dynamic_color),
                enabledText = stringResource(Res.string.enabled),
                disabledText = stringResource(Res.string.disabled),
                enabled = canUseDynamicColors()
            )
        }
    }
}

@Composable
fun HistoryTabContent() {
    // Display user history
    Column {
        repeat(30) {
            Text(
                text = stringResource(Res.string.history),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun AnalyticsTabContent() {
    // Display analytics information
    Column {
        repeat(30) {
            Text(
                text = stringResource(Res.string.analytics),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutTabContent(
    libraries: State<Libs?>
) {
    // Display about information
    val navController = rememberNavController()
    NavHost(
        startDestination = "about",
        navController = navController
    ) {
        dialog("libraries") {
            val libs by libraries
            ButlerDialogSurface(modifier = Modifier.largeDialogSize()) {
                LibrariesContainer(libraries = libs, Modifier.fillMaxSize())
            }
        }
        composable("about") {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                        Text(text = stringResource(Res.string.about_x, stringResource(Res.string.app_name)))
                    }
                }
                Text(
                    text = stringResource(Res.string.app_description_brief),
                    style = MaterialTheme.typography.bodyMedium,
                )
                MediumMenuButton(
                    text = stringResource(Res.string.libraries),
                    onClick = { navController.navigate("libraries") },
                )
                val uriHandler = butlerUriHandler()
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    ButlerLargeSolidButton(
                        onClick = { uriHandler?.openUri("https://github.com/HLCaptain/butler") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.VolunteerActivism, contentDescription = null)
                        }
                    ) {
                        Text(text = stringResource(Res.string.support_app_description))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

}

@Composable
fun AuthTabContent() {
    // Display authentication options
    Column {
        repeat(30) {
            Text(
                text = stringResource(Res.string.auth),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun User.displayName() = displayName ?: username ?: fullName

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ProfileDisplayAndSelector(
    modifier: Modifier = Modifier,
    selectedUser: User?,
    users: PersistentSet<User>,
    onUserSelected: (User?) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedUser != null) {
            AsyncImage(
                modifier = Modifier
//                    .sharedElement(
//                        rememberSharedContentState("profile_image"),
//                        animatedVisibilityScope
//                    )
                    .size(128.dp),
                model = selectedUser.photoUrl,
                contentDescription = null,
                placeholder = rememberVectorPainter(Icons.Default.AccountCircle),
                error = rememberVectorPainter(Icons.Default.AccountCircle)
            )
        } else {
            Icon(
                modifier = Modifier
//                    .sharedElement(
//                        rememberSharedContentState("profile_image"),
//                        animatedVisibilityScope
//                    )
                    .size(128.dp),
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier,
//                    .sharedElement(
//                        rememberSharedContentState("profile_name"),
//                        animatedVisibilityScope
//                    ),
                text = selectedUser?.displayName() ?: selectedUser?.email ?: stringResource(Res.string.device),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            selectedUser?.let {
                selectedUser.displayName()?.let {
                    Text(
                        modifier = Modifier,
//                            .sharedElement(
//                                rememberSharedContentState("profile_email"),
//                                animatedVisibilityScope
//                            ),
                        text = selectedUser.email,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    modifier = Modifier,
//                        .sharedElement(
//                            rememberSharedContentState("profile_endpoint"),
//                            animatedVisibilityScope
//                        ),
                    text = selectedUser.endpoint,
                    textAlign = TextAlign.Center
                )
            }
            var isDropdownOpen by rememberSaveable { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = isDropdownOpen,
                onExpandedChange = { isDropdownOpen = it }
            ) {
                SmallMenuButton(
                    modifier = Modifier,
//                        .sharedElement(
//                            rememberSharedContentState("profile_dropdown"),
//                            animatedVisibilityScope
//                        ),
                    enabled = users.isNotEmpty(),
                ) {
                    Text(
                        text = stringResource(Res.string.select_user)
                    )
                }
                ButlerDropdownMenu(
                    expanded = isDropdownOpen,
                    onDismissRequest = { isDropdownOpen = false },
                    matchTextFieldWidth = false
                ) {
                    ButlerDropdownMenuDefaults.DropdownMenuList(
                        values = (0..users.size).toList(),
                        selectedValue = selectedUser?.let { users.indexOf(it) } ?: users.size,
                        selectValue = {
                            onUserSelected(users.elementAtOrNull(it))
                            isDropdownOpen = false
                        },
                        valueText = {
                            val user = users.elementAtOrNull(it)
                            user?.displayName() ?: stringResource(Res.string.device)
                        },
                        getValueLeadingIcon = { index ->
                            val selectedUserIndex =
                                selectedUser?.let { users.indexOf(it) } ?: users.size
                            if (index == selectedUserIndex) {
                                Icons.Rounded.Check
                            } else {
                                null
                            }
                        },
                        getValueTrailingIcon = { null },
                        onDismissRequest = { isDropdownOpen = false },
                    )
                }
            }
        }
    }
}
