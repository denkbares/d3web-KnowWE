package de.knowwe.fingerprint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.d3web.strings.Strings;
import de.d3web.utils.Files;
import de.d3web.utils.Pair;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.version.taghandler.VersionTagHandler;

public class Fingerprint {

	public static final LineFilter ALL_LINES = new AllLinesFilter();

	private static final String FINGERPRINT = "_fingerprint.info";
	private static final Scanner[] scanners = new Scanner[] {
			new KDOMScanner(),
			new MessageScanner(),
			new KBScanner(),
			new TestCaseScanner(),
			new DashboadScanner()
	};

	public static void createFingerprint(String web, File folder) throws IOException {
		ArticleManager manager = Environment.getInstance().getArticleManager(web);
		Collection<Article> articles = manager.getArticles();
		createFingerprint(articles, folder);
	}

	public static void createFingerprint(Collection<Article> articles, File folder) throws FileNotFoundException, IOException {
		// create metadata file
		folder.mkdirs();
		try (PrintStream out = new PrintStream(new File(folder, FINGERPRINT))) {
			String baseUrl = Environment.getInstance().getWikiConnector().getBaseUrl();
			out.printf("Base URL: %s\n", baseUrl);
			out.printf("Created:  %tc\n", new Date());
			if (VersionTagHandler.hasVersionInfo()) {
				out.printf("KnowWE Version: %s - %s (%s)\n",
						VersionTagHandler.getBuildNumber(),
						VersionTagHandler.getBuildTag(),
						VersionTagHandler.getBuildTime());
			}
			else {
				out.printf("KnowWE Version: --\n");
			}
		}

		// create scanner files
		for (Article article : articles) {
			for (Scanner scanner : scanners) {
				File target = getFile(folder, article, scanner.getExtension());
				scanner.scan(article, target);
			}
		}
	}

	public static String createDiffArticle(File expected, File actual) throws IOException {
		StringBuilder page = new StringBuilder();

		// append compare header
		page.append("!!!Original finger-print: \n");
		page.append(escape(Files.getText(new File(expected, FINGERPRINT))));
		page.append("\n\n");
		page.append("!!!Actual finger-print: \n");
		page.append(escape(Files.getText(new File(actual, FINGERPRINT))));
		page.append("\n\n");

		// compare particular article files
		page.append("!!!Compared article results: \n");
		Map<String, File> expectedFiles = getFileMap(expected);
		Map<String, File> actualFiles = getFileMap(actual);
		Collection<String> names = new HashSet<>();
		names.addAll(expectedFiles.keySet());
		names.addAll(actualFiles.keySet());
		names.remove(FINGERPRINT);
		List<String> sorted = new LinkedList<>(names);
		Collections.sort(sorted);
		Collection<String> articleNames = new LinkedHashSet<>();
		Map<Pair<String, Scanner>, Diff> results = new LinkedHashMap<>();
		for (String name : sorted) {
			File expectedFile = expectedFiles.get(name);
			File actualFile = actualFiles.get(name);
			Scanner scanner = findScanner(name);
			String articleName = Strings.decodeURL(name.substring(0, name.length()
					- scanner.getExtension().length()));
			Diff result;
			if (expectedFile == null) {
				result = new Diff();
				result.fail("original file is missing");
			}
			else if (actualFile == null) {
				result = new Diff();
				result.fail("actual file is missing");
			}
			else {
				result = scanner.compare(expectedFile, actualFile);
			}
			articleNames.add(articleName);
			results.put(new Pair<>(articleName, scanner), result);
		}

		// print result table
		// header
		page.append("|| Article");
		for (Scanner scanner : scanners) {
			String name = scanner.getItemName();
			name = name.replaceAll("(\\w[\\s+-=?,.;:#'/\\\\\\[\\]\"§$%&]+)(\\w)", "$1\\\\\\\\$2");
			page.append(" || ").append(name);
		}
		page.append("\n");
		// and one line for each article
		List<Diff> fails = new ArrayList<>(articleNames.size());
		for (String articleName : articleNames) {
			page.append("| ").append(escape(articleName));
			for (Scanner scanner : scanners) {
				Diff diff = results.get(new Pair<>(articleName, scanner));
				page.append(" | ");
				if (diff == null) { // NOSONAR
					// do nothing
				}
				else if (diff.isEqual()) {
					page.append("%%(color:green)√%%");
				}
				else {
					fails.add(diff);
					page.append("%%(color:red)x%% [").append(fails.size()).append("]");
				}
			}
			page.append("\n");
		}
		page.append("\n");

		// print list of deviations
		int index = 1;
		for (Diff fail : fails) {
			page.append("[#").append(index++).append("]");
			if (fail.getMessages().size() > 1) page.append("\\\\");
			page.append(escape(Strings.concat("\n", fail.getMessages())));
			page.append("\n\n");
		}

		return page.toString();
	}

	private static String escape(String text) {
		return text.replace("|", "~|").replace("[", "~[").replace("\n", "\\\\");
	}

	private static Scanner findScanner(String filename) {
		for (Scanner scanner : scanners) {
			if (filename.endsWith(scanner.getExtension())) {
				return scanner;
			}
		}
		return null;
	}

	private static Map<String, File> getFileMap(File folder) {
		Map<String, File> result = new HashMap<>();
		for (File file : folder.listFiles()) {
			result.put(file.getName(), file);
		}
		return result;
	}

	private static File getFile(File folder, Article article, String extension) {
		String filename = article.getTitle();
		filename = Strings.encodeURL(filename);
		filename = filename.replace("/", "%2F");
		filename = filename.replace("\\", "%5C");
		filename = filename.replace(":", "%3A");
		if (filename.startsWith(".")) {
			filename = "%2E" + filename.substring(1);
		}
		return new File(folder, filename + extension);
	}

	static Diff compareTextFiles(File file1, File file2) throws IOException {
		return compareTextFiles(file1, file2, ALL_LINES);
	}

	static Diff compareTextFiles(File file1, File file2, LineFilter filter) throws IOException {
		Diff result = new Diff();
		String text1 = Files.getText(file1);
		String text2 = Files.getText(file2);

		compareText(text1, text2, filter, result);
		return result;
	}

	static void compareText(String text1, String text2, Diff result) {
		compareText(text1, text2, ALL_LINES, result);
	}

	static void compareText(String text1, String text2, LineFilter filter, Diff result) {
		String[] lines1 = text1.split("\\r?\\n");
		String[] lines2 = text2.split("\\r?\\n");

		// number of lines of each file
		int count1 = lines1.length;
		int count2 = lines2.length;

		if (count1 > 15000 || count2 > 15000) {
			result.fail("Files to big to make detailed diff. Please compare manually.\n");
			return;
		}

		// opt[i][j] = length of LCS of x[i..M] and y[j..N]
		int[][] opt = new int[count1 + 1][count2 + 1];

		// compute length of LCS and all subproblems via dynamic programming
		for (int i = count1 - 1; i >= 0; i--) {
			for (int j = count2 - 1; j >= 0; j--) {
				if (lines1[i].equals(lines2[j])) opt[i][j] = opt[i + 1][j + 1] + 1;
				else opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
			}
		}

		// recover LCS itself and print out non-matching lines to standard
		// output
		int i = 0, j = 0;
		while (i < count1 && j < count2) {
			if (lines1[i].equals(lines2[j])) {
				i++;
				j++;
			}
			else if (opt[i + 1][j] >= opt[i][j + 1]) {
				String line = lines1[i++];
				if (filter.accept(line)) result.fail("[-] " + line);
			}
			else {
				String line = lines2[j++];
				if (filter.accept(line)) result.fail("[+] " + line);
			}
		}

		// dump out one remainder of one string if the other is exhausted
		while (i < count1 || j < count2) {
			if (i == count1) {
				String line = lines2[j++];
				if (filter.accept(line)) result.fail("[+] " + line);
			}
			else if (j == count2) {
				String line = lines1[i++];
				if (filter.accept(line)) result.fail("[-] " + line);
			}
		}
	}

}
