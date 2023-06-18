package illyan.butler.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform