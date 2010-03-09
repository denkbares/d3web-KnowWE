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

package de.d3web.KnOfficeParser.util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.core.io.PersistenceManager;
import de.d3web.core.knowledge.KnowledgeBase;

public class TestLoader {

	int problems = 0;
	int loaded = 0;

	private static final String allWebs = "allWebs";
	private static ResourceBundle rb = null;

	public static void main(String[] args) throws IOException {
		rb = ResourceBundle
		.getBundle("TestLoaderConfig");
		// String path =
		// "C:/KnowWE/workspace/KnowWE-Webapp/bin/WEB-INF/resources/webs";
		// String path =
		// "C:/Programme/xampp/tomcat/webapps/KWiki/WEB-INF/resources/webs";
		String path = rb.getString("path_to_web_kbs");
		String web = "allWebs";
		if (args.length > 0) {
			web = args[0];
		}
		TestLoader loader = new TestLoader();
		StringBuffer log = new StringBuffer();
		loader.loadKBs(web, log);
		Date date = new Date();
		String dateString = date.toString();
		dateString = dateString.replaceAll(":", "_");
		writeFile(rb.getString("path_to_logs") + "TestLoader-" + dateString
				+ ".log", log.toString() + "\n" + loader.loaded
				+ " KBs loaded"+"\n" + loader.problems
				+ " problems found");
	}

	private void loadKBs(String web, StringBuffer log) throws IOException {
		String path = rb.getString("path_to_web_kbs");
		try {
			URL url = new File(path).toURI().toURL();
		} catch (MalformedURLException e) {
			log.append("cannot open directory: " + path + "\n");
			problems++;
			e.printStackTrace();
		}
		File f = new File(path);
		if(!f.exists()) {
			log.append("cannot open directory: " + path + "\n");
			problems++;
			
		}
		if (web.equals(allWebs)) {
			File[] webFolders = f.listFiles();
			for (int i = 0; i < webFolders.length; i++) {
				loadAllKBs(webFolders[i], log);
			}
		} else {
			File webFile = new File(path+"/"+web);
			if(!webFile.exists()) {
				log.append("cannot open directory: " + webFile.toString() + "\n");
				problems++;
			}else {
				loadAllKBs(webFile,log);
			}
		}

	}

	public static Collection<KnowledgeBase> loadAllKBs(File file, StringBuffer log) throws IOException {
		PersistenceManager m = PersistenceManager.getInstance();
		List<KnowledgeBase> list = new ArrayList<KnowledgeBase>();
		File[] jars = file.listFiles(new JarFileFilter());
		for (int i = 0; i < jars.length; i++) {
			try {
				KnowledgeBase kb = m.load(jars[i]);
				String id = jars[i].toString();
				kb.setId(""+i);
				list.add(kb);
				//loaded++;
				log.append("loaded: " + jars[i].toString() + "\n");
			} catch (MalformedURLException e) {
				log.append("cannot load file: " + jars[i].toString() + "\n");
				//problems++;
				e.printStackTrace();
			}
		}
		return list;
	}

	public static void writeFile(String fileName, String fileText) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(fileText);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	static class JarFileFilter implements FileFilter{
		
		public boolean accept(File f) {
			return f.getName().endsWith(".jar");
		}
		
	}

}
