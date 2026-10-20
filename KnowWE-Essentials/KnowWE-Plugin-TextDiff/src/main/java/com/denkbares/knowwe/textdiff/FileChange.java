package com.denkbares.knowwe.textdiff;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.utils.Pair;

import de.knowwe.core.utils.KnowWEUtils;

/**
 * Render model for one {@code <knowwe-file-change>} wrapper. The factories build the model of one article
 * operation, the constructor is for callers that bring their own names and urls.
 */
public record FileChange(
		@NotNull ChangeType changeType,
		@Nullable String oldName,
		@Nullable String newName,
		@Nullable String url,
		@Nullable String oldUrl,
		@Nullable String newUrl,
		@Nullable TextDiff diff
) {

	@NotNull
	public static FileChange added(@NotNull String name, @NotNull String newContent) {
		return new FileChange(ChangeType.ADDED, null, name, articleUrl(name), null, null, new TextDiff(null, newContent));
	}

	@NotNull
	public static FileChange deleted(@NotNull String name, @NotNull String oldContent) {
		return new FileChange(ChangeType.DELETED, name, null, articleUrl(name), null, null, new TextDiff(oldContent, null));
	}

	@NotNull
	public static FileChange modified(@NotNull String name, @NotNull String oldContent, @NotNull String newContent) {
		return new FileChange(ChangeType.MODIFIED, name, name, articleUrl(name), null, null, new TextDiff(oldContent, newContent));
	}

	@NotNull
	public static FileChange renamed(@NotNull String oldName, @NotNull String newName) {
		return new FileChange(ChangeType.RENAMED, oldName, newName, null, articleUrl(oldName), articleUrl(newName), null);
	}

	@NotNull
	public static FileChange renamedModified(
			@NotNull String oldName,
			@NotNull String newName,
			@NotNull String oldContent,
			@NotNull String newContent
	) {
		return new FileChange(ChangeType.RENAMED_MODIFIED, oldName, newName, null, articleUrl(oldName), articleUrl(newName),
				new TextDiff(oldContent, newContent));
	}

	@NotNull
	private static String articleUrl(@NotNull String articleName) {
		return KnowWEUtils.getURLLink(articleName, emptyUrlParameters());
	}

	@SuppressWarnings("unchecked")
	private static Pair<String, String>[] emptyUrlParameters() {
		return new Pair[0];
	}

	public enum ChangeType {
		ADDED("added"),
		DELETED("deleted"),
		MODIFIED("modified"),
		RENAMED("renamed"),
		RENAMED_MODIFIED("renamed-modified");

		private final String htmlValue;

		ChangeType(String htmlValue) {
			this.htmlValue = htmlValue;
		}

		String htmlValue() {
			return htmlValue;
		}
	}
}
