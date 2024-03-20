package illyan.butler.services.identity.di

import com.chrynan.krypt.hash.Hasher
import com.chrynan.krypt.hash.argon.argon2
import org.koin.core.annotation.Single

@Single
fun provideArgon2Hasher() = Hasher.argon2()