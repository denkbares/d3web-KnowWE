package de.knowwe.fingerprint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.d3web.core.io.PersistenceManager;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.SessionFactory;
import de.d3web.utils.Streams;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

public class KBScanner implements Scanner {

	@Override
	public void scan(Article article, File target) throws IOException {
		// check if this article compiles a base
		Section<DefaultMarkupPackageCompileType> kbType = Sections.successor(
				article.getRootSection(), DefaultMarkupPackageCompileType.class);
		if (kbType == null) return;

		// write the base
		// create Session to trigger lazy stuff like "start" and "now";
		KnowledgeBase base = D3webUtils.getKnowledgeBase(kbType);
		SessionFactory.createSession(base);
		PersistenceManager persistance = PersistenceManager.getInstance();
		persistance.save(base, target);
	}

	@Override
	public String getExtension() {
		return ".d3web";
	}

	@Override
	public Diff compare(File file1, File file2) throws IOException {
		Diff result = new Diff();

		ZipFile zip1 = new ZipFile(file1);
		ZipFile zip2 = new ZipFile(file2);

		Map<String, Long> expectedCrcMap = getCrcMap(zip1, result);
		Map<String, Long> actualCrcMap = getCrcMap(zip2, result);

		String fileName = zip1.getName();
		if (fileName.contains("/")) {
			fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		}
		// we compare the hash-maps manually
		// to provide detailed output of what file(s) differs
		for (String name : expectedCrcMap.keySet()) {
			long expectedCRC = expectedCrcMap.get(name);
			Long actualCRC = actualCrcMap.get(name);
			if (actualCRC == null) {
				result.fail("zip entry " + fileName + " missing in file " + file2);
				continue;
			}
			if (expectedCRC == actualCRC) continue;
			if (name.toLowerCase().endsWith(".xml")) {
				checkEqual(zip1, zip2, name, result);
			}
			else {
				result.fail("zip entry " + name + " has changed CRC codes");
			}
		}
		return result;
	}

	private String getContent(ZipFile zip, String entryName, String skipTo) throws IOException, UnsupportedEncodingException {
		InputStream in = zip.getInputStream(zip.getEntry(entryName));
		String text = new String(Streams.getBytesAndClose(in), "UTF-8");
		if (skipTo != null) {
			int index = text.indexOf(skipTo);
			if (index >= 0) text = text.substring(index + skipTo.length());
		}
		return text;
	}

	/**
	 * Returns a map of crc codes by pathname for each (relevant) file of a
	 * knowledge base zip file.
	 * 
	 * @created 16.04.2012
	 * @param zip the knowledge base file
	 * @return the map of relevant crc codes
	 */
	private Map<String, Long> getCrcMap(ZipFile zip, Diff result) {
		Map<String, Long> crcMap = new HashMap<>();
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			// ignore not required files
			if (notNeeded(entry)) continue;
			String name = entry.getName();
			long crc = entry.getCrc();
			if (crc == -1) {
				result.fail("cannot process zip files with unknown crc code");
			}
			crcMap.put(name, crc);
		}
		return crcMap;
	}

	private void checkEqual(ZipFile expectedZip, ZipFile actualZip, String entryName, Diff diff) throws IOException {
		LineFilter filter = new SkipRegexLinesFilter(
				"<\\?xml .*\\?>",
				"</?infoStore>",
				"<entry property=\"created\">",
				"</entry>",
				"<Date>.*</Date>");

		String skipTo = null;
		if (entryName.equals("kb/basic.xml")) skipTo = "<InitQuestions>";
		// if (entryName.equals("kb/settings.xml")) skipTo = "</plugins>";

		// ignore xml declaration line if xml and nothing is skipped
		if (skipTo == null && entryName.endsWith(".xml")) skipTo = "?>";

		// compare the rest of the files
		String expected = getContent(expectedZip, entryName, skipTo);
		String actual = getContent(actualZip, entryName, skipTo);
		expected = expected.replace("\r", "");
		actual = actual.replace("\r", "");
		if (!actual.equals(expected)) {
			diff.message("zip entry " + entryName + " has changed contents:");
			Fingerprint.compareText(expected, actual, filter, diff);
		}
	}

	/**
	 * Returns the the specified entry is a not required file.
	 * 
	 * @created 18.06.2011
	 * @param entry the entry to be checked
	 * @return if the entry should not be checked
	 */
	private boolean notNeeded(ZipEntry entry) {
		if (entry.isDirectory()) return true;
		String name = entry.getName();
		return name.equalsIgnoreCase("KB-INF/Index.xml")
				|| name.equalsIgnoreCase("CRS-INF/Index.xml")
				|| name.equals("META-INF/MANIFEST.MF")
				|| name.startsWith(".")
				|| name.equals("kb/settings.xml");
	}

	@Override
	public String getItemName() {
		return "Knowlegde Base";
	}

}
