package org.apache.wiki.providers.gitCache.items;

import java.util.Date;

import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Engine;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.NotNull;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 12.12.19
 */
public class AttachmentCacheItem extends GitCacheItem {
	private final String parentName;
	private final String attachmentName;

	public AttachmentCacheItem(String parentName, String attachmentName, String commitMessage, String author,
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

	@NotNull
	public Attachment toAttachment(Engine engine) {
		Attachment att = new org.apache.wiki.attachment.Attachment(engine, getParentName(), getAttachmentName());
		att.setVersion(getVersion());
		att.setAuthor(getAuthor());
		att.setSize(getSize());
		att.setLastModified(getDate());
		att.setAttribute(WikiPage.CHANGENOTE, getFullMessage());
		return att;
	}
}
