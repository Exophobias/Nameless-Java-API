package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Strict protocol scalars: Gson's numeric accessors otherwise truncate decimals and coerce strings. */
final class StoreWire {
    private StoreWire() {}

    static int integer(JsonObject json, String key, int minimum) {
        JsonElement field = json.get(key);
        if (field == null || !field.isJsonPrimitive() || !field.getAsJsonPrimitive().isNumber()
                || !field.toString().matches("-?(0|[1-9][0-9]*)")) {
            throw new IllegalStateException("Invalid Store integer: " + key);
        }
        try {
            long value = Long.parseLong(field.toString());
            if (value < minimum || value > Integer.MAX_VALUE) throw new NumberFormatException();
            return (int) value;
        } catch (NumberFormatException invalid) {
            throw new IllegalStateException("Invalid Store integer: " + key);
        }
    }

    static boolean bool(JsonObject json, String key) {
        JsonElement field = json.get(key);
        if (field == null || !field.isJsonPrimitive() || !field.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalStateException("Invalid Store boolean: " + key);
        }
        return field.getAsBoolean();
    }
}
