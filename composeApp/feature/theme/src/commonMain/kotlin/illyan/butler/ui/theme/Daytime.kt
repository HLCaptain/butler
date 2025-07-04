package illyan.butler.ui.theme

import androidx.compose.ui.util.lerp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sin
import kotlin.math.tan
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun calculateSunriseSunsetTimes(
    dayOfYear: Int,
    daysInYear: Int,
    timeZone: TimeZone,
    latitude: Double = getLatitudeFromOffset(timeZone)
): Pair<Int, Int> {
    val offsetInSeconds = timeZone.offsetAt(Clock.System.now()).totalSeconds

    // Basic approximation of day length variation over a year using a sine wave
    val declinationAngle = 23.44 * sin((360.0 / daysInYear) * (dayOfYear - 81) * PI / 180.0)
    val solarNoon = 12  // Assuming solar noon occurs at 12 PM UTC

    // Calculate day length
    val hourAngle = acos(-tan(latitude * PI / 180.0) * tan(declinationAngle * PI / 180.0)) * 180.0 / PI
    val daylightHours = 2 * hourAngle / 15.0

    // Calculate sunrise and sunset in hours from midnight
    val sunriseHour = solarNoon - (daylightHours / 2.0) + offsetInSeconds / 3600.0
    val sunsetHour = solarNoon + (daylightHours / 2.0) + offsetInSeconds / 3600.0

    // Convert hours to seconds since midnight
    val sunriseSeconds = (sunriseHour * 3600).toInt()
    val sunsetSeconds = (sunsetHour * 3600).toInt()

    return sunriseSeconds to sunsetSeconds
}

@OptIn(ExperimentalTime::class)
fun getLatitudeFromOffset(timeZone: TimeZone): Double {
    val offsetInSeconds = timeZone.offsetAt(Clock.System.now()).totalSeconds
    return lerp(-90.0f, 90.0f, offsetInSeconds / 43200.0f).toDouble()
}
