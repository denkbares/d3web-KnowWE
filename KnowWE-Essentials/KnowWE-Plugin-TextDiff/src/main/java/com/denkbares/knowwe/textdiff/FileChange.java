package com.denkbares.knowwe.textdiff;

import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.utils.Pair;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Render model for one {@code <knowwe-file-change>} wrapper.
 *
 * <p>The direct constructor is intentionally simple for callers that already know the operation.
 * The {@code fromArticle(...)} factories inspect the current wiki article state and return
 * {@code null} when there is no visible change to render.
 */
public record FileChange(
		@NotNull ChangeType changeType,
		@Nullable String oldName,
		@Nullable String newName,
		@Nullable String url,
		@Nullable String oldUrl,
		@Nullable String newUrl,
		@Nullable TextDiff diff,
		boolean collapsed
) {

	/**
	 * Creates a change model for updating, creating, or deleting one article without renaming it.
	 *
	 * @param articleName current/final article name
	 * @param newContent  final content; {@code null} means delete
	 * @return the inferred change, or {@code null} if the current article already has the requested state
	 */
	@Nullable
	public static FileChange fromArticle(@NotNull String articleName, @Nullable String newContent) {
		return fromArticle(articleName, articleName, newContent);
	}

	/**
	 * Creates a change model from an intended article state.
	 *
	 * @param oldName    original article name; may be {@code null} for create/modify-by-target-name
	 * @param newName    final article name; when {@code null}, {@code oldName} is used unless
	 *                   {@code newContent} is {@code null}, which means delete
	 * @param newContent final content; {@code null} means delete
	 * @return the inferred change, or {@code null} if there is no visible change to render or no article name is set
	 */
	@Nullable
	@Contract("null, null, _ -> null")
	public static FileChange fromArticle(
			@Nullable String oldName,
			@Nullable String newName,
			@Nullable String newContent
	) {
		if (oldName == null && newName == null) return null;
		String effectiveName = newName == null ? oldName : newName;
		String existingName = oldName == null ? newName : oldName;

		ArticleManager articleManager = Environment.getInstance().getArticleManager();
		Article oldArticle = articleManager.getArticle(existingName);
		String oldContent = oldArticle == null ? null : oldArticle.getText();

		if (newContent == null) {
			if (oldArticle == null) return null;
			return deleted(existingName, oldContent);
		}

		if (oldArticle == null) {
			return added(effectiveName, newContent);
		}

		boolean renamed = !Objects.equals(existingName, effectiveName);
		boolean contentChanged = !Objects.equals(oldContent, newContent);
		if (!renamed && !contentChanged) return null;

		if (renamed && !contentChanged) {
			return renamed(existingName, effectiveName);
		}
		if (renamed) {
			return renamedModified(existingName, effectiveName, oldContent, newContent);
		}
		return modified(effectiveName, oldContent, newContent);
	}

	@NotNull
	public static FileChange added(@NotNull String name, @NotNull String newContent) {
		return new FileChange(
					ChangeType.ADDED,
					null,
					name,
					articleUrl(name),
					null,
					null,
					new TextDiff(null, newContent),
					false
		);
	}

	@NotNull
	public static FileChange deleted(@NotNull String name, @NotNull String oldContent) {
		return new FileChange(
					ChangeType.DELETED,
					name,
					null,
					articleUrl(name),
					null,
					null,
					new TextDiff(oldContent, null),
					false
		);
	}

	@NotNull
	public static FileChange modified(
			@NotNull String name,
			@NotNull String oldContent,
			@NotNull String newContent
	) {
		return new FileChange(
					ChangeType.MODIFIED,
					name,
					name,
					articleUrl(name),
					null,
					null,
					new TextDiff(oldContent, newContent),
					false
		);
	}

	@NotNull
	public static FileChange renamed(@NotNull String oldName, @NotNull String newName) {
		return new FileChange(
					ChangeType.RENAMED,
					oldName,
					newName,
					null,
					articleUrl(oldName),
					articleUrl(newName),
					null,
					false
			);
	}

	@NotNull
	public static FileChange renamedModified(
			@NotNull String oldName,
			@NotNull String newName,
			@NotNull String oldContent,
			@NotNull String newContent
	) {
		return new FileChange(
					ChangeType.RENAMED_MODIFIED,
					oldName,
					newName,
					null,
					articleUrl(oldName),
					articleUrl(newName),
					new TextDiff(oldContent, newContent),
					false
			);
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
