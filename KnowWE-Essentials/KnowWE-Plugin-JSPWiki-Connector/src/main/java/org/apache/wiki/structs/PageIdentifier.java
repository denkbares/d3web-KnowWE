package org.apache.wiki.structs;

import java.io.File;
import java.io.IOException;

import org.apache.wiki.gitBridge.JSPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.wiki.providers.AbstractFileProvider.FILE_EXT;

public interface PageIdentifier {

	Logger LOGGER = LoggerFactory.getLogger(PageIdentifier.class);

	String pageName();

	String basePath();

	File accordingFile();

	default boolean exists() {
		File pageFile = accordingFile();
		if (pageFile == null) {
			return false;
		}
		try {

			return pageFile.exists() && pageFile.getCanonicalPath().equals(pageFile.getAbsolutePath());
		}
		catch (IOException e) {
			LOGGER.warn("Could not evaluate canonical path", e);
		}
		return pageFile.exists();
	}

	int version();

	static DefaultPageIdentifier fromPagename(String basePath, String pageName, int version) {
		if (pageName.endsWith(".txt")) {
			LOGGER.info("Trying to create a page with a suspicious ending, might result in unwanted consequences!");
		}
		return new DefaultPageIdentifier(basePath, pageName, PageIdentifiertype.Name, version);
	}

	static DefaultPageIdentifier fromPath(String basePath, String path, int version) {
		return new DefaultPageIdentifier(basePath, path, PageIdentifiertype.Path, version);
	}

	static PageIdentifier fromFile(String filesystemPath, File file) {
		String fileName = file.getName();
		int cutpoint = fileName.indexOf(FILE_EXT);
		String pageName = JSPUtils.unmangleName(fileName.substring(0, cutpoint));
		return PageIdentifier.fromPagename(filesystemPath, pageName, -1);
	}

	static DefaultPageIdentifier fromEncodedname(String basePath, String encodedName, int version) {
		return new DefaultPageIdentifier(basePath, encodedName, PageIdentifiertype.EncodedName, version);
	}

	boolean isValid();

	String fileName();
}
