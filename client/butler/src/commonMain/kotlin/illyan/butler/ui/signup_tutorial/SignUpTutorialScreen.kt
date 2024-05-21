package illyan.butler.ui.signup_tutorial

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.butler_logo_alternative
import illyan.butler.generated.resources.sign_up
import illyan.butler.generated.resources.sign_up_tutorial
import illyan.butler.ui.components.largeDialogWidth
import illyan.butler.ui.login.LoginScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class SignUpTutorialScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Make your Compose Multiplatform UI

        // 1. Show sign up tutorial
        // 2. Show sign up screen
        // 3. Go back on user authentication

        Column(
            modifier = Modifier.padding(16.dp).safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)).weight(1f, fill = false),
                contentScale = ContentScale.Fit,
                painter = painterResource(Res.drawable.butler_logo_alternative),
                contentDescription = "Butler Alternative Logo"
            )
            Column(
                modifier = Modifier.largeDialogWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.sign_up_tutorial),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Button(onClick = { navigator.push(LoginScreen()) }) {
                    Text(text = stringResource(Res.string.sign_up))
                }
            }
        }
    }
}

val LocalSignInCallback = compositionLocalOf { {} }