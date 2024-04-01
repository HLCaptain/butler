package illyan.butler.services.ai.data.mapping

import com.aallam.openai.api.model.Model
import illyan.butler.services.ai.data.model.ai.ModelDto

fun Model.toModelDto() = ModelDto(
    name = id.id,
    id = id.id,
    description = null,
    greetingMessage = null,
    author = ownedBy,
)