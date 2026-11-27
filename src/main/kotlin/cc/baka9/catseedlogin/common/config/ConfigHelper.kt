package cc.baka9.catseedlogin.common.config

class ConfigHelper private constructor() {
    companion object {
        @JvmStatic fun parseLocationString(location: LocationData): String =
            String.format("%s:%.2f:%.2f:%.2f:%.2f:%.2f", location.world, location.x, location.y, location.z, location.yaw, location.pitch)

        @JvmStatic fun parseLocationString(locationStr: String?, defaultLocation: LocationData): LocationData {
            if (locationStr.isNullOrEmpty()) return defaultLocation
            val parts = locationStr.split(":").dropLastWhile { it.isEmpty() }
            return try {
                LocationData.builder()
                    .world(parts.getOrElse(0) { defaultLocation.world })
                    .x(parts.getOrElse(1) { defaultLocation.x.toString() }.toDouble())
                    .y(parts.getOrElse(2) { defaultLocation.y.toString() }.toDouble())
                    .z(parts.getOrElse(3) { defaultLocation.z.toString() }.toDouble())
                    .yaw(parts.getOrElse(4) { defaultLocation.yaw.toString() }.toFloat())
                    .pitch(parts.getOrElse(5) { defaultLocation.pitch.toString() }.toFloat())
                    .build()
            } catch (_: NumberFormatException) { defaultLocation }
        }

        @JvmStatic fun sanitizeString(input: String?): String? = input?.trim()
        @JvmStatic fun parseIntOrDefault(value: String?, defaultValue: Int): Int = parseValue(value, defaultValue) { it.toInt() }
        @JvmStatic fun parseLongOrDefault(value: String?, defaultValue: Long): Long = parseValue(value, defaultValue) { it.toLong() }
        @JvmStatic fun parseBooleanOrDefault(value: String?, defaultValue: Boolean): Boolean = if (value.isNullOrEmpty()) defaultValue else value.toBoolean()

        private fun <T> parseValue(value: String?, defaultValue: T, parser: (String) -> T): T =
            if (value.isNullOrEmpty()) defaultValue else try { parser(value) } catch (_: Exception) { defaultValue }
    }

    class LocationData(@JvmField var world: String, @JvmField var x: Double, @JvmField var y: Double, @JvmField var z: Double) {
        @JvmField var yaw: Float = 0f
        @JvmField var pitch: Float = 0f

        companion object { @JvmStatic fun builder(): LocationDataBuilder = LocationDataBuilder() }

        class LocationDataBuilder {
            private val data = LocationData("", 0.0, 0.0, 0.0)
            fun world(world: String) = apply { data.world = world }
            fun x(x: Double) = apply { data.x = x }
            fun y(y: Double) = apply { data.y = y }
            fun z(z: Double) = apply { data.z = z }
            fun yaw(yaw: Float) = apply { data.yaw = yaw }
            fun pitch(pitch: Float) = apply { data.pitch = pitch }
            fun build(): LocationData = data
        }
    }
}
