/*
 * Copyright (C) 2010 denkbares GmbH, Würzburg, Germany
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
package de.knowwe.tagging.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

/**
 * A simple converter to replace the old <tags>...</tags> markup of KnowWE with
 * the new %%tags ... % markup.
 * 
 * @author Joachim Baumeister (denkbares GmbH)
 */
public class TagMarkupConverter {

	public static void main(String[] args) throws IOException {
		if (args != null && args.length > 0 && args[0] != null) {
			new TagMarkupConverter().run(new File(args[0]));
		}
		else {
			System.out.println("Please specify folder.");
		}
	}

	private void run(File dir) throws IOException {
		for (File file : getAllFilesWith(dir, ".txt")) {
			String content = readFileContent(file);
			if (containsTags(content)) {
				System.out.println("[Convert] " + file.getName());
				convert(file, content);
			}
		}
	}

	private void convert(File file, String content) throws IOException {
		String regex = "(.*)\\<tags\\>(.*)\\<\\/tags\\>(.*)";
		String newReg = "$1%%tags\n $2 \n%\n $3";
		content = content.replaceAll(regex, newReg);
		writeToFile(file, content);
	}

	private void writeToFile(File file, String content) throws IOException {
		Writer out = new BufferedWriter(new FileWriter(file));
		out.write(content);
		out.close();
	}

	private boolean containsTags(String content) {
		String tagPattern = "<tags>";
		if (content.contains(tagPattern)) return true;
		else return false;
	}

	private String readFileContent(File file) throws IOException {
		StringBuffer buffy = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file),
				"UTF8"));
		String str;
		while ((str = in.readLine()) != null) {
			buffy.append(str + "\n");
		}
		in.close();
		return buffy.toString();
	}

	private File[] getAllFilesWith(File dir, final String fileEnding) {
		FileFilter fileFilter = new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(fileEnding);
			}
		};
		return dir.listFiles(fileFilter);
	}

}
