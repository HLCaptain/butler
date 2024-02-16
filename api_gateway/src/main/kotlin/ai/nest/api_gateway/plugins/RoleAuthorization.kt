package ai.nest.api_gateway.plugins

import ai.nest.api_gateway.utils.Claim
import ai.nest.api_gateway.utils.Permission
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

class RoleBaseConfiguration {
    val requiredRoles = mutableSetOf<Permission>()
    fun roles(roles: Set<Permission>) {
        requiredRoles.addAll(roles)
    }
}

val RoleAuthorizationPlugin = createRouteScopedPlugin("RoleAuthorizationPlugin", ::RoleBaseConfiguration) {
    on(AuthenticationChecked) { call ->
        val principal = call.principal<JWTPrincipal>()
        val roles = principal?.getListClaim(Claim.PERMISSIONS, Permission::class)

        val doesHavePermission = roles?.any { it in pluginConfig.requiredRoles } ?: false
        if (!doesHavePermission) call.respond(HttpStatusCode.Forbidden)
    }
}