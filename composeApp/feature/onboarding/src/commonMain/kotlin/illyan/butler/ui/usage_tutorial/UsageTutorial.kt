package illyan.butler.ui.usage_tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.domain.model.DomainModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.create_new_chat
import illyan.butler.generated.resources.new_chat_tutorial
import illyan.butler.ui.new_chat.ModelListItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun UsageTutorial(
    onTutorialDone: () -> Unit,
) {
    ButlerDialogContent(
        modifier = Modifier.widthIn(max = 480.dp),
        title = {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(Res.string.create_new_chat)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.new_chat_tutorial),
                    style = MaterialTheme.typography.bodyMedium
                )
                ModelListItem(
                    model = DomainModel(
                        id = "chefgpt",
                        name = "ChefGPT",
                        description = "ChefGPT is a chatbot that can help you with recipes and cooking tips.",
                        greetingMessage = null,
                        author = "Illyan"
                    ),
                    providers = listOf("https://api.chefgpt.com/", "https://api.cook.ai/"),
                    isSelfHostAvailable = true,
                    selectModelWithProvider = { onTutorialDone() }
                )
            }
        },
        buttons = {
            ButlerMediumSolidButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onTutorialDone
            ) {
                Text(text = stringResource(Res.string.close))
            }
        },
        containerColor = Color.Transparent,
    )
}
