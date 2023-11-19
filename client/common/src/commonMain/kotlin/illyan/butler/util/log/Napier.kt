package illyan.butler.util.log

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

// TODO: log based on user analytics preference
fun initNapier(isDebug: Boolean = true) {
    Napier.takeLogarithm()
    if (isDebug) {
        Napier.base(DebugAntilog())
    }
}