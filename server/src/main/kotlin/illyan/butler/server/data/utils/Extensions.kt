package illyan.butler.server.data.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

// Utility function to get the date of the last month
fun getLastMonthDate() = Clock.System.now().minus(30.days).startOfDay()

fun getLastWeekDate() = Clock.System.now().minus(7.days).startOfDay()

fun Instant.startOfDay() = toLocalDateTime(TimeZone.UTC).date.atStartOfDayIn(TimeZone.UTC)
