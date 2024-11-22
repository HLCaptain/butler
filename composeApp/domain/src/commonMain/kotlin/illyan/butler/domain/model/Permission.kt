package illyan.butler.domain.model

enum class Permission {
    CAMERA, // Accessing camera
    GALLERY, // Accessing gallery or image library
    EXTERNAL_STORAGE, // Accessing external or domain storage
    WRITE_EXTERNAL_STORAGE, // Writing to external or domain storage
    MEDIA_STORAGE, // Accessing media storage with images/audio/video
    FINE_LOCATION, // Accessing fine location
    COARSE_LOCATION, // Accessing coarse location (not accurate)
    BACKGROUND_LOCATION, // Accessing location in the background
    REMOTE_NOTIFICATION, // Receiving remote notifications
    RECORD_AUDIO, // Recording audio with microphone
    BLUETOOTH_LE, // Accessing Bluetooth Low Energy
    BLUETOOTH_SCAN, // Scanning for Bluetooth devices
    BLUETOOTH_CONNECT, // Connecting to Bluetooth devices
    BLUETOOTH_ADVERTISE, // Advertising as a Bluetooth device
    MOTION_SENSORS, // Accessing motion sensors (accelerometer, gyroscope, etc.)
}