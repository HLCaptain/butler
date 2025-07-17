package illyan.butler.core.utils

import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice

actual fun getSystemMetadata(): Map<String, String> {
    val device = UIDevice.currentDevice
    val processInfo = NSProcessInfo.processInfo
    return mapOf(
        "osName" to device.systemName,
        "osVersion" to device.systemVersion,
        "deviceModel" to device.model,
        "deviceName" to device.name,
        "osVersionString" to processInfo.operatingSystemVersionString,
        "processorCount" to processInfo.processorCount.toString(),
        "physicalMemory" to processInfo.physicalMemory.toString()
    )
}

actual fun getPlatformName(): String {
    return "iOS"
}

actual fun getOsName(): String {
    return UIDevice.currentDevice.systemVersion
}
