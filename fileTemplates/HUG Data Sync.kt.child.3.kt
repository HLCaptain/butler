#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
#parse("File Header.java")
import kotlinx.serialization.Serializable

@Serializable
data class Network${NAME}(
    val uuid: String,
    val ownerUUID: String
)
