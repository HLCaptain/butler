package illyan.butler.repository

import dev.gitlive.firebase.auth.FirebaseAuth
import org.koin.core.annotation.Single

@Single
class UserRepository(
    private val auth: FirebaseAuth,
) {
    fun getSignedInUserFlow() = auth.authStateChanged

    suspend fun anonymousSignIn() {
        auth.signInAnonymously()
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
    }

    suspend fun createUserWithEmailAndPassword(email: String, password: String): Boolean {
        return auth.createUserWithEmailAndPassword(email, password).user != null
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
    }

    suspend fun sendPasswordResetEmailToCurrentUser() {
        auth.currentUser?.email?.let { email ->
            auth.sendPasswordResetEmail(email)
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun delete() {
        auth.currentUser?.delete()
    }
}