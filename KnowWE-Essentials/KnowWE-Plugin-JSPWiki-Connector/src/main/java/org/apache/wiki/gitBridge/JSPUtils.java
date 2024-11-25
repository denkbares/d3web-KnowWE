package org.apache.wiki.gitBridge;

import java.io.File;

import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.util.TextUtil;
import org.jetbrains.annotations.NotNull;

import static org.apache.wiki.providers.BasicAttachmentProvider.DIR_EXTENSION;

public class JSPUtils {

	@NotNull
	public static String getAttachmentDir(String page) {
		return mangleName(page) + DIR_EXTENSION;
	}

	public static String getPath(Attachment att) {
		return getAttachmentDir(att.getParentName()) + "/" + mangleName(att.getFileName());
	}

	@NotNull
	public static String mangleName(String page) {
		return TextUtil.urlEncodeUTF8(page);
	}

	public static String unmangleName(final String filename) {
		return TextUtil.urlDecodeUTF8(filename);
	}


	public static File  findAttachmentFile(String parentName, String fileName, String storangeDir) throws ProviderException {
		return new File(findPageDir(parentName, storangeDir), mangleName(fileName));
	}

	public static File findPageDir(String page, String storageDir) throws ProviderException {
		File dir = new File(storageDir, JSPUtils.getAttachmentDir(page));
		if (!dir.exists()) {
			dir = new File(storageDir, JSPUtils.getAttachmentDir(page.replace("+", " ")));
		}
		if (dir.exists() && !dir.isDirectory()) {
			throw new ProviderException("Attachment directory for " + page + " is not a directory");
		}
		return dir;
	}



	public static String getAttachmentDir(Attachment att) {
		 return mangleName(att.getParentName()) + DIR_EXTENSION;
	}
}
