package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreWireTest {
    private JsonObject value(String literal) {
        return JsonParser.parseString("{\"value\":" + literal + "}").getAsJsonObject();
    }

    @Test void rejectsCoercedTruncatedAndOverflowingIntegers() {
        for (String invalid : new String[]{"\"1\"", "1.5", "1.0", "1e0", "true", "null", "[]", "{}", "2147483648", "999999999999999999999999999999", "-1"}) {
            assertThrows(IllegalStateException.class, () -> StoreWire.integer(value(invalid), "value", 0), invalid);
        }
        assertThrows(IllegalStateException.class, () -> StoreWire.integer(new JsonObject(), "value", 0));
    }

    @Test void acceptsOnlyInRangeLiteralIntegers() {
        assertEquals(0, StoreWire.integer(value("0"), "value", 0));
        assertEquals(Integer.MAX_VALUE, StoreWire.integer(value("2147483647"), "value", 0));
        assertEquals(Integer.MIN_VALUE, StoreWire.integer(value("-2147483648"), "value", Integer.MIN_VALUE));
        assertThrows(IllegalStateException.class, () -> StoreWire.integer(value("0"), "value", 1));
    }

    @Test void requiresRealBooleanScalars() {
        assertTrue(StoreWire.bool(value("true"), "value"));
        assertFalse(StoreWire.bool(value("false"), "value"));
        for (String invalid : new String[]{"\"true\"", "1", "null", "{}"}) {
            assertThrows(IllegalStateException.class, () -> StoreWire.bool(value(invalid), "value"), invalid);
        }
    }
}
