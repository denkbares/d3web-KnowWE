/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import at.spardat.xma.xdelta.JarPatcher;
import de.knowwe.dialog.Utils;
import de.knowwe.dialog.repository.ArchiveUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;

public class SyncClientContext {

	/**
	 * One and only instance of the SyncClientContext
	 */
	private static SyncClientContext INSTANCE;

	public static final String SUFFIX_BAK = ".bak";
	public static final String SUFFIX_PATCH = ".patch";
	public static final String SUFFIX_PATCHED = ".patched";

	/**
	 * The SyncState represents the different states the sync process runs
	 * through during updating the client.
	 *
	 * @author Volker Belli
	 */
	public enum SyncState {
		NOT_STARTED,
		CONNECTING_SERVER,
		DOWNLOAD_IN_PROGRESS,
		PATCH_IN_PROGRESS,
		RESTART_REQUIRED,
		FINISHED,
		CANCEL_REQUESTED,
		CANCELED,
		ERROR_OCCURED
	}

	public class UpdateFile {

		private UpdateFile(File localFile, String checksum) {
			this.localFile = localFile;
			this.checksum = checksum;
		}

		public final File localFile;
		public final String checksum;
		public int downloadSize;
		public long changeDate;
		public boolean isPatch;

		/**
		 * Returns the file where the downloaded patch is located.
		 */
		public File getPatchFile() {
			String filename = this.localFile.getName();
			if (this.isPatch) filename += SUFFIX_PATCH;
			return new File(patchesFolder, filename);
		}

		/**
		 * Returns the file that has been created by applying the patch to the
		 * original file (localFile).
		 */
		public File getPatchedFile() {
			return new File(this.localFile.getAbsolutePath() + SUFFIX_PATCHED);
		}
	}

	@SuppressWarnings("serial")
	private static class CancelException extends Exception {
	}

	private final File rootFolder;
	private final File patchesFolder;

	private SyncState syncState = SyncState.NOT_STARTED;
	private long currentProgress = 0;
	private long totalProgress = 1;
	private final long PROGRESS_LOCAL_FILE_WEIGHT = 1;
	private final long PROGRESS_DOWNLOAD_FILE_WEIGHT = 10;
	private final long PROGRESS_ACTIVITY_STEP_WEIGHT = 1000;

	private SyncClientContext(File rootFolder) {
		this.rootFolder = rootFolder;
		this.patchesFolder = new File(rootFolder, "update");
		if (!this.patchesFolder.exists()) this.patchesFolder.mkdirs();
	}

	/**
	 * Access the singleton instance of the SyncClientContext
	 *
	 * @return singleton instance
	 * @throws IOException never thrown at the moment
	 */
	public static SyncClientContext getInstance() {
		return INSTANCE;
	}

	public static void initInstance(File rootFolder) {
		INSTANCE = new SyncClientContext(rootFolder);
	}

	/**
	 * Returns the progress of the current update process. If there is no
	 * current update progress, it delivers the progress of the last update
	 * process.
	 *
	 * @return the progress between 0.0 and 1.0
	 */
	public float getProgess() {
		return (float) currentProgress / (float) totalProgress;
	}

	private void initProgress(long total) {
		this.currentProgress = 0;
		this.totalProgress = total;
	}

	private void incrementProgress(long increment) {
		this.currentProgress += increment;
	}

	/**
	 * Returns the State of the current sync process (if there is any) or the
	 * state of the sync client.
	 *
	 * @return the current sync state
	 */
	public SyncState getSyncState() {
		return this.syncState;
	}

	/**
	 * Returns if the current sync state is an idle state
	 *
	 * @return if sync is idle
	 */
	public synchronized boolean isIdle() {
		switch (this.syncState) {
			case CONNECTING_SERVER:
			case DOWNLOAD_IN_PROGRESS:
			case PATCH_IN_PROGRESS:
			case CANCEL_REQUESTED:
			case RESTART_REQUIRED:
				return false;

			case CANCELED:
			case FINISHED:
			case NOT_STARTED:
			case ERROR_OCCURED:
				return true;

			default:
				throw new IllegalStateException();
		}
	}

	/**
	 * Starts the synchronization process if it is idle.
	 */
	public synchronized void startSync(final String serverURL, final String alias) {
		// do not start if we are still running
		if (!isIdle()) return;
		new Thread(new Runnable() {

			@Override
			public void run() {
				SyncClientContext.this.doSync(serverURL, alias);
			}
		}, "sync process").start();
	}

	/**
	 * Cancels the synchronization process if it is running.
	 */
	@SuppressWarnings("incomplete-switch")
	public synchronized void cancelSync() {
		switch (this.syncState) {
			case CONNECTING_SERVER:
			case DOWNLOAD_IN_PROGRESS:
			case PATCH_IN_PROGRESS:
				this.syncState = SyncState.CANCEL_REQUESTED;
		}
	}

	/**
	 * Searches and applies existing patches that has been created by a previous
	 * synchronization but have not been applied immediately (e.g. because of
	 * the original file was locked).
	 * <p>
	 * This method may be used after client restart.
	 */
	public void applyExistingPatches() {
		applyExistingPatches(rootFolder);
	}

	private void applyExistingPatches(File folder) {
		// otherwise search directories recursively
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				applyExistingPatches(file);
			}
			else {
				String name = file.getName().toLowerCase();
				if (name.endsWith(".jar") || name.endsWith(".zip")) {
					File patchFile = new File(file.getAbsolutePath() + SUFFIX_PATCHED);
					if (patchFile.exists()) {
						File bakFile = new File(file.getAbsolutePath() + SUFFIX_BAK);
						file.renameTo(bakFile);
						if (patchFile.renameTo(file)) {
							bakFile.delete();
						}
						else {
							bakFile.renameTo(file);
						}
					}
				}
			}
		}
	}

	private void doSync(String serverURL, String alias) {
		try {
			this.syncState = SyncState.CONNECTING_SERVER;
			initProgress(1);
			List<UpdateFile> files = negotiateUpdateFiles(serverURL, alias);
			checkCancel();

			// init progess bar
			// file size of each file (*2 because of downloading and patching)
			// plus 1024 for each action (connect, download single file, patch
			// single file)
			long total = PROGRESS_ACTIVITY_STEP_WEIGHT;
			for (UpdateFile file : files) {
				total += (file.downloadSize) * PROGRESS_DOWNLOAD_FILE_WEIGHT;
				total += (file.downloadSize + file.localFile.length()) * PROGRESS_LOCAL_FILE_WEIGHT;
				total += 2 * PROGRESS_ACTIVITY_STEP_WEIGHT;
			}
			initProgress(total);
			incrementProgress(PROGRESS_ACTIVITY_STEP_WEIGHT);

			this.syncState = SyncState.DOWNLOAD_IN_PROGRESS;
			for (UpdateFile file : files) {
				downloadPatch(serverURL, file, alias);
				incrementProgress(PROGRESS_ACTIVITY_STEP_WEIGHT);
				checkCancel();
			}

			boolean requireRestart = false;
			this.syncState = SyncState.PATCH_IN_PROGRESS;
			for (UpdateFile file : files) {
				boolean success = applyPatch(file);
				if (!success) requireRestart = true;
				incrementProgress(PROGRESS_ACTIVITY_STEP_WEIGHT);
				checkCancel();
			}

			this.syncState = (requireRestart) ? SyncState.RESTART_REQUIRED : SyncState.FINISHED;
		}
		catch (CancelException e) {
			this.syncState = SyncState.CANCELED;
		}
		catch (IOException e) {
			Log.severe("synchronisation error", e);
			this.syncState = SyncState.ERROR_OCCURED;
		}
		catch (Exception e) {
			Log.severe("unexpected internal synchronisation error", e);
			this.syncState = SyncState.ERROR_OCCURED;
		}
	}

	private synchronized void checkCancel() throws CancelException {
		if (this.syncState == SyncState.CANCEL_REQUESTED) {
			throw new CancelException();
		}
	}

	/**
	 * Searches all archive files and negotiates their updates with the server.
	 *
	 * @param serverURL the root url of the sync server servlet
	 * @return the negotiated update files
	 * @throws IOException
	 */
	public List<UpdateFile> negotiateUpdateFiles(String serverURL, String alias) throws IOException {
		// search local files
		List<UpdateFile> files = searchUpdateFiles();

		// and negotiate them with the server
		InputStream in = getServerRespondXML(serverURL, files, alias);
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(in);

			// prepare map of files for quick access
			Map<String, UpdateFile> fileByName = new HashMap<>();
			for (UpdateFile file : files) {
				fileByName.put(file.localFile.getName().toLowerCase(), file);
			}

			// then create the result set
			List<UpdateFile> result = new LinkedList<>();
			for (Node fileNode = doc.getFirstChild()
					.getFirstChild(); fileNode != null; fileNode = fileNode.getNextSibling()) {
				if (fileNode.getNodeName().equals("#text")) continue;
				String name = fileNode.getTextContent();
				String size = fileNode.getAttributes().getNamedItem("size").getTextContent();
				String time = fileNode.getAttributes().getNamedItem("time").getTextContent();
				String patch = fileNode.getAttributes().getNamedItem("patch").getTextContent();
				UpdateFile file = fileByName.get(name.toLowerCase());
				file.downloadSize = Integer.parseInt(size);
				file.changeDate = Long.parseLong(time);
				file.isPatch = Boolean.parseBoolean(patch);
				result.add(file);
			}

			return result;
		}
		catch (ParserConfigurationException e) {
			throw new IllegalStateException("internal configuration error", e);
		}
		catch (SAXException e) {
			throw new IOException("internal error, bad xml format from sync server", e);
		}
	}

	/**
	 * Negotiates the files to be updated with the server and returns the
	 * servers respond XML
	 *
	 * @return the server respond
	 */
	private static InputStream getServerRespondXML(String serverURL, List<UpdateFile> files, String alias) throws IOException {
		if (!serverURL.endsWith("/")) serverURL += "/";
		serverURL += "KnowWE/action/GetUpdateInfo";

		// prepare connection
		URLConnection conn = new URL(serverURL).openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		Writer writer = new OutputStreamWriter(conn.getOutputStream());
		writer.append("xml=").append(Strings.encodeURL(createRequestXML(files, alias)));
		writer.flush();
		writer.close();
		// connect
		conn.connect();
		// use results
		return conn.getInputStream();
	}

	private static String createRequestXML(List<UpdateFile> files, String alias) throws IOException {
		StringBuilder xml = new StringBuilder();
		xml.append("<archives alias='").append(alias).append("'>\n");
		for (UpdateFile file : files) {
			xml.append("\t<file checksum='").append(file.checksum).append("'>");
			xml.append(Utils.encodeXML(file.localFile.getName()));
			xml.append("</file>\n");
		}
		xml.append("</archives>");

		return xml.toString();
	}

	private void downloadPatch(String serverURL, UpdateFile file, String alias) throws IOException, CancelException {
		if (!serverURL.endsWith("/")) serverURL += "/";
		serverURL +=
				"sync.server.GetUpdateFile" +
						"?file=" + Strings.encodeURL(file.localFile.getName()) +
						"&checksum=" + Strings.encodeURL(file.checksum) +
						"&alias=" + Strings.encodeURL(alias);

		// prepare connection
		URLConnection conn = new URL(serverURL).openConnection();
		conn.connect();
		checkCancel();

		// write results to disc
		try (OutputStream out = new FileOutputStream(file.getPatchFile())) {
			InputStream in = conn.getInputStream();
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
				incrementProgress(len * PROGRESS_DOWNLOAD_FILE_WEIGHT);
				checkCancel();
			}
		}
	}

	/**
	 * Applies a patch from the downloaded file. If the actual file cannot be
	 * replaced (but the patched file has been created successfully) the method
	 * returns false. If the patch has been created and the original file has
	 * been replaced it return true; If the patch cannot be applied, an error is
	 * thrown.
	 *
	 * @param file The UpdateFile to be patched
	 * @return if is was possible to replace the file after the patch has been
	 * created
	 * @throws IOException
	 * @throws CancelException
	 */
	private boolean applyPatch(UpdateFile file) throws IOException, CancelException {
		final long fullProgress = (file.getPatchFile().length() + file.localFile.length())
				* PROGRESS_LOCAL_FILE_WEIGHT;

		File origFile = file.localFile;
		File patchFile = file.getPatchFile();
		File newFile = file.getPatchedFile();
		File bakFile = new File(origFile.getAbsolutePath() + SUFFIX_BAK);
		// delete existing files (maybe remaining from other sync trial)
		newFile.delete();
		bakFile.delete();

		if (file.isPatch) {
			ZipFile source = null, patch = null;
			ZipOutputStream output = null;
			// patch archive
			try {
				source = new ZipFile(origFile);
				patch = new ZipFile(patchFile);
				output = new ZipOutputStream(new FileOutputStream(newFile));
				JarPatcher patcher = new JarPatcher();
				patcher.applyDelta(source, patch, output);
			}
			finally {
				if (source != null) source.close();
				if (patch != null) patch.close();
				if (output != null) output.close();
			}
		}
		else {
			// copy archive
			if (!patchFile.renameTo(newFile)) {
				throw new IOException(
						"cannot copy update file to destination");
			}
			incrementProgress(fullProgress);
		}

		// wenn der Patch erzeugt werden konnte,
		// dann sofort das Patchfile löschen
		patchFile.delete();
		if (patchFile.exists()) {
			patchFile.deleteOnExit();
		}

		if (!origFile.renameTo(bakFile)) {
			// the original file cannot be renamed
			return false;
		}
		if (!newFile.renameTo(origFile)) {
			// the created file cannot be renamed
			// so restore the original file
			bakFile.renameTo(origFile);
			return false;
		}

		// remove other temporary files
		newFile.delete();
		bakFile.delete();
		return true;
	}

	private List<UpdateFile> searchUpdateFiles() throws IOException {
		List<UpdateFile> result = new LinkedList<>();
		searchUpdateFiles(rootFolder, result);
		return result;
	}

	private void searchUpdateFiles(File dir, List<UpdateFile> result) throws IOException {
		// exclude our patches folder from search
		if (dir.equals(patchesFolder)) return;

		// otherwise search directories recursively
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				searchUpdateFiles(file, result);
			}
			else {
				String name = file.getName().toLowerCase();
				if (name.endsWith(".jar") || name.endsWith(".zip")) {
					ZipFile zipFile = new ZipFile(file);
					// TODO: cache checksum if file has not been modified
					String hash = ArchiveUtils.checksum(zipFile);
					result.add(new UpdateFile(file, hash));
				}
			}
		}
	}

}
