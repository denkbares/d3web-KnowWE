/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import at.spardat.xma.xdelta.JarDelta;

public class ArchiveStorage {

	private final File rootFolder;

	/**
	 * Class for dynamically created Patch files to update a given archive name
	 * and its original checksum.
	 * 
	 * @author Volker Belli
	 */
	public class PatchFile {

		private final String archiveName;
		private final File file;
		private final boolean patch;

		private PatchFile(String archiveName, File file, boolean patch) {
			this.archiveName = archiveName;
			this.file = file;
			this.patch = patch;
		}

		public String getArchiveName() {
			return archiveName;
		}

		public File getFile() {
			return file;
		}

		public boolean isIncrementalPatch() {
			return patch;
		}
	}

	/**
	 * Class representing a archive file contained (managed) by the archive
	 * storage.
	 * 
	 * @author Volker Belli
	 */
	public class StorageFile {

		private final String archiveName;
		private final File file;
		private final String checksum;

		private StorageFile(String archiveName, File file, String checksum) {
			this.archiveName = archiveName;
			this.file = file;
			this.checksum = checksum;
		}

		public String getArchiveName() {
			return archiveName;
		}

		public File getFile() {
			return file;
		}

		public String getChecksum() {
			return checksum;
		}
	}

	public ArchiveStorage(File rootFolder) {
		this.rootFolder = rootFolder;
	}

	public StorageFile addArchive(File archiveFile)
			throws IOException {
		// create target name
		ZipFile zipFile = new ZipFile(archiveFile);
		String checksum = ArchiveUtils.checksum(zipFile);
		zipFile.close();
		String targetName = createRepositoryName(archiveFile.getName(), checksum);
		File targetFile = new File(this.rootFolder, targetName);

		// copy file to repository folder
		copyFile(archiveFile, targetFile);
		return new StorageFile(archiveFile.getName(), targetFile, checksum);
	}

	public StorageFile getFile(String archiveName, String checksum) throws FileNotFoundException {
		String baseName = createRepositoryName(archiveName, checksum);
		File file = new File(this.rootFolder, baseName);
		if (!file.exists()) throw new FileNotFoundException(
				"requested archive file does not exists in storage: '" + baseName + "'");
		return new StorageFile(archiveName, file, checksum);
	}

	public PatchFile getPatch(String archiveName, String originalChecksum, String targetChecksum) throws IOException {
		String baseName = createRepositoryName(archiveName, originalChecksum);
		String currentName = createRepositoryName(archiveName, targetChecksum);

		// wenn die aktuelle Version des Archivs nicht bekannt ist, dann Fehler
		if (currentName == null) throw new FileNotFoundException("archive '" + archiveName
				+ "' not available in repository '" + this.rootFolder + "'");

		// wenn die Versionen gleich sind, dann kein Patch nötig
		File baseFile = new File(this.rootFolder, baseName);
		File currentFile = new File(this.rootFolder, currentName);
		if (currentFile.equals(baseFile)) return null;

		// wenn die Basis-version nicht existiert, dann einfach das aktuelle
		// Archiv als Patch bereitstellen
		if (!baseFile.exists()) {
			return new PatchFile(archiveName, currentFile, false);
		}

		// ansonsten einen Patch zurückliefern und vorher ggf. erzeugen
		String patchName = createRepositoryName(currentName, "from-" + originalChecksum) + ".patch";
		File patchFile = new File(this.rootFolder, patchName);
		if (!patchFile.exists()) {
			new JarDelta().computeDelta(
					new ZipFile(baseFile),
					new ZipFile(currentFile),
					new ZipOutputStream(new FileOutputStream(patchFile)));
		}
		return new PatchFile(archiveName, patchFile, true);
	}

	private void copyFile(File source, File target) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(target);
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
		}
		finally {
			if (in != null) in.close();
			if (out != null) out.close();
		}
	}

	private String createRepositoryName(String sourceFileName, String checksum) {
		int dotIndex = sourceFileName.lastIndexOf('.');
		return (dotIndex == -1) ? sourceFileName + "-" + checksum
				: sourceFileName.substring(0, dotIndex) + "-" + checksum
						+ sourceFileName.substring(dotIndex);
	}

}
