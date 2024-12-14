package illyan.butler.ui.onboard_flow

import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import illyan.butler.core.ui.components.ButlerSmallSolidButton
import illyan.butler.core.ui.components.largeDialogWidth
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.back
import illyan.butler.generated.resources.next
import illyan.butler.ui.select_host_tutorial.SelectHostTutorial
import illyan.butler.ui.signup_tutorial.SignUpTutorial
import illyan.butler.ui.usage_tutorial.UsageTutorial
import illyan.butler.ui.welcome.Welcome
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardFlow(
    authSuccessEnded: () -> Unit,
    onTutorialDone: () -> Unit,
) {
    val navController = rememberNavController()
    val animationTime = 200
    val navigationOrder = listOf(
        "welcome",
        "selectHostTutorial",
        "signUpTutorial",
        "usageTutorial"
    )
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            NavHost(
                navController = navController,
                contentAlignment = Alignment.Center,
                sizeTransform = { SizeTransform(clip = false) },
                startDestination = "welcome",
                enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
                popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
                exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
                popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
            ) {
                composable("welcome") {
                    Welcome {
                        navController.navigate("selectHostTutorial")
                    }
                }
                composable("selectHostTutorial") {
                    SelectHostTutorial {
                        navController.navigate("signUpTutorial")
                    }
                }
                composable("signUpTutorial") {
                    SignUpTutorial {
                        authSuccessEnded()
                        navController.navigate("usageTutorial")
                    }
                }
                composable("usageTutorial") {
                    UsageTutorial(onTutorialDone = onTutorialDone)
                }
            }
        }
        val backStackEntry by navController.currentBackStackEntryAsState()
        val nextDestination = remember(backStackEntry) {
            (navigationOrder.indexOf(navController.currentDestination?.route) + 1)
        }
        val previousDestination = remember(backStackEntry) {
            navigationOrder.indexOf(navController.currentDestination?.route) - 1
        }
        val currentStep = remember(backStackEntry) {
            navigationOrder.indexOf(navController.currentDestination?.route) + 1
        }
        OnboardingProgressBar(
            modifier = Modifier.largeDialogWidth().systemBarsPadding().imePadding(),
            onNext = { navController.navigate(navigationOrder[nextDestination]) },
            onBack = { navController.navigateUp() },
            canGoForward = nextDestination < navigationOrder.size && navController.currentDestination?.route != "auth",
            canGoBack = previousDestination >= 0 && navController.currentDestination?.route != "usageTutorial",
            currentStep = currentStep,
            numberOfSteps = navigationOrder.size
        )
    }
}

@Composable
fun OnboardingProgressBar(
    modifier: Modifier = Modifier,
    onNext: () -> Unit,
    onBack: () -> Unit,
    canGoBack: Boolean = true,
    canGoForward: Boolean = true,
    currentStep: Int = 1,
    numberOfSteps: Int = 5
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row {
            ButlerSmallSolidButton(
                onClick = onBack,
                enabled = canGoBack,
            ) {
                Text(stringResource(Res.string.back))
            }
            Spacer(Modifier.weight(1f))
            ButlerSmallSolidButton(
                onClick = onNext,
                enabled = canGoForward,
            ) {
                Text(stringResource(Res.string.next))
            }
        }

        var layoutWidth by remember { mutableStateOf(0f) }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layoutWidth = placeable.width.toFloat()
                    layout(placeable.width, placeable.height) {
                        placeable.place(0, 0)
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.weight((1000 / layoutWidth - 1f).coerceAtLeast(0.1f)))
            Row(modifier = Modifier.padding(16.dp).weight(1f)) {
                for (i in 1..numberOfSteps) {
                    val animatedProgress by animateFloatAsState(
                        // Workaround for faster animation for previous values
                        // Progress is in 0..1, so negative values are OK
                        targetValue = if (i == currentStep) 1f else if (i < currentStep) 1.01f else -0.01f,
                        animationSpec = if (i == currentStep) tween(durationMillis = 1000, easing = EaseInOutQuart) else tween(durationMillis = 500, easing = EaseOutQuart),
                        label = "Current Step state"
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.height(ButlerProgressBarDefaults.ProgressLineThickness).weight(1f, fill = false),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = ButlerProgressBarDefaults.DisabledColor,
                        progress = { animatedProgress },
                        gapSize = -ButlerProgressBarDefaults.ProgressLineThickness,
                        drawStopIndicator = {}
                    )
                    if (i != numberOfSteps) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            Spacer(Modifier.weight((1000 / layoutWidth - 1f).coerceAtLeast(0.1f)))
        }
    }
}

object ButlerProgressBarDefaults {
    val ProgressLineThickness = 6.dp
    val DisabledColor: Color @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
}