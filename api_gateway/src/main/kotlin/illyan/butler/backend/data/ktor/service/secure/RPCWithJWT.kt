package illyan.butler.backend.data.ktor.service.secure

import com.auth0.jwt.interfaces.Payload
import kotlinx.rpc.RPC

interface RPCWithJWT : RPC {
    val jwtPayload: Payload
}