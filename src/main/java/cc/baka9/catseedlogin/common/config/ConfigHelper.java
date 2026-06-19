package cc.baka9.catseedlogin.common.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

public class ConfigHelper {

    public final static String parseLocationString(LocationData location) {
        return String.format("%s:%.2f:%.2f:%.2f:%.2f:%.2f",
                location.world,
                location.x,
                location.y,
                location.z,
                location.yaw,
                location.pitch);
    }

    public static LocationData parseLocationString(String locationStr, LocationData defaultLocation) {
        if (locationStr == null || locationStr.isEmpty()) {
            return defaultLocation;
        }
        String[] parts = locationStr.split(":");
        try {
            return LocationData.builder()
                    .world(parts.length > 0 ? parts[0] : defaultLocation.world)
                    .x(parts.length > 1 ? Double.parseDouble(parts[1]) : defaultLocation.x)
                    .y(parts.length > 2 ? Double.parseDouble(parts[2]) : defaultLocation.y)
                    .z(parts.length > 3 ? Double.parseDouble(parts[3]) : defaultLocation.z)
                    .yaw(parts.length > 4 ? Float.parseFloat(parts[4]) : defaultLocation.yaw)
                    .pitch(parts.length > 5 ? Float.parseFloat(parts[5]) : defaultLocation.pitch)
                    .build();
        } catch (NumberFormatException e) {
            return defaultLocation;
        }
    }

    public static class LocationData {
        public String world;
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;

        public LocationData(String world, double x, double y, double z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = 0.0f;
            this.pitch = 0.0f;
        }

        public static LocationDataBuilder builder() {
            return new LocationDataBuilder();
        }

        public static class LocationDataBuilder {
            private final LocationData data = new LocationData("", 0, 0, 0);

            public LocationDataBuilder world(String world) {
                data.world = world;
                return this;
            }

            public LocationDataBuilder x(double x) {
                data.x = x;
                return this;
            }

            public LocationDataBuilder y(double y) {
                data.y = y;
                return this;
            }

            public LocationDataBuilder z(double z) {
                data.z = z;
                return this;
            }

            public LocationDataBuilder yaw(float yaw) {
                data.yaw = yaw;
                return this;
            }

            public LocationDataBuilder pitch(float pitch) {
                data.pitch = pitch;
                return this;
            }

            public LocationData build() {
                return data;
            }
        }
    }

    public static String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        return input.trim();
    }

    public static int parseIntOrDefault(String value, int defaultValue) {
        return parseValue(value, Integer::parseInt, defaultValue);
    }

    public static long parseLongOrDefault(String value, long defaultValue) {
        return parseValue(value, Long::parseLong, defaultValue);
    }

    public static boolean parseBooleanOrDefault(String value, boolean defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    private static <T> T parseValue(String value, Function<String, T> parser, T defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return parser.apply(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}