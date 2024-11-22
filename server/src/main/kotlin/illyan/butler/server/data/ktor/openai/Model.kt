package illyan.butler.shared.model.openai

import com.aallam.openai.api.model.ModelPermission
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//{"object":"list","data":[{"id":"phi-2","object":"model"},{"id":"open-llama-13b-open-instruct.ggmlv3.q3_K_M.bin","object":"model"}]}
@Serializable
data class Model(
    @SerialName("id") val id: String,
    @SerialName("object") val typeObject: String? = null,
    @SerialName("created") val created: Long? = null,
    @SerialName("owned_by") val ownedBy: String? = null,
    @SerialName("permission") val permission: List<ModelPermission>? = null
)
