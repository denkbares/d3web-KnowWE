/*
 * Copyright (C) 2020 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.wikiConnector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Files;
import com.denkbares.utils.Streams;

/**
 * Utility class with methods to handle wiki attachments.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 18.03.2020
 */
public class WikiAttachments {
	private static File TEMP_FOLDER = null;

	/**
	 * Copies the specified WikiAttachment to a temporary file, or uses an existing file if it has already been copied
	 * before. The temp file is automatically marked to be deleted on system exit.
	 * <p>
	 * You should prefer the method {@link WikiAttachment#asFile()}, as it may be able to avoid to create the temp file,
	 * based on the underlying attachment provider.
	 *
	 * @param attachment the attachment to be represented as a temp file
	 * @return the temp file of the wiki attachment
	 * @throws IOException if the wiki attachment could not been read, or the temp file could not been created
	 * @see WikiAttachment#asFile()
	 */
	public static synchronized File asTempFile(WikiAttachment attachment) throws IOException {
		// create a unique file, including article, file, version, date, and file length
		String filename = Strings.encodeURL(attachment.getFileName());
		String article = Strings.encodeURL(attachment.getParentName());
		long time = attachment.getDate().getTime();
		long size = attachment.getSize();
		int version = attachment.getVersion();
		File file = new File(requireTempFolder(),
				String.format("wiki-attachment-%s-#%d-%d-%d-%s", article, version, size, time, filename));

		// if the file already exists, use it
		if (file.exists() && (file.lastModified() == time) && (file.length() == size)) {
			return file;
		}

		// otherwise create the file as required, and mark to be deleted on system exit
		try (InputStream in = attachment.getInputStream(); OutputStream out = new FileOutputStream(file)) {
			Streams.stream(in, out);
		}
		file.setLastModified(time);
		file.deleteOnExit();

		return file;
	}

	@NotNull
	private static File requireTempFolder() throws IOException {
		if (TEMP_FOLDER == null) {
			// create the folder only if required first, mark to be deleted on exit
			TEMP_FOLDER = Files.createTempDir();
			TEMP_FOLDER.deleteOnExit();
		}
		return TEMP_FOLDER;
	}
}
