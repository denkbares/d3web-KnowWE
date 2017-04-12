package cc.knowwe.dialog.action;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.OS;
import com.denkbares.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Action that enables to call some specific external viewers.
 * 
 * @author Volker Belli
 */
public class ExternalViewer extends AbstractAction {

	private static class Viewer {

		private final Pattern file;
		private final OS os;
		private final String cmdLine;

		public Viewer(String fileRegex, OS os, String cmdLine) {
			this.file = Pattern.compile(fileRegex, Pattern.CASE_INSENSITIVE);
			this.os = os;
			this.cmdLine = cmdLine;
		}

		public boolean matches(String viewFile) {
			return os.isCurrentOS() && file.matcher(viewFile).find();
		}
	}

	private static final List<Viewer> viewers = new LinkedList<>();
	private static final File[] searchFolders = new File[] {
			null, // search app directly without any folder
			new File("."),
			new File(System.getProperty("user.dir")),
			new File(System.getProperty("user.home")),
			new File("/Applications"),
			new File("C:\\Programme"),
	};

	static {
		// you may add viewers to test reasons here
	}

	public static void loadViewers(File xmlFile) throws IOException {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.normalize();

			NodeList nodes = doc.getElementsByTagName("viewer");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element viewer = (Element) nodes.item(i);
				String cmdLine = viewer.getTextContent().trim();
				String file = viewer.getAttribute("file");
				String osName = viewer.getAttribute("os");
				if (Strings.isBlank(osName)) {
					addViewer(file, cmdLine);
				}
				else {
					addViewer(file, OS.valueOf(osName), cmdLine);
				}
			}
		}
		catch (SAXException e) {
			throw new IOException("unexpected xml error while reading: " + xmlFile, e);
		}
		catch (ParserConfigurationException e) {
			throw new IOException("unexpected xml error while reading: " + xmlFile, e);
		}
	}

	public static void addViewer(String fileRegex, OS os, String application) {
		viewers.add(new Viewer(fileRegex, os, application));
	}

	public static void addViewer(String fileRegex, String application) {
		addViewer(fileRegex, null, application);
	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		String rest = context.getPath();

		// look for registered viewer
		String viewerApp = null;
		for (Viewer viewer : viewers) {
			if (viewer.matches(rest)) {
				viewerApp = viewer.cmdLine;
				break;
			}
		}
		if (viewerApp == null) {
			context.sendError(401, "no viewer available for file " + rest);
			return;
		}

		// search viewer executable file
		for (File folder : searchFolders) {
			File file = new File(folder, viewerApp);
			if (file.exists() && file.canExecute()) {
				execCommand(new String[] {
						file.getCanonicalPath(), rest }, false);
				return;
			}
		}

		context.sendError(401, "no viewer available viewer executable found");
	}

	/**
	 * Calls 'an external program. Requires to have the program installed
	 * properly and specified full path to it. The command line may be called in
	 * a asyncron or synchron manner. if the command line is executed in a
	 * synchron manner, this method returns the command lines return code, -1
	 * otherwise.
	 * 
	 * @created 27.04.2011
	 * @param cmdLine the command line to be executed
	 * @param waitForEnd if the method should wait for the termination of the
	 *        executed command line
	 * @return the result code of the application or -1 if the method does not
	 *         wait for termination of the executed command line
	 * @throws IOException if there are any i/o problems during creation
	 */
	public static int execCommand(String[] cmdLine, boolean waitForEnd) throws IOException {
		Log.info("execute: " + Arrays.asList(cmdLine));
		Process process = Runtime.getRuntime().exec(cmdLine);
		try {
			Streams.streamAsync(process.getInputStream(), System.out);
			Streams.streamAsync(process.getErrorStream(), System.err);
			if (waitForEnd) {
				int exitCode = process.waitFor();
				return exitCode;
			}
			else {
				return -1;
			}
		}
		catch (InterruptedException e) {
			throw new IOException("unexpected interruption while creating pdf output", e);
		}
	}

}
