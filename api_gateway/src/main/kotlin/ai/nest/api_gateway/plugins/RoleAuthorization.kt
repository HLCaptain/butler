package ai.nest.api_gateway.plugins

import ai.nest.api_gateway.utils.Claim
import ai.nest.api_gateway.utils.Role
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

class RoleBaseConfiguration {
    val requiredRoles = mutableSetOf<Role>()
    fun roles(roles: Set<Role>) {
        requiredRoles.addAll(roles)
    }
}

val RoleAuthorizationPlugin = createRouteScopedPlugin("RoleAuthorizationPlugin", ::RoleBaseConfiguration) {
    on(AuthenticationChecked) { call ->
        val principal = call.principal<JWTPrincipal>() ?: return@on
        val roles = principal.getListClaim(Claim.PERMISSION, Role::class)

        if (pluginConfig.requiredRoles.isNotEmpty() && roles.intersect(pluginConfig.requiredRoles).isEmpty()) {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}