package ai.nest.api_gateway.data.model.identity

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val latitude: Double,
    val longitude: Double
)