package illyan.butler.services.chat.data.schema

import illyan.butler.services.chat.data.utils.NanoIdTable

object ContentUrls : NanoIdTable() {
    val url = text("url").uniqueIndex()
}