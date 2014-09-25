package de.knowwe.util;

import de.knowwe.core.Environment;

/**
 * Represents a generic icon used throughout the KnowWE User Interface.
 *
 * @author Alex Legler
 */
public enum Icon {
	// Message Types
	INFORMATION("templates/KnowWE/images/information.png"),
	WARNING("templates/KnowWE/images/warning.png"),
	ERROR("templates/KnowWE/images/error.png"),

	// Actions
	NEW("templates/KnowWE/images/new.png"),
	EDIT("templates/KnowWE/images/edit.png"),
	DELETE("templates/KnowWE/images/delete.png"),
	RENAME("templates/KnowWE/images/rename.png"),
	REFRESH("templates/KnowWE/images/refresh.png"),
	SAVE("templates/KnowWE/images/save.png"),
	DOWNLOAD("templates/KnowWE/images/download.png"),

	// Entities
	ARTICLE("templates/KnowWE/images/article.png"),
	PACKAGE("KnowWEExtension/images/package.png"),

	// Files
	DOCUMENT_WORD("templates/KnowWE/images/document-word.png"),
	DOCUMENT_EXCEL("templates/KnowWE/images/document-excel.png"),
	DOCUMENT_PDF("templates/KnowWE/images/document-pdf.png");

	private final String path;

	private Icon(String path) {
		this.path = path;
	}

	public String toString() {
		return path;
	}

	public String getPath() {
		return path;
	}

	public String getURL() {
		final String baseURL = Environment.getInstance().getWikiConnector().getBaseUrl();

		return baseURL + (baseURL.endsWith("/") ? "" : "/") + getPath();
	}

	public String getImageTag() {
		return getImageTag(null);
	}

	public String getImageTag(String title) {
		return "<img src=\"" + getPath() + "\"" + (title != null ? " title=\"" + title + "\"" : "") + " />";
	}
}