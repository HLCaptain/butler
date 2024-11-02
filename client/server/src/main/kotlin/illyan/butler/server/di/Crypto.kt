package illyan.butler.server.di

import org.koin.core.annotation.Single
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Single
fun providePasswordEncoder(): PasswordEncoder {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
}