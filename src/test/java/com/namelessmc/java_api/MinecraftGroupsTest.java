package com.namelessmc.java_api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MinecraftGroupsTest {
    @Test void sendsServerBoundDeltaToUuidScopedRoute() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> path = new AtomicReference<>();
        AtomicReference<JsonObject> body = new AtomicReference<>();
        server.createContext("/", exchange -> {
            path.set(exchange.getRequestURI().getPath());
            body.set(JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8)).getAsJsonObject());
            byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try {
            NamelessAPI api = NamelessAPI.builder(URI.create("http://127.0.0.1:"
                    + server.getAddress().getPort() + "/").toURL(), "test").build();
            UUID uuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
            NamelessUser user = api.userByMinecraftUuidLazy(uuid);
            user.updateMinecraftGroups(7, new String[]{"member", "builder"}, new String[]{"guest"});
            assertEquals("/minecraft/integration_id:Minecraft:11111111222233334444555555555555/sync-groups", path.get());
            assertEquals(7, body.get().get("server_id").getAsInt());
            assertEquals(JsonParser.parseString("[\"member\",\"builder\"]"), body.get().get("add"));
            assertEquals(JsonParser.parseString("[\"guest\"]"), body.get().get("remove"));
            path.set(null);
            assertThrows(IllegalArgumentException.class,
                    () -> user.updateMinecraftGroups(0, new String[0], new String[0]));
            assertNull(path.get(), "Invalid server ids must not send a request");
        } finally {
            server.stop(0);
        }
    }
}
