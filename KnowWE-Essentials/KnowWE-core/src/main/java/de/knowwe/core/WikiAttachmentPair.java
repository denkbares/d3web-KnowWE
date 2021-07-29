/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 *
 */

package de.knowwe.core;

import java.io.IOException;

import com.denkbares.utils.Log;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * Class that simply holds two WikiAttachments
 *
 * @author Tobias Schmee (denkbares GmbH)
 * @created 27.07.21
 */
public class WikiAttachmentPair {

	private final WikiAttachment firstAttachment;
	private final WikiAttachment secondAttachment;

	public WikiAttachmentPair(String firstAttachmentPath, String secondAttachmentPath) {
		this.firstAttachment = createAttachment(firstAttachmentPath);
		this.secondAttachment = createAttachment(secondAttachmentPath);
	}

	/**
	 * Creates a WikiAttachment from the path where it is placed
	 *
	 * @param attachmentPath path to the attachment
	 * @return attachment
	 */
	private WikiAttachment createAttachment(String attachmentPath) {
		// title is usually the last path of the String
		String[] pathComponents = attachmentPath.split("/");
		String title = pathComponents.length > 0 ? pathComponents[pathComponents.length - 1] : attachmentPath;
		try {
			return KnowWEUtils.getAttachment(title, attachmentPath);
		}
		catch (IOException e) {
			Log.severe("Problems reading an attachment file in " + attachmentPath + ". " + e);
		}
		return null;
	}

	public WikiAttachment getFirstAttachment() {
		return firstAttachment;
	}

	public WikiAttachment getSecondAttachment() {
		return secondAttachment;
	}
}

