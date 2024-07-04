package illyan.butler.data.room

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toList(databaseValue: String): List<String> {
        if (databaseValue.isEmpty()) return emptyList()
        return databaseValue.split(",")
    }

    @TypeConverter
    fun toString(map: Map<String, String>): String {
        return map.map { "${it.key}=${it.value}" }.joinToString(",")
    }

    @TypeConverter
    fun toMap(databaseValue: String): Map<String, String> {
        if (databaseValue.isEmpty()) return emptyMap()
        return databaseValue.split(",").filter { it.isNotBlank() }.associate {
            val (key, value) = it.split("=")
            key to value
        }
    }
}