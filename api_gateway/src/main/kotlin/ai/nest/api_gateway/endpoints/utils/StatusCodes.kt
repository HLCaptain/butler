package ai.nest.api_gateway.endpoints.utils

enum class StatusCodes(val code: Int, val message: String) {
    USER_CREATED_SUCCESSFULLY(1000, "User created successfully"),
    USER_ALREADY_EXISTS(1001, "User already exists"),
    USER_NOT_FOUND(1002, "User not found"),
    USER_UPDATED_SUCCESSFULLY(1003, "User updated successfully"),
    USER_DELETED_SUCCESSFULLY(1004, "User deleted successfully"),
    USER_NOT_CREATED(1005, "User not created"),
    USER_NOT_UPDATED(1006, "User not updated"),
    USER_NOT_DELETED(1007, "User not deleted"),
    USER_NOT_AUTHENTICATED(1008, "User not authenticated"),
    USER_NOT_AUTHORIZED(1009, "User not authorized"),
    USER_NOT_ACTIVATED(1010, "User not activated"),
}

val errorMessages by lazy { StatusCodes.entries.associate { it.code to it.message } }