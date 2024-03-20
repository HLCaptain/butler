package illyan.butler.services.identity.endpoints

import illyan.butler.services.identity.data.model.identity.UserDto
import illyan.butler.services.identity.data.model.identity.UserRegistrationDto
import illyan.butler.services.identity.data.service.IdentityService
import illyan.butler.services.identity.endpoints.utils.WebSocketServerHandler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import org.koin.ktor.ext.inject

fun Route.identityRoute() {
    val identityService: IdentityService by inject()
    val webSocketServerHandler: WebSocketServerHandler by inject()

    route("/users") {
        post {
            val user = call.receive<UserRegistrationDto>()
            call.respond(identityService.registerUser(user.userName, user.email, user.password))
        }

        post("/login") {
            val email = call.parameters["email"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val password = call.parameters["password"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            call.respond(identityService.getUserIdByEmailAndPassword(email, password))
        }

        route("/{userId}") {
            get {
                val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(identityService.getUser(userId))
            }

            put {
                val user = call.receive<UserDto>()
                call.respond(identityService.editUser(user))
            }

            delete {
                val userId = call.parameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                call.respond(identityService.deleteUser(userId))
            }

            webSocket {
                val userId = call.parameters["userId"] ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
                val userFlow = identityService.getUserChanges(userId)
                webSocketServerHandler.sessions[userId] = this
                webSocketServerHandler.sessions[userId]?.let {
                    webSocketServerHandler.tryToCollect(userFlow, it)
                }
            }
        }
    }
}