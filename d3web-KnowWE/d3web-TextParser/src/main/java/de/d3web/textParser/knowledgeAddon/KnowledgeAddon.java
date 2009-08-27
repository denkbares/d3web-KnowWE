package de.d3web.textParser.knowledgeAddon;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.multimedia.MMExtensionsDataManager;
import de.d3web.kernel.supportknowledge.DCElement;
import de.d3web.kernel.supportknowledge.DCMarkup;
import de.d3web.report.Message;
import de.d3web.report.Report;

public class KnowledgeAddon {

	private KnowledgeBase kb;

	private Report report = new Report();

	public KnowledgeAddon(KnowledgeBase kb) {
		this.kb = kb;
	}

	/**
	 * Parst eine Datei in welcher der Name,der Autor der Wissensbasis und
	 * zugehoerige Multimedia-Dateien stehen. Diese werden zur Wissensbasis
	 * hinzugefuegt.
	 * 
	 * @param r
	 *            Reader
	 * @return Report mit eventuellen Fehlermeldungen.
	 */
	public Report parseFile(Reader r) {
		byte[] bytes = readBytes(r);
		String text = new String(bytes);
		Scanner scanner = new Scanner(text);
		ArrayList<File> files = new ArrayList<File>();
		String basePath = null;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.startsWith("author ") || line.startsWith("Author ")
					|| line.startsWith("author=") || line.startsWith("Author")) {
				setAuthor(line);
			} else if (line.startsWith("title ") || line.startsWith("Title ")
					|| line.startsWith("title=") || line.startsWith("Title=")) {
				setTitle(line);
			} else if (line.startsWith("BasePath ")
					|| line.startsWith("basepath ")
					|| line.startsWith("BasePath=")
					|| line.startsWith("basepath=")) {
				basePath = getValue(line);
			} else if (line.equals("")) {
				// do nothing
			} else {
				if (basePath != null) {
					File file = new File(basePath.concat(line));
					files.add(file);
				} else {
					// keine BasePath angegeben.
					report.add(new Message("basePath doesn't exist"));

				}
			}
		}
		this.setFiles(files);
		return report;
	}

	private String getValue(String line) {
		int start = line.indexOf("=");
		int end = line.length();
		String result = line.subSequence(start + 1, end).toString();
		result = result.trim();
		return result;
	}

	private void setFiles(ArrayList<File> files) {
		MMExtensionsDataManager datamanager = MMExtensionsDataManager
				.getInstance();
		datamanager.addFiles(files);
	}

	private void setAuthor(String line) {
		DCMarkup dc = kb.getDCMarkup();
		dc.setContent(DCElement.CREATOR, this.getValue(line));
		kb.setDCDMarkup(dc);
	}

	private void setTitle(String line) {
		DCMarkup dc = kb.getDCMarkup();
		dc.setContent(DCElement.TITLE, this.getValue(line));
		kb.setDCDMarkup(dc);
	}

	private static byte[] readBytes(Reader r) {
		int zeichen = 0;
		java.util.List bytes = new LinkedList();
		while (true) {

			try {
				zeichen = r.read();
			} catch (IOException e) {
				e.printStackTrace();
				// sollte nicht passieren!
			}
			if (zeichen == -1)
				break;
			Byte b = new Byte((byte) zeichen);
			bytes.add(b);
		}

		Object[] o = bytes.toArray();
		byte[] byteArray = new byte[o.length];

		for (int i = 0; i < o.length; i++) {
			byteArray[i] = ((Byte) o[i]).byteValue();

		}
		return byteArray;
	}

	/**
	 * Gibt die Wissensbasis zurueck.
	 * 
	 * @return KnowledgeBase
	 */
	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}
}
