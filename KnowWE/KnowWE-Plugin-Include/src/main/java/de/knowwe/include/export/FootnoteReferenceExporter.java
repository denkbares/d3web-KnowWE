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
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;

import de.d3web.utils.Pair;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.jspwiki.types.FootnoteType;
import de.knowwe.jspwiki.types.LinkType;

/**
 * Exporter for exporting inner-wiki links that are pointing to a footnote of
 * that page.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class FootnoteReferenceExporter implements Exporter<LinkType> {

	private final Set<Pair<Article, String>> exportedIDs = new HashSet<Pair<Article, String>>();

	@Override
	public boolean canExport(Section<LinkType> section) {
		return LinkType.isFootnote(section);
	}

	@Override
	public Class<LinkType> getSectionType() {
		return LinkType.class;
	}

	/**
	 * Returns if the footnote can be exported as a real reference. We do not
	 * export as a reference in on of the following cases:
	 * <ol>
	 * <li>the footnote is used more than once and already be exported (word or
	 * poi does not support this, leads to corrupt document)
	 * <li>the footnote definition is not found on that page (also lead to
	 * corrupt document otherwise)
	 * </ol>
	 * 
	 * @created 19.02.2014
	 * @param article the current article
	 * @param refNumber the wiki footnote number
	 * @return
	 */
	private boolean canExportAsReference(Article article, String refNumber) {
		// check if this is the first occurrence
		Pair<Article, String> key = new Pair<Article, String>(article, refNumber.trim());
		boolean isNew = exportedIDs.add(key);
		if (!isNew) return false;

		// check for footnote section
		for (Section<FootnoteType> footnote : Sections.successors(article, FootnoteType.class)) {
			String id = footnote.get().getFootnoteID(footnote);
			if (refNumber.equals(id)) return true;
		}
		// otherwise do not export
		return false;
	}

	@Override
	public void export(Section<LinkType> section, DocumentBuilder manager) throws ExportException {
		// find id for our footnote
		String refNumber = LinkType.getLink(section);
		BigInteger noteId = FootnoteExporter.getFootnoteID(manager, section.getArticle(), refNumber);

		// add reference to text
		XWPFRun run = manager.getParagraph().createRun();
		run.setSubscript(VerticalAlign.SUPERSCRIPT);
		if (canExportAsReference(section.getArticle(), refNumber)) {
			// add reference to footnote
			CTFtnEdnRef textRef = run.getCTR().addNewFootnoteReference();
			textRef.setId(noteId);
		}
		else {
			// otherwise only add text in similar style
			run.setText(refNumber);
		}
	}

}
