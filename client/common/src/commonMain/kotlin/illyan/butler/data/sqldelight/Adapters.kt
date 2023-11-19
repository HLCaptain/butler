package illyan.butler.data.sqldelight

import app.cash.sqldelight.ColumnAdapter

val mapAdapter = object : ColumnAdapter<Map<String, String>, String> {
    override fun decode(databaseValue: String): Map<String, String> {
        if (databaseValue.isEmpty()) return emptyMap()
        return databaseValue.split(",").associate {
            val (key, value) = it.split("=")
            key to value
        }
    }

    override fun encode(value: Map<String, String>): String {
        return value.map { "${it.key},${it.value}" }.joinToString(",")
    }
}

val listAdapter = object : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String): List<String> {
        if (databaseValue.isEmpty()) return emptyList()
        return databaseValue.split(",")
    }

    override fun encode(value: List<String>): String {
        return value.joinToString(",")
    }
}
