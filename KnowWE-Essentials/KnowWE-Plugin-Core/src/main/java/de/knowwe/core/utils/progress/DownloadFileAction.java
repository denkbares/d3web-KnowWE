package de.knowwe.core.utils.progress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import de.d3web.strings.Strings;
import de.d3web.utils.Files;
import de.d3web.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Class that allows to download files created on the server. This class shall
 * be utilized to download e.g. newly created temp files. For security reasons
 * only file can be downloaded if their direct parent folders has previously
 * added to the set of allowed directories.
 * 
 * @see #allowDirectory(File)
 * 
 * @author Albrecht Striffler, Volker Belli (denkbares GmbH)
 * @created 08.02.2014
 */
public class DownloadFileAction extends AbstractAction {

	private static final Set<File> allowedDirectories = new HashSet<>();
	private static File defaultTempDirectory = null;

	/**
	 * Enables read access for the specified directory. After that call
	 * potentially all files that are directly located in that folder can be
	 * accessed by this action. It does not (!) automatically grant read access
	 * to the sub-folders of the specified folder.
	 * 
	 * @created 08.02.2014
	 * @param directory the directory to grant read access for
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
		try {
			context.setContentType("application/x-bin");
			context.setHeader("Content-Disposition", "attachment;filename=\"" + name + "\"");

			FileInputStream in = new FileInputStream(file);
			OutputStream out = context.getOutputStream();
			Streams.streamAndClose(in, out);
		}
		finally {
			file.delete();
			file.deleteOnExit();
		}
	}

	/**
	 * Returns a temp directory that has automatically read access granted for
	 * this action. Therefore a call to {@link #allowDirectory(File)} is not
	 * required using this method.
	 * 
	 * @created 08.02.2014
	 * @return a temp directory with granted access rights
	 */
	public static File getTempDirectory() throws IOException {
		if (defaultTempDirectory == null) {
			defaultTempDirectory = Files.createTempDir();
			allowDirectory(defaultTempDirectory);
		}
		return defaultTempDirectory;
	}

}