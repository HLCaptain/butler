package illyan.butler.core.utils

actual fun getSystemMetadata(): Map<String, String> {
    return mapOf(
        "os.arch" to System.getProperty("os.arch"),
        "os.version" to System.getProperty("os.version"),
        "java.version" to System.getProperty("java.version"),
        "java.vendor" to System.getProperty("java.vendor"),
        "java.vm.version" to System.getProperty("java.vm.version"),
        "java.vm.vendor" to System.getProperty("java.vm.vendor"),
        "java.vm.name" to System.getProperty("java.vm.name")
    )
}

actual fun getPlatformName(): String {
    return "JVM"
}

actual fun getOsName(): String {
    return System.getProperty("os.name")
}
