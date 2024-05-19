package illyan.butler.di

import illyan.butler.ui.chat_detail.ChatDetailViewModel
import illyan.butler.ui.chat_list.ChatListViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

fun getViewModelModule() = module {
    viewModelOf(::ChatListViewModel)
    viewModelOf(::ChatDetailViewModel)
}