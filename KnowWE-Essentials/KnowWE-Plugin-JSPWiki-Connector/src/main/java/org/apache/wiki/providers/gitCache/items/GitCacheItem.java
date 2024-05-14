package org.apache.wiki.providers.gitCache.items;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

/**
 * @author Josua Nürnberger (Feanor GmbH)
 * @created 12.12.19
 */
public class GitCacheItem {
	private final String fullMessage;
	private final String author;
	private final Date date;
	private final ObjectId id;
	private final boolean delete;
	private int version;
	private long size;

	public GitCacheItem(String fullMessage, String author, Date date, long size, ObjectId id, boolean delete) {
		this.fullMessage = fullMessage;
		this.author = author;
		this.date = date;
		this.size = size;
		this.id = id;
		this.delete = delete;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getFullMessage() {
		return fullMessage;
	}

	public String getAuthor() {
		return author;
	}

	public Date getDate() {
		return date;
	}

	public ObjectId getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public long getSize() {
		return this.size;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
