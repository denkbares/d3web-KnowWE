package de.d3web.KnOfficeParser.util;
import java.io.BufferedReader;
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

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.persistence.xml.PersistenceManager;

public class TestLoader {

	int problems = 0;
	int loaded = 0;

	private static final String allWebs = "allWebs";
	private static ResourceBundle rb = null;

	public static void main(String[] args) {
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

	private void loadKBs(String web, StringBuffer log) {
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

	public static Collection<KnowledgeBase> loadAllKBs(File file, StringBuffer log) {
		PersistenceManager m = PersistenceManager.getInstance();
		List<KnowledgeBase> list = new ArrayList<KnowledgeBase>();
		File[] jars = file.listFiles(new JarFileFilter());
		for (int i = 0; i < jars.length; i++) {
			try {
				KnowledgeBase kb = m.load(jars[i].toURI().toURL());
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
