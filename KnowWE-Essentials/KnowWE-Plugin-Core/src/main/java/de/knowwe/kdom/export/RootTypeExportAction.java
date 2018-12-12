/*
 * Copyright (C) 2012 denkbares GmbH
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.kdom.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.denkbares.utils.OS;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.Type;

/**
 * 
 * 
 * @author Johanna
 * @created 10.11.2012
 */
public class RootTypeExportAction extends AbstractAction {

	// path of the local dot-Installation
	private static String DOT_INSTALLATION;

	// sources for the dot-file
	private String dotSource;
	private List<String> dotSourceLabel;
	private Map<Edge, String> dotSourceRelations;

	// savepath
	private String realPath;
	private String tmpPath;
	private static final String TMP_FOLDER = "tmp";
	private static final String KNOWWEEXTENSION_FOLDER = "KnowWEExtension";
	private String path;
	private static String FILE_SEPARATOR = System.getProperty("file.separator");

	@Override
	public void execute(UserActionContext context) throws IOException {
		String filename = "RootType.svg";
		context.setContentType("application/x-bin");
		context.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
		DOT_INSTALLATION = "dot";

		ServletContext servletContext = context.getServletContext();
		if (servletContext == null) return; // at wiki startup only
		tmpPath = KNOWWEEXTENSION_FOLDER + FILE_SEPARATOR + TMP_FOLDER + FILE_SEPARATOR;
		realPath = servletContext.getRealPath("");
		path = realPath + FILE_SEPARATOR + tmpPath;

		RootType rt = RootType.getInstance();
		setGeneralGraphSettings();
		dotSourceLabel = new LinkedList<>();
		dotSourceRelations = new LinkedHashMap<>();
		insertMainConcept(rt.getName() + rt.hashCode());

		addSuccessors(rt);
		connectSources();
		writeFiles();
		downloadsvg(context);
	}

	/**
	 * 
	 * 
	 * @created 10.12.2012
	 */
	private void setGeneralGraphSettings() {
		// general node settings
		String fontcolor = "black";
		String shape = "ellipse";
		String fontsize = "18";
		String fontname = "Helvetica";

		// general edge settings
		String arrowtail = "normal";
		String color = "black";

		// general graph settings
		String ranksep = "2.5";
		String nodesep = "0.75";
		String rankdir = "LR";

		dotSource = "digraph finite_state_machine {\nnode [fontcolor=\"" + fontcolor
				+ "\" shape=\"" + shape + "\" fontsize=\"" + fontsize + "\" fontname=\"" + fontname
				+ "\"];\n"
				+ "edge [arrowtail=\"" + arrowtail + "\" color=\"" + color + "\"];\n"
				+ "ranksep = " + ranksep + ";\nnodesep = " + nodesep + ";\nrankdir = \"" + rankdir
				+ "\";\n";
	}

	private void downloadsvg(UserActionContext context) throws IOException {
		File svg = new File(path + "RootType.svg");
		checkReadable(svg);
		FileInputStream fis = new FileInputStream(svg);
		OutputStream ous = context.getOutputStream();
		byte[] readBuffer = new byte[2156];
		int bytesIn = 0;
		while ((bytesIn = fis.read(readBuffer)) != -1)
		{
			ous.write(readBuffer, 0, bytesIn);
		}
		// close the Stream
		fis.close();
		ous.close();
	}

	/**
	 * 
	 * 
	 * @created 10.11.2012
	 * @param concept
	 */
	private void insertMainConcept(String concept) {
		// Main Concept Attributes
		String style = "filled";
		String fillcolor = "yellow";
		String fontsize = "14";
		String shape = "ellipse";

		String conceptKey = "\"" + concept + "\"";
		String conceptValue = "[ style=\"" + style + "\" fillcolor=\"" + fillcolor
				+ "\" fontsize=\"" + fontsize + "\" shape=\"" + shape + "\"];\n";
		dotSourceLabel.add(conceptKey + conceptValue);
	}

	/**
	 * 
	 * 
	 * @created 10.11.2012
	 * @param type
	 */
	private void addSuccessors(Type type) {
		List<Type> children = type.getChildrenTypes();
		if (children == null) return;
		for (Type child : children) {
			String childString = child.getName() + child.hashCode();
			String typeString = type.getName() + type.hashCode();
			if (!dotSourceLabel.contains("\"" + childString + "\"\n")) {
				addConcept(typeString, childString);
				addSuccessors(child);
			}
			else {
				addConcept(typeString, childString);
			}
		}
	}

	/**
	 * 
	 * 
	 * @created 10.11.2012
	 * @param from
	 * @param to
	 */
	private void addConcept(String from, String to) {
		String newLineLabel = "\"" + to + "\"\n";

		Edge newLineRelationsKey = new Edge(from, "", to);
		String newLineRelationsValue = "\n";

		if (!dotSourceLabel.contains(newLineLabel)) {
			dotSourceLabel.add(newLineLabel);
		}
		if (!dotSourceRelations.containsKey(newLineRelationsKey)
				|| (dotSourceRelations.get(newLineRelationsKey)) != newLineRelationsValue) {
			dotSourceRelations.put(newLineRelationsKey, newLineRelationsValue);
		}

	}

	/**
	 * 
	 * 
	 * @created 11.11.2012
	 */
	private void connectSources() {
		// the label of the RootType is added separately (because it is the only
		// label with specified additional attributes
		for (String label : dotSourceLabel) {
			RootType rt = RootType.getInstance();
			if (label.equals("\"" + rt.getClass().toString() + rt.hashCode() + "\"")) {
				dotSource += label;
			}
		}
		// iterate over the relations and add them to the dotSource
		for (Edge key : dotSourceRelations.keySet()) {
			dotSource += "\"" + key.getSubject() + "\"" + " -> " + "\"" + key.getObject() + "\" "
					+ dotSourceRelations.get(key);
		}

		dotSource += "}";
	}

	/**
	 * 
	 * 
	 * @created 11.11.2012
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void writeFiles() throws FileNotFoundException, IOException {
		File dot = createFile("dot", path);
		File svg = createFile("svg", path);

		writeDot(dot);
		// create svg
		String command;
		if (OS.getCurrentOS() == OS.WINDOWS) {
			command = DOT_INSTALLATION + " \"" + dot.getAbsolutePath() +
					"\" -Tsvg -o \"" + svg.getAbsolutePath() + "\"";
		}
		else {
			command = DOT_INSTALLATION + " " + dot.getAbsolutePath() +
					" -Tsvg -o " + svg.getAbsolutePath() + "";
		}
		createFileOutOfDot(svg, dot, command);
	}

	/**
	 * 
	 * 
	 * @created 11.11.2012
	 * @param dot
	 */
	private void writeDot(File dot) {
		try {
			checkWriteable(dot);
			FileWriter writer;
			writer = new FileWriter(dot);
			writer.append(dotSource);
			writer.flush();
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if the specified file and the required folder structure can be
	 * written. This prevents failures later on when the pdf will be created. If
	 * the file cannot be written an {@link IOException} is thrown.
	 * 
	 * @created 20.04.2011
	 * @param file the file to be written
	 * @throws IOException if the file cannot be written
	 */
	private static void checkWriteable(File file) throws IOException {
		// create/check target output folder
		File parent = file.getAbsoluteFile().getParentFile();
		parent.mkdirs();
		if (!parent.exists()) {
			throw new IOException(
					"failed to create non-existing parent folder: " + parent.getCanonicalPath());
		}
		// if there is already a file that cannot be overwritten,
		// throw an exception
		if (file.exists() && !file.canWrite()) {
			throw new IOException(
					"output file cannot be written: " + file.getCanonicalPath());
		}

		if (!file.exists()) {
			try {
				file.createNewFile();
			}
			catch (IOException io) {
				throw new IOException(
						"output file could not be created: " + file.getCanonicalPath());
			}
		}
	}

	/**
	 * 
	 * 
	 * @created 11.11.2012
	 * @param type
	 * @param path
	 * @return
	 */
	private File createFile(String type, String path) {
		String filename = path + "RootType" + "." + type;
		File f = new File(filename);
		return f;
	}

	/**
	 * 
	 * 
	 * @created 11.11.2012
	 * @param file
	 * @param dot
	 * @param command
	 * @throws IOException
	 */
	private void createFileOutOfDot(File file, File dot, String command) throws IOException {
		checkWriteable(file);
		checkReadable(dot);
		try {
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
			int exitValue = process.exitValue();
			if (exitValue != 0) {
				printStream(process.getErrorStream());
				throw new IOException("Command could not successfully be executed: " + command);
			}

		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void printStream(InputStream str) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(str));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			System.out.println(inputLine);
		in.close();
	}

	/**
	 * Checks if the specified file can be read. If not an {@link IOException}
	 * is thrown.
	 * 
	 * @created 20.04.2011
	 * @param file the file to read from
	 * @throws IOException if the file cannot be read
	 */
	private static void checkReadable(File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException(
					"file does not exist: " + file.getCanonicalPath());
		}
		if (!file.canRead()) {
			throw new IOException(
					"file cannot be read:" + file.getCanonicalPath());
		}
	}
}
