#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
#parse("File Header.java")
data class Domain${NAME}(
    val uuid: String,
    val ownerUUID: String
)