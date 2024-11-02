package illyan.butler.utils

import illyan.butler.isDebugBuild
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

// TODO: log based on user analytics preference
fun initNapier() {
    Napier.takeLogarithm()
    if (isDebugBuild()) {
        Napier.base(DebugAntilog())
    }
}