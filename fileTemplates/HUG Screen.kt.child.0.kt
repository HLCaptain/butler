#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
#parse("File Header.java")
data class ${NAME}State(
    val dataFlow1: Boolean,
    val dataFlow2: Boolean
)