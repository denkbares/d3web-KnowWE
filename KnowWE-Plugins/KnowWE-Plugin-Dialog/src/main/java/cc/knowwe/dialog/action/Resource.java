package cc.knowwe.dialog.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.activation.MimetypesFileTypeMap;

import cc.knowwe.dialog.SessionConstants;
import cc.knowwe.dialog.Utils;

import com.denkbares.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Searches within the resource folder for a certain resource specified by the path behind the
 * command name.
 * <p/>
 * The resource path may contain a archive path (zip/jar) followed by the relative path within the
 * archive.
 * 
 * @author Volker Belli
 */
public class Resource extends AbstractAction {

	private static final MimetypesFileTypeMap MIMETYPE_MAP = new MimetypesFileTypeMap();

	static {
		// the default map is missing png files, so we add them manually
		MIMETYPE_MAP.addMimeTypes("image/png png pnG pNg pNG Png PnG PNg PNG");
		MIMETYPE_MAP.addMimeTypes("text/css css CSS");
		MIMETYPE_MAP.addMimeTypes("text/javascript js Js jS JS");
	}

	private final String rootFolder = SessionConstants.DEFAULT_RESOURCE_FOLDER;

	// TODO: For developing purposes, no cache is implemented yet!

	@Override
	public void execute(UserActionContext context) throws IOException {
		String rest = context.getPath();
		File root = Utils.getRootDirectory(context);
		File current = new File(root, rootFolder);
		for (;;) {
			int splitPos = rest.indexOf('/');

			// if we reached the end, we assume
			// we found a file to be delivered
			if (splitPos == -1) {
				File file = new File(current, rest);
				checkAccess(root, file);
				deliverFile(context, file);
				break;
			}

			// otherwise put the search into the next level
			current = new File(current, rest.substring(0, splitPos));
			rest = rest.substring(splitPos + 1);

			// if the next level is an archive
			// we deliver the specified archives content
			if (current.isFile()) {
				checkAccess(root, current);
				deliverArchiveContent(context, current, rest);
				break;
			}

			// otherwise continue searching...
		}
	}

	private void deliverFile(UserActionContext context, File file) throws IOException {
		OutputStream out = context.getOutputStream();
		try (FileInputStream in = new FileInputStream(file.getAbsolutePath())) {
			context.setContentLength((int) file.length());
			context.setContentType(getContentType(file.getName()));
			Streams.stream(in, out);
			// cmdContext.setHeader("Cache-Control",
			// "public, max-age=60, s-maxage=60");
		}
		finally {
			out.flush();
		}
	}

	private void deliverArchiveContent(UserActionContext context, File archiveFile, String path) throws IOException {
		try (ZipFile zipFile = new ZipFile(archiveFile.getAbsolutePath())) {
			ZipEntry entry = zipFile.getEntry(path);
			// if the entry has not been found,
			// try to access with "\" as path separator
			// because some windows tools seams to create malformed zip entries
			if (entry == null) entry = zipFile.getEntry(path.replace('/', '\\'));
			if (entry == null) {
				throw new FileNotFoundException("entry '" + path + "' not found in archive '"
						+ archiveFile.getPath() + "'");
			}

			InputStream in = zipFile.getInputStream(entry);
			Streams.stream(in, context.getOutputStream());
			in.close();

			context.setContentLength((int) entry.getSize());
			context.setContentType(getContentType(entry.getName()));
			context.setHeader("Cache-Control", "public, max-age=60, s-maxage=60");
		}
	}

	/**
	 * Checks weather the specified file is allowed to be sent to the user. The file has to be
	 * located in the resource root folder (avoid outbreak).
	 * 
	 * @param file to be checked
	 * @throws IOException if the file is outside the folder
	 */
	private void checkAccess(File rootFolder, File file) throws IOException {
		String filePath = file.getCanonicalPath();
		String rootPath = rootFolder.getCanonicalPath();
		if (!filePath.startsWith(rootPath)) {
			throw new FileNotFoundException(
					"access violation, not allowed to access files outside resource folder");
		}
	}

	private static String getContentType(String path) {
		return MIMETYPE_MAP.getContentType(path);
	}

}
