package org.apache.wiki.structs;

import java.io.File;
import java.nio.file.Paths;

import org.apache.wiki.gitBridge.JSPUtils;

import static org.apache.wiki.providers.AbstractFileProvider.FILE_EXT;

public class DefaultPageIdentifier implements PageIdentifier {
	private final String identifierValue;
	private final PageIdentifiertype identifierType;

	private final int version;
	private final String basePath;

	protected DefaultPageIdentifier(String basePath, String identifierValue, PageIdentifiertype type, int version) {
		this.identifierValue = identifierValue;
		this.identifierType = type;
		this.version = version;
		this.basePath = basePath;
	}



	@Override
	public File accordingFile() {
		String fileName = null;
		if (this.identifierType == PageIdentifiertype.Name) {
			fileName = JSPUtils.mangleName(pageName());
		}
		if (this.identifierType == PageIdentifiertype.EncodedName) {
			fileName = this.identifierValue;
		}

		if (fileName == null) {
			return null;
		}
		//append .txt as thats the way stuff is stored in the wiki!
		fileName += FILE_EXT;

//		String fileName = this.attemptGetFilename(pageName(), false);

		File file = Paths.get(basePath, fileName).toFile();
		if (file.exists()) {
			return file;
		}
		return null;
	}

	@Override
	public String pageName() {
		if (this.identifierType == PageIdentifiertype.Name) {
			return this.identifierValue;
		}
		if (this.identifierType == PageIdentifiertype.EncodedName) {
			return JSPUtils.unmangleName(this.identifierValue);
		}

		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public String basePath() {
		return this.basePath;
	}

	@Override
	public int version() {
		return this.version;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public String fileName() {
		File file = this.accordingFile();
		if (file == null || !file.exists()) {
			return null;
		}
		//TODO im not sure if this has to be relative to the base path ..
		return file.getName();
	}

	@Override
	public String toString() {
		return pageName();
	}
}

/**
 * This is only used for the the DefaultPageIdentifier, do not use elsewhere!
 */
enum PageIdentifiertype {
	Name, EncodedName, Path;
}


