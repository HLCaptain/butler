package illyan.butler.services.ai.data.mapping

import illyan.butler.services.ai.data.model.ai.ModelDto
import illyan.butler.services.ai.data.model.openai.Model

fun Model.toModelDto() = ModelDto(
    name = null,
    id = id,
    description = null,
    greetingMessage = null,
    author = ownedBy,
)