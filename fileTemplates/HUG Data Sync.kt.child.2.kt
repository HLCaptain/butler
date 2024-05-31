#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
#parse("File Header.java")
fun ${NAME}.toDomainModel() = Domain${NAME}(
    uuid = uuid,
    ownerUUID = ownerUUID
)

fun Domain${NAME}.toNetworkModel() = Network${NAME}(
    uuid = uuid,
    ownerUUID = ownerUUID
)

fun Network${NAME}.toLocalModel() = ${NAME}(
    uuid = uuid,
    ownerUUID = ownerUUID
)

fun ${NAME}.toNetworkModel() = toDomainModel().toNetworkModel()
fun Network${NAME}.toDomainModel() = toLocalModel().toDomainModel()
fun Domain${NAME}.toLocalModel() = toNetworkModel().toLocalModel()