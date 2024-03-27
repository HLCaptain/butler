package illyan.butler.ui.onboarding

data class OnBoardingState(
    val isHostSelected: Boolean = false,
    val isUserSignedIn: Boolean = false,
    val isTutorialDone: Boolean = false
)