/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.repository;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class VersionSet {

	private final String version;
	private final String comment;
	private final Date createDate;
	private final List<ArchiveStorage.StorageFile> files = new LinkedList<>();
	
	public VersionSet(String version, String comment, Date createDate) {
		this.version = version;
		this.comment = comment;
		this.createDate = createDate;
	}

	public VersionSet createBranch(String version, String comment) {
		VersionSet versionSet = new VersionSet(version, comment, new Date());
		versionSet.files.addAll(this.files);
		return versionSet;
	}
	
	public void addFile(ArchiveStorage.StorageFile file) {
		this.files.add(file);
	}
	
	public ArchiveStorage.StorageFile getFile(String archiveName) {
		for (ArchiveStorage.StorageFile file : this.files) {
			if (file.getArchiveName().equalsIgnoreCase(archiveName)) {
				return file;
			}
		}
		return null;
	}
	
	public List<ArchiveStorage.StorageFile> getFiles() {
		return Collections.unmodifiableList(files);
	}

	public String getVersion() {
		return version;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public String getComment() {
		return comment;
	}
}
