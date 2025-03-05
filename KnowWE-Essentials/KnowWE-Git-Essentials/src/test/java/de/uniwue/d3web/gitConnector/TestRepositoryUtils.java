package de.uniwue.d3web.gitConnector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.denkbares.utils.Files;
import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.jgit.JGitConnector;

public class TestRepositoryUtils {

	public static GitConnector createDummyRepositoryAt(String path) {

//		BareGitConnector gitConnector = BareGitConnector.fromPath(path);
		GitConnector gitConnector = JGitConnector.fromPath(path);

		for (int i = 1; i <= 250; i++) {
			String pageName = i + ".txt";
			Path pagePath = Paths.get(path, pageName);

			if (pagePath.toFile().exists()) {
				continue;
			}

			//create all version
			for (int version = 1; version <= i; version++) {
				try {
					Files.writeFile(pagePath.toFile(), "This is page " + pageName + " in version: " + version);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}

				gitConnector.changePath(pagePath, new UserData("Max Mustermann", "max@mustermann.com", "Commit file: " + pageName + " in version: " + version));
			}
		}

		return gitConnector;
	}

	public static void main(String[] args) {
		String wikiPath = "/Users/mkrug/Konap/Wiki_KM";
		BareGitConnector connector = BareGitConnector.fromPath(wikiPath);
		//GitConnector connector = JGitConnector.fromPath(wikiPath);




		for (File f : new File(wikiPath).listFiles()) {
			if(f.isDirectory() && !f.getName().startsWith(".")){
				for(File att : f.listFiles()){
					if(!att.getName().endsWith(".txt") && !att.getName().endsWith(".xml") && att.getName().endsWith(".jpg")){
						String path = att.getParentFile().getName()+"/"+att.getName();
						List<String> strings = connector.commitHashesForFile(path);
						System.out.println(att +" with revisions: " + strings.size());

						for(String hash : strings){
							byte[] bytesForCommit = connector.getBytesForCommit(hash, path);
							int a=2;
						}
					}

				}
			}
			else{
				if(!f.getName().endsWith(".txt")){
					System.out.println(f);
				}
			}

		}

//		String wikiPath = "/Users/mkrug/temp/konap_test/konap_huge-wiki";
//		GitConnector connector = TestRepositoryUtils.createDummyRepositoryAt(wikiPath);
////
//		for (int i = 1; i <= 250; i++) {
//			String pageName = i + ".txt";
//			List<String> strings = connector.commitHashesForFile(pageName);
//		}

	}
}
