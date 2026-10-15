package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PendingCommandsResponseTest {

    private static PendingCommandsResponse parse(String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        // No customer is present, so parsing does not need an API instance.
        return new PendingCommandsResponse(null, object);
    }

    @Test
    void acceptsLegacyResponseWithoutPageMetadata() {
        PendingCommandsResponse response = parse("{\"online_mode\":true,\"customers\":[]}");
        Assertions.assertTrue(response.shouldUseUuids());
        Assertions.assertTrue(response.customers().isEmpty());
        Assertions.assertNull(response.page());
    }

    @Test
    void parsesCursorAndHighWaterMetadata() {
        PendingCommandsResponse response = parse("{\"online_mode\":false,\"customers\":[],"
                + "\"page\":{\"high_water_id\":60,\"next_cursor\":{\"order\":7,\"id\":50},\"has_more\":true}}");
        PendingCommandsResponse.Page page = response.page();
        Assertions.assertNotNull(page);
        Assertions.assertEquals(60, page.highWaterId());
        Assertions.assertTrue(page.hasMore());
        PendingCommandsResponse.Cursor cursor = page.nextCursor();
        Assertions.assertNotNull(cursor);
        Assertions.assertEquals(7, cursor.order());
        Assertions.assertEquals(50, cursor.id());
    }

    @Test
    void rejectsImpossiblePageMetadata() {
        Assertions.assertThrows(IllegalStateException.class, () -> parse("{\"online_mode\":false,\"customers\":[],"
                + "\"page\":{\"high_water_id\":60,\"next_cursor\":null,\"has_more\":true}}"));
        Assertions.assertThrows(IllegalStateException.class, () -> parse("{\"online_mode\":false,\"customers\":[],"
                + "\"page\":{\"high_water_id\":60,\"next_cursor\":{\"order\":7,\"id\":61},\"has_more\":true}}"));
    }

    @Test
    void rejectsCoercionInActualPageParsing() {
        for (String invalid : new String[]{"\"60\"", "60.5", "6e1", "true", "null", "2147483648"}) {
            Assertions.assertThrows(IllegalStateException.class, () -> parse("{\"online_mode\":true,\"customers\":[],"
                    + "\"page\":{\"high_water_id\":" + invalid + ",\"next_cursor\":null,\"has_more\":false}}"), invalid);
        }
    }

    @Test
    void validatesCommandIdsBeforeTheWorkerCanDispatchThem() {
        for (String invalid : new String[]{"\"1\"", "1.5", "1e0", "true", "null", "0", "-1", "2147483648"}) {
            Assertions.assertThrows(IllegalStateException.class, () -> parse("{\"online_mode\":true,\"customers\":["
                    + "{\"customer_id\":1,\"username\":\"Reader\",\"commands\":[{\"id\":" + invalid
                    + ",\"command\":\"test\",\"order_id\":1,\"require_online\":true}]}]}"), invalid);
        }
    }

    @Test
    void rejectsStringBooleanProtocolValues() {
        Assertions.assertThrows(IllegalStateException.class, () -> parse("{\"online_mode\":\"true\",\"customers\":[]}"));
        Assertions.assertThrows(IllegalStateException.class, () -> parse("{\"online_mode\":true,\"customers\":[],"
                + "\"page\":{\"high_water_id\":0,\"next_cursor\":null,\"has_more\":\"false\"}}"));
    }
}
