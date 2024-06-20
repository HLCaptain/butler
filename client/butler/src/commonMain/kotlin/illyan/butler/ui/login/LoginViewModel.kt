/*
 * Copyright (c) 2022-2023 Balázs Püspök-Kiss (Illyan)
 *
 * Jay is a driver behaviour analytics app.
 *
 * This file is part of Jay.
 *
 * Jay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jay.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.butler.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.KoinNames
import illyan.butler.manager.AuthManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class LoginViewModel(
    private val authManager: AuthManager,
    @Named(KoinNames.DispatcherIO) private val dispatcherIO: CoroutineDispatcher,
): ViewModel() {
    val state = combine(
        authManager.isUserSigningIn,
        authManager.isUserSignedIn
    ) { signingIn, signedIn ->
        LoginScreenState(
            isSignedIn = signedIn,
            isSigningIn = signingIn
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = LoginScreenState()
    )

    fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch(dispatcherIO) {
            authManager.signInWithEmailAndPassword(email, password)
        }
    }
}