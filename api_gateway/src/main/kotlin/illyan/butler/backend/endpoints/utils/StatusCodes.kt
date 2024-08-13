package illyan.butler.backend.endpoints.utils

enum class StatusCodes(val code: Int, val message: String) {
    USER_CREATED_SUCCESSFULLY(2000, "User created successfully"),
    USER_UPDATED_SUCCESSFULLY(2001, "User updated successfully"),
    USER_DELETED_SUCCESSFULLY(2002, "User deleted successfully"),
    USER_ALREADY_EXISTS(4000, "User already exists"),
    USER_NOT_FOUND(4001, "User not found"),
    USER_NOT_CREATED(4002, "User not created"),
    USER_NOT_UPDATED(4003, "User not updated"),
    USER_NOT_DELETED(4004, "User not deleted"),
    USER_NOT_AUTHENTICATED(4005, "User not authenticated"),
    USER_NOT_AUTHORIZED(4006, "User not authorized"),
    USER_NOT_ACTIVATED(4007, "User not activated"),
}

val errorMessages by lazy { StatusCodes.entries.associate { it.code to it.message } }