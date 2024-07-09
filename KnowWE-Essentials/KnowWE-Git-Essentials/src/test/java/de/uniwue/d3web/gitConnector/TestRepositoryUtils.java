package de.uniwue.d3web.gitConnector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.denkbares.utils.Files;
import de.uniwue.d3web.gitConnector.impl.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.JGitConnector;

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

		Calendar calendar = Calendar.getInstance();

		// Set the calendar to January 1, 2023
		calendar.set(2000, Calendar.MAY, 9, 10, 26, 10);
		calendar.set(Calendar.MILLISECOND, 0);

		// Get the Date object representing January 1, 2023
		Date date = calendar.getTime();

		for (File f : new File(wikiPath).listFiles()) {
			List<String> strings = connector.commitHashesForFileSince(f.getName(), date);
			System.out.println("Result: since: " + strings.size());

			List<String> strings2 = connector.commitHashesForFile(f.getName());
			System.out.println("Result: " + strings2.size());
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
