/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.include.export;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;

import com.denkbares.utils.Pair;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;
import de.knowwe.jspwiki.types.FootnoteType;
import de.knowwe.jspwiki.types.LinkType;

/**
 * Exporter for exporting inner-wiki links that are pointing to a footnote of that page.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class FootnoteReferenceExporter implements Exporter<LinkType> {

	private final Map<Pair<Article, String>, String> exportedIDs =
			new HashMap<>();

	@Override
	public boolean canExport(Section<LinkType> section) {
		return LinkType.isFootnote(section);
	}

	@Override
	public Class<LinkType> getSectionType() {
		return LinkType.class;
	}

	/**
	 * Checks if the footnote can be exported as a real reference and returns null in this case. Otherwise it returns a
	 * string that shall be used as the referencing String. This method also reports some warnings to the export if
	 * there are any. We do not export as a reference in one of the following cases: <ol> <li>the footnote is used more
	 * than once and already be exported (word or poi does not support this, leads to corrupt document) <li>the footnote
	 * definition is not found on that page (also lead to corrupt document otherwise) </ol>
	 *
	 * @param manager the export manager of this export
	 * @param article the current article
	 * @param refNumber the wiki footnote number
	 * @return null if the footnote-reference can be exported as real reference, a synthetic label otherwise
	 * @created 19.02.2014
	 */
	private String getForcedLabel(DocumentBuilder manager, Article article, String refNumber) {
		// check if this is the first occurrence
		Pair<Article, String> key = new Pair<>(article, refNumber);
		String exportedID = exportedIDs.get(key);
		if (exportedID != null) return exportedID;

		// check for footnote section
		for (Section<FootnoteType> footnote : Sections.successors(article, FootnoteType.class)) {
			String id = footnote.get().getFootnoteID(footnote);
			// if footnote matches, return if the footnote
			// is included in the export (if not, footnote will go wrong)
			if (refNumber.equals(id)) {
				boolean contained = manager.getModel().getManager().isContained(footnote);
				if (!contained) {
					manager.getModel().addMessage(Messages.warning("" +
							"The footnote #" + refNumber + " " +
							"in article '" + article.getTitle() + "' " +
							"is referenced, but not included in the export."));
					return "";
				}
				return null;
			}
		}
		// otherwise do not export
		manager.getModel().addMessage(Messages.warning("" +
				"The footnote #" + refNumber + " " +
				"referenced in article '" + article.getTitle() + "' " +
				"is not defined."));
		return "";
	}

	private void addForcedLabel(Article article, String refNumber, String label) {
		Pair<Article, String> key = new Pair<>(article, refNumber);
		exportedIDs.put(key, label);
	}

	@Override
	public void export(Section<LinkType> section, DocumentBuilder manager) throws ExportException {
		// find id for our footnote
		String refNumber = LinkType.getLink(section);

		// add reference to text
		XWPFRun run = manager.getParagraph().createRun();
		run.setSubscript(VerticalAlign.SUPERSCRIPT);
		Article article = section.getArticle();
		String label = getForcedLabel(manager, article, refNumber);
		if (label == null) {
			// add reference to footnote
			CTFtnEdnRef textRef = run.getCTR().addNewFootnoteReference();
			BigInteger noteId = FootnoteExporter.getFootnoteID(manager, article, refNumber);
			textRef.setId(noteId);
			addForcedLabel(article, refNumber, String.valueOf(noteId));
		}
		else {
			// otherwise only add text in similar style
			run.setText(label);
			addForcedLabel(article, refNumber, label);
		}
	}

}
