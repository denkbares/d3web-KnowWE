/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action.sync.server;

import java.io.File;
import java.io.IOException;

import de.knowwe.dialog.repository.ArchiveRepository;

public class SyncServerContext {
	
	private static SyncServerContext INSTANCE;
	private final File rootFolder;
	private final ArchiveRepository archiveRepository;
	
	private SyncServerContext(File rootFolder) throws IOException {
		this.rootFolder = rootFolder;
		this.archiveRepository = new ArchiveRepository(new File(this.rootFolder, "repository"));
	}
	
	public static SyncServerContext getInstance() {
		return INSTANCE;
	}
	
	public static void initInstance(File rootFolder) throws IOException {
		INSTANCE = new SyncServerContext(rootFolder);
	}
	
	public ArchiveRepository getRepository() {
		return this.archiveRepository;
	}
}
