package illyan.butler.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import org.koin.core.annotation.Single

@Single
fun provideFirebaseAuth() = Firebase.auth

@Single
fun provideFirebaseFirestore() = Firebase.firestore