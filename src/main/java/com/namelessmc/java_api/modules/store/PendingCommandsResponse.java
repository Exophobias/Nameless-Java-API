package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PendingCommandsResponse {

	private final boolean useUuids;
	private final List<PendingCommandsCustomer> customers;
	private final @Nullable Page page;

	PendingCommandsResponse(NamelessAPI api, JsonObject json) {
		this.useUuids = json.get("online_mode").getAsBoolean();
		JsonArray customers = json.getAsJsonArray("customers");
		this.customers = new ArrayList<>(customers.size());
		for (JsonElement element : customers) {
			this.customers.add(new PendingCommandsCustomer(api, element.getAsJsonObject()));
		}
		this.page = json.has("page") ? new Page(json.getAsJsonObject("page")) : null;
	}

	public boolean shouldUseUuids() {
		return this.useUuids;
	}

	public List<PendingCommandsCustomer> customers() {
		return this.customers;
	}

	/** Null for the original, unpaged Store response. */
	public @Nullable Page page() {
		return this.page;
	}

	public static final class Cursor {
		private final int order;
		private final int id;

		private Cursor(JsonObject json) {
			this.order = exactInt(json, "order", Integer.MIN_VALUE);
			this.id = exactInt(json, "id", 1);
		}

		public int order() {
			return this.order;
		}

		public int id() {
			return this.id;
		}
	}

	public static final class Page {
		private final int highWaterId;
		private final @Nullable Cursor nextCursor;
		private final boolean hasMore;

		private Page(JsonObject json) {
			this.highWaterId = exactInt(json, "high_water_id", 0);
			this.hasMore = json.get("has_more").getAsBoolean();
			this.nextCursor = json.has("next_cursor") && !json.get("next_cursor").isJsonNull()
					? new Cursor(json.getAsJsonObject("next_cursor")) : null;
			if (this.hasMore && this.nextCursor == null) {
				throw new IllegalStateException("Store page has more rows without a cursor");
			}
			if (this.nextCursor != null && this.nextCursor.id() > this.highWaterId) {
				throw new IllegalStateException("Store cursor exceeds its high-water ID");
			}
		}

		public int highWaterId() {
			return this.highWaterId;
		}

		public @Nullable Cursor nextCursor() {
			return this.nextCursor;
		}

		public boolean hasMore() {
			return this.hasMore;
		}
	}

	private static int exactInt(JsonObject json, String key, int minimum) {
		long value = json.get(key).getAsLong();
		if (value < minimum || value > Integer.MAX_VALUE) {
			throw new IllegalStateException("Invalid Store page " + key);
		}
		return (int) value;
	}

	public static class PendingCommandsCustomer extends StoreCustomer {

		private final List<PendingCommand> pendingCommands;

		private PendingCommandsCustomer(NamelessAPI api, JsonObject json) {
			super(api, json);

			JsonArray commands = json.getAsJsonArray("commands");
			this.pendingCommands = new ArrayList<>(commands.size());
			for (JsonElement element : commands) {
				this.pendingCommands.add(new PendingCommand(element.getAsJsonObject()));
			}
		}

		@Override
		public @NonNull String username() {
			String username = super.username();
			if (username == null) {
				throw new IllegalStateException("Pending commands response cannot contain null username");
			}
			return username;
		}

		public List<PendingCommand> pendingCommands() {
			return this.pendingCommands;
		}

	}

	public static class PendingCommand {

		private final int id;
		private final String command;
		private final int orderId;
		private final boolean requireOnline;

		private PendingCommand(JsonObject json) {
			this.id = json.get("id").getAsInt();
			this.command = json.get("command").getAsString();
			this.orderId = json.get("order_id").getAsInt();
			this.requireOnline = json.get("require_online").getAsBoolean();
		}

		public int id() {
			return id;
		}

		public String command() {
			return command;
		}

		public int orderId() {
			return orderId;
		}

		public boolean isOnlineRequired() {
			return requireOnline;
		}

	}

}
