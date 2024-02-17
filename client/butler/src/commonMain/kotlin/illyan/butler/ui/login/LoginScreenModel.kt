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

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import illyan.butler.di.NamedCoroutineDispatcherIO
import illyan.butler.manager.AuthManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class LoginScreenModel(
    private val authManager: AuthManager,
    @NamedCoroutineDispatcherIO private val dispatcherIO: CoroutineDispatcher,
): ScreenModel {
    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn = _isSigningIn.asStateFlow()

    val isUserSignedIn = authManager.isUserSignedIn
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    fun signInWithEmailAndPassword(email: String, password: String) {
        screenModelScope.launch(dispatcherIO) {
            authManager.signInWithEmailAndPassword(email, password)
        }
    }

    fun signUpWithEmailAndPassword(email: String, password: String) {
        screenModelScope.launch(dispatcherIO) {
            if (authManager.createUserWithEmailAndPassword(email, password)) {
                authManager.signInWithEmailAndPassword(email, password)
            }
        }
    }
}