/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 *
 */

package de.knowwe.core;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * Class that simply holds two WikiAttachments
 *
 * @author Tobias Schmee (denkbares GmbH)
 * @created 27.07.21
 */
public class WikiAttachmentPair {

	private final String testObjectName;

	public WikiAttachmentPair(String testObjectName) {
		this.testObjectName = testObjectName;
	}

	@NotNull
	public WikiAttachment getFirstAttachment() throws IOException {
		return getWikiAttachment(getAttachmentPaths()[0]);
	}

	@NotNull
	public WikiAttachment getSecondAttachment() throws IOException {
		return getWikiAttachment(getAttachmentPaths()[1]);
	}

	@NotNull
	private WikiAttachment getWikiAttachment(String attachmentPath) throws IOException {
		WikiAttachment attachment = Environment.getInstance().getWikiConnector().getAttachment(attachmentPath);
		if (attachment == null) throw new IOException(attachmentPath + " is not a valid attachment path");
		return attachment;
	}

	@NotNull
	private String[] getAttachmentPaths() throws IOException {
		String[] paths = testObjectName.split("\\s*[,;]\\s*");
		if (paths.length != 2) {
			throw new IOException("Expected two attachment paths separated by a comma, but got: " + testObjectName);
		}
		return paths;
	}

}

