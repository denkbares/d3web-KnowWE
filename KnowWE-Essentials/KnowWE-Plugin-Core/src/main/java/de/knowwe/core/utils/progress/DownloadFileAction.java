package de.knowwe.core.utils.progress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Files;
import com.denkbares.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Class that allows to download files created on the server. This class shall be utilized to download e.g. newly
 * created temp files. For security reasons only file can be downloaded if their direct parent folders has previously
 * added to the set of allowed directories.
 *
 * @author Albrecht Striffler, Volker Belli (denkbares GmbH)
 * @created 08.02.2014
 * @see #allowDirectory(File)
 */
public class DownloadFileAction extends AbstractAction {

	private static final Set<File> allowedDirectories = new HashSet<>();
	private static File defaultTempDirectory = null;

	/**
	 * Enables read access for the specified directory. After that call potentially all files that are directly located
	 * in that folder can be accessed by this action. It does not (!) automatically grant read access to the sub-folders
	 * of the specified folder.
	 *
	 * @param directory the directory to grant read access for
	 * @created 08.02.2014
	 */
	public static void allowDirectory(File directory) {
		allowedDirectories.add(directory);
	}

	public static void checkAllowed(File file) throws IOException {
		File directory = file.getParentFile();
		if (!allowedDirectories.contains(directory)) {
			throw new IOException("You are not allowed to access that file");
		}
	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		String fileParameter = context.getParameter("file");
		String filePath = Strings.decodeURL(fileParameter);
		String nameParameter = context.getParameter("name");
		String name = Strings.decodeURL(nameParameter);

		File file = new File(filePath);
		checkAllowed(file);

		try (InputStream in = new FileInputStream(file); OutputStream out = context.getOutputStream()) {
			context.setContentType(BINARY);
			context.setHeader("Content-Disposition", "attachment;filename=\"" + name + "\"");
			Streams.stream(in, out);
		}
		finally {
			file.delete();
			file.deleteOnExit();
		}
	}

	/**
	 * Returns a temp directory that has automatically read access granted for this action. Therefore a call to {@link
	 * #allowDirectory(File)} is not required using this method.
	 *
	 * @return a temp directory with granted access rights
	 * @created 08.02.2014
	 */
	public static File getTempDirectory() throws IOException {
		if (defaultTempDirectory == null) {
			defaultTempDirectory = Files.createTempDir();
			allowDirectory(defaultTempDirectory);
		}
		return defaultTempDirectory;
	}
}
