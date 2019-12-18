package org.apache.wiki.providers;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 12.12.19
 */
public class AttachmentCacheItem extends GitCacheItem {
	private final String parentName;
	private final String attachmentName;

	AttachmentCacheItem(String parentName, String attachmentName, String commitMessage, String author,
						Date date, long size, boolean delete, ObjectId id) {
		super(commitMessage, author, date, size, id, delete);
		this.parentName = parentName;
		this.attachmentName = attachmentName;
	}

	public AttachmentCacheItem(String parentName, String fileName, String commitMsg, String author, Date date, long size, ObjectId id) {
		this(parentName, fileName, commitMsg, author, date, size, false, id);
	}

	public String getParentName() {
		return parentName;
	}

	public String getAttachmentName() {
		return attachmentName;
	}
}
