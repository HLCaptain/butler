package illyan.butler.di

import illyan.butler.ui.auth.AuthViewModel
import illyan.butler.ui.chat_detail.ChatDetailViewModel
import illyan.butler.ui.chat_details.ChatDetailsViewModel
import illyan.butler.ui.chat_list.ChatListViewModel
import illyan.butler.ui.home.HomeViewModel
import illyan.butler.ui.login.LoginViewModel
import illyan.butler.ui.new_chat.NewChatViewModel
import illyan.butler.ui.permission.PermissionRequestViewModel
import illyan.butler.ui.profile.ProfileViewModel
import illyan.butler.ui.select_host.SelectHostViewModel
import illyan.butler.ui.select_host_tutorial.SelectHostTutorialViewModel
import illyan.butler.ui.settings.user.UserSettingsViewModel
import illyan.butler.ui.signup.SignUpViewModel
import illyan.butler.ui.theme.ThemeViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

fun getViewModelModule() = module {
    viewModelOf(::ChatListViewModel)
    viewModelOf(::ChatDetailViewModel)
    viewModelOf(::ThemeViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SelectHostTutorialViewModel)
    viewModelOf(::SelectHostViewModel)
    viewModelOf(::UserSettingsViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ChatDetailsViewModel)
    viewModelOf(::PermissionRequestViewModel)
    viewModelOf(::NewChatViewModel)
}