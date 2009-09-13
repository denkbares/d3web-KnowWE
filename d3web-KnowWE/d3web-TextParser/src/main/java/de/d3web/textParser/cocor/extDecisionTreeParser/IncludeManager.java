/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.textParser.cocor.extDecisionTreeParser;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.complexRule.Utils;

public class IncludeManager {

	private String originalText;

	private String expandedText;

	private Report report = new Report();

	private String path = "";

	private List<Include> includes = new ArrayList<Include>();

	public IncludeManager(String text, String path) {
		this.originalText = text;
		this.path = path;
	}

	/**
	 * 
	 * @return Returns the original text and replaces the includes with the
	 *         content of the included files.
	 */
	public String expand() {

		// read each line of the file into array list
		LineNumberReader lineReader = new LineNumberReader(new StringReader(
				originalText));
		ArrayList<String> lines = new ArrayList<String>();
		while (true) {
			String line = "line couldn't be red";
			try {
				line = lineReader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (line == null)
				break;
			lines.add(line);
		}

		HashMap<Integer, String> includeFiles = extractIncludes(lines, report);

		if (includeFiles.size() == 0) {
			return originalText;
		}

		// includes auslesen und Zeilen ersetzen
		for (Integer line : includeFiles.keySet()) {
			String src = includeFiles.get(line);
			String text = "text not read";
			URL url = null;
			try {
				url = new URL("file://" + path + src);

				File f = new File(url.getFile());

				if (!f.exists()) {
					report.add(new Message(Message.ERROR, "file not found: "
							+ src, null, 0, 0, null));
				} else {
					ArrayList<String> l = Utils.readFile(url);

					StringBuffer buffi = new StringBuffer();
					for (Iterator<String> iter = l.iterator(); iter.hasNext();) {
						String element = iter.next();
						buffi.append(element
								+ System.getProperty("line.separator"));

					}
					text = buffi.toString();
					includes.add(new Include(src, text, line));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			lines.set(line.intValue(), text);

		}

		// alles wieder zusammenfuegen
		StringBuffer resultBuffi = new StringBuffer();
		for (String line : lines) {
			resultBuffi.append(line + System.getProperty("line.separator"));
		}
		expandedText = resultBuffi.toString();
		return expandedText;
	}

	/***************************************************************************
	 * 
	 * 
	 * 
	 * @param lines
	 *            ArrayList<String> containing lines of source file
	 * @return HashMap<Integer, String> containing <LineNumber,
	 *         AbsolutePathOfIncludeFile>
	 */
	public static HashMap<Integer, String> extractIncludes(
			ArrayList<String> lines) {
		return extractIncludes(lines, null);
	}

	/**
	 * Extracts include file paths with line number from array list of file
	 * lines.
	 * 
	 * @param lines
	 *            Lines of input file.
	 * @param report
	 * @return HashMap containing for each include <LineNumber, FilePath>
	 */
	private static HashMap<Integer, String> extractIncludes(List<String> lines,
			Report report) {
		Pattern includePattern = Pattern.compile("<include src"
				+ "=\"[\\w\\W]*?\"" + ">");

		// <LineNumber, AbsolutePathOfIncludeFile>
		HashMap<Integer, String> includeFiles = new HashMap<Integer, String>();

		// alle includes suchen und merken
		for (int i = 0; i < lines.size(); i++) {
			Matcher ma = includePattern.matcher(lines.get(i));
			if (ma.find()) {

				String finding = ma.group();
				// extract include file name
				String src = finding.substring(14, finding.length() - 2);
				includeFiles.put(new Integer(i), src);
				if (report != null)
					report.add(new Message("Include found: Line: " + i
							+ " file: " + src));
			}
		}

		return includeFiles;
	}

	public List<Message> adaptErrors(List<Message> set) {

		Collections.sort(includes);
		for (Iterator iter = set.iterator(); iter.hasNext();) {
			Message element = (Message) iter.next();
			int line = element.getLineNo();
			int includedLinesSum = 0;
			boolean isDone = false;
			for (Iterator iterator = includes.iterator(); iterator.hasNext();) {
				Include include = (Include) iterator.next();
				int includeSize = include.getTextLineCount();
				int includeLine = include.getOriginalLineNumber();

				if (line < includedLinesSum + includeLine) {
					// ist vor dem include
					int newLineNo = line - includedLinesSum;
					element.setLineNo(newLineNo);
					isDone = true;
					break;
				}
				if (line < includedLinesSum + includeLine + includeSize) {
					// ist IN dem include
					int newLineNo = line - includedLinesSum - includeLine;
					element.setLineNo(newLineNo);
					element.setFilename(include.getSrc());
					element.setMessageText(element.getMessageText()
							+ " in include file:"
							+ include.getSrc().substring(
									include.getSrc().lastIndexOf("/") + 1));
					isDone = true;
					break;
				}

				includedLinesSum += includeSize;

			}
			if (!isDone) {
				// ist nach dem letzten include
				int newLineNo = line - includedLinesSum;
				element.setLineNo(newLineNo);
			}

		}
		return set;

	}

	public class Include implements Comparable {

		private String src;
		private String text;
		private int originalLineNumber;
		private int textLineCount;

		public Include(String src, String text, int number) {
			this.src = src;
			this.text = text;
			this.originalLineNumber = number;
			this.textLineCount = text.split(System
					.getProperty("line.separator")).length;
		}

		public int compareTo(Object o) {
			if (o instanceof Include) {
				if (((Include) o).originalLineNumber < originalLineNumber) {
					return 1;
				} else {
					return -1;
				}
			}
			return 0;
		}

		public int getOriginalLineNumber() {
			return originalLineNumber;
		}

		public String getSrc() {
			return src;
		}

		public String getText() {
			return text;
		}

		public int getTextLineCount() {
			return textLineCount;
		}

	}
}
