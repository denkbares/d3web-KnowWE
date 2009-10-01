package de.d3web.we.hermes;

import java.io.File;

import com.ecyrd.jspwiki.TextUtil;

import de.d3web.we.hermes.timeline.TimelineDatabase;
import de.d3web.we.javaEnv.KnowWEEnvironment;
import de.d3web.we.javaEnv.KnowWETopicLoader;

public class CompleteParser {
	/**
	 * Reparses web. Reads all .txt pages in the source folder of the
	 * topicLoader and processes the Article.
	 */
	public static String reparseWeb() {
		TimelineDatabase.getInstance().clear();

		KnowWETopicLoader loader = KnowWEEnvironment.getInstance().getTopicLoader();

		File file = new File(loader.getFilePath());
		File[] allFiles = file.listFiles();
		String errorsInFiles = "";
		for (File f : allFiles) {
			try {
			if (f.getName().endsWith(".txt")) {
				String header = f.getName().substring(0, f.getName().length() - 4);
				String content = loader.loadTopic(KnowWEEnvironment.DEFAULT_WEB, header);
				KnowWEEnvironment.getInstance().processAndUpdateArticle(content, TextUtil.urlDecodeUTF8(header),
						KnowWEEnvironment.DEFAULT_WEB);
			}
			} catch (Exception e) {
				e.printStackTrace();
				errorsInFiles += "\t" + f.getName() + "\n";
			}
		}
		
		if (errorsInFiles.isEmpty()) {
			return "Parsing Successful.";
		} else {
			return "Errors while parseing following file(s):\n" +  errorsInFiles;
		}
	}
}
