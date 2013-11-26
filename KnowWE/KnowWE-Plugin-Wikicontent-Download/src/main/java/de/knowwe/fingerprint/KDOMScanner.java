package de.knowwe.fingerprint;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;

public class KDOMScanner implements Scanner {

	@Override
	public void scan(Article article, File target) throws IOException {
		PrintStream printStream = new PrintStream(target);
		try {
			printKDOM(article.getRootSection(), printStream);
		}
		finally {
			printStream.close();
		}
	}

	private void printKDOM(Section<?> section, PrintStream out) {
		int start = section.getOffsetInArticle();
		String name = section.get().getName();
		out.printf("<%s start='%d'>\n", name, start);
		for (Section<?> child : section.getChildren()) {
			printKDOM(child, out);
		}
		out.printf("</%s>\n", name);
	}

	@Override
	public String getExtension() {
		return ".kdom";
	}

	@Override
	public Diff compare(File file1, File file2) throws IOException {
		return Fingerprint.compareTextFiles(file1, file2);
	}

	@Override
	public String getItemName() {
		return "Parser";
	}

}
