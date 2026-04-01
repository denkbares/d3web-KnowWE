package de.knowwe.include;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import com.denkbares.strings.Strings;

final class InterWikiChanges {

	private static final String STATUS = "status";
	private static final String UPDATES = "updates";
	private static final String IMPORTS = "imports";
	private static final String SOURCE_LATEST_CHANGE = "sourceLatestChange";
	private static final String SOURCE_TEXT = "sourceText";
	private static final String REQUESTING_SECTION_ID = "requestingSectionId";
	private static final String PAGE = "page";
	private static final String SECTION = "section";
	private static final String LATEST_CHANGE = "latestChange";

	enum Status {
		ok,
		not_authorized
	}

	record RequestedImport(String requestingSectionId, String page, @Nullable String section,
	                       @Nullable Instant latestChange) {
		JSONObject toJson() {
			JSONObject json = new JSONObject();
			json.put(REQUESTING_SECTION_ID, requestingSectionId);
			json.put(PAGE, page);
			if (section != null) json.put(SECTION, section);
			if (latestChange != null) json.put(LATEST_CHANGE, latestChange.toString());
			return json;
		}

		static RequestedImport fromJson(JSONObject json) {
			return new RequestedImport(
					json.getString(REQUESTING_SECTION_ID),
					json.getString(PAGE),
					json.optString(SECTION, null),
					parseInstant(json.optString(LATEST_CHANGE, null)));
		}
	}

	record Update(String requestingSectionId, Instant sourceLatestChange, String sourceText) {
		JSONObject toJson() {
			JSONObject json = new JSONObject();
			json.put(REQUESTING_SECTION_ID, requestingSectionId);
			json.put(SOURCE_LATEST_CHANGE, sourceLatestChange.toString());
			json.put(SOURCE_TEXT, sourceText);
			return json;
		}

		static Update fromJson(JSONObject json) {
			return new Update(
					json.getString(REQUESTING_SECTION_ID),
					Instant.parse(json.getString(SOURCE_LATEST_CHANGE)),
					json.getString(SOURCE_TEXT));
		}
	}

	private final Status status;
	private final List<Update> updates;

	private InterWikiChanges(Status status, List<Update> updates) {
		this.status = status;
		this.updates = List.copyOf(updates);
	}

	Status getStatus() {
		return status;
	}

	List<Update> getUpdates() {
		return updates;
	}

	JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put(STATUS, status.name());
		JSONArray updatesArray = new JSONArray();
		for (Update update : updates) {
			updatesArray.put(update.toJson());
		}
		json.put(UPDATES, updatesArray);
		return json;
	}

	static InterWikiChanges fromJson(String jsonString) {
		if (Strings.isBlank(jsonString)) return null;
		if (jsonString.startsWith("<!DOCTYPE html>")) {
			return InterWikiChanges.notAuthorized(); // probably not authorized
		}
		JSONObject json = new JSONObject(jsonString);
		Status status = Status.valueOf(json.optString(STATUS, Status.ok.name()));
		List<Update> updates = new ArrayList<>();
		JSONArray updatesArray = json.optJSONArray(UPDATES);
		if (updatesArray != null) {
			for (int i = 0; i < updatesArray.length(); i++) {
				updates.add(Update.fromJson(updatesArray.getJSONObject(i)));
			}
		}
		return new InterWikiChanges(status, updates);
	}

	static String toRequestJson(List<RequestedImport> imports) {
		JSONObject json = new JSONObject();
		JSONArray importsArray = new JSONArray();
		for (RequestedImport requestedImport : imports) {
			importsArray.put(requestedImport.toJson());
		}
		json.put(IMPORTS, importsArray);
		return json.toString();
	}

	static List<RequestedImport> parseRequestJson(String jsonString) {
		if (Strings.isBlank(jsonString)) return List.of();
		JSONObject json = new JSONObject(jsonString);
		JSONArray importsArray = json.optJSONArray(IMPORTS);
		List<RequestedImport> imports = new ArrayList<>();
		if (importsArray == null) return imports;
		for (int i = 0; i < importsArray.length(); i++) {
			imports.add(RequestedImport.fromJson(importsArray.getJSONObject(i)));
		}
		return imports;
	}

	static InterWikiChanges ok(List<Update> updates) {
		return new InterWikiChanges(Status.ok, updates);
	}

	static InterWikiChanges notAuthorized() {
		return new InterWikiChanges(Status.not_authorized, List.of());
	}

	@Nullable
	public static Instant parseInstant(@Nullable String text) {
		if (Strings.isBlank(text)) {
			return null;
		}
		try {
			return Instant.parse(text);
		}
		catch (DateTimeParseException ignored) {
			try {
				return Instant.ofEpochMilli(Long.parseLong(text));
			}
			catch (NumberFormatException e) {
				return null;
			}
		}
	}
}
