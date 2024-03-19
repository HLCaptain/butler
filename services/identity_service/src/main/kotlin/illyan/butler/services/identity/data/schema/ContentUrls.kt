package illyan.butler.services.identity.data.schema

import illyan.butler.services.identity.data.utils.NanoIdTable

object ContentUrls : NanoIdTable() {
    val url = text("url").uniqueIndex()
}