package illyan.butler.data.sqldelight

import app.cash.sqldelight.ColumnAdapter
import illyan.butler.domain.model.ErrorState

val mapAdapter = object : ColumnAdapter<Map<String, String>, String> {
    override fun decode(databaseValue: String): Map<String, String> {
        if (databaseValue.isEmpty()) return emptyMap()
        return databaseValue.split(",").filter { it.isNotBlank() }.associate {
            val (key, value) = it.split("=")
            key to value
        }
    }

    override fun encode(value: Map<String, String>): String {
        return value.map { "${it.key}=${it.value}" }.joinToString(",")
    }
}

fun <T> getListAdapter(
    decode: (String) -> List<T>,
    encode: (List<T>) -> String
) = object : ColumnAdapter<List<T>, String> {
    override fun decode(databaseValue: String): List<T> {
        if (databaseValue.isEmpty()) return emptyList()
        return decode(databaseValue)
    }
    override fun encode(value: List<T>): String {
        return encode(value)
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

val errorStateAdapter = object : ColumnAdapter<ErrorState, String> {
    override fun decode(databaseValue: String): ErrorState {
        return ErrorState.valueOf(databaseValue)
    }

    override fun encode(value: ErrorState): String {
        return value.name
    }
}
