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
import org.apache.poi.xwpf.usermodel.XWPFFootnote;
import org.apache.poi.xwpf.usermodel.XWPFFootnotes;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFtnEdn;

import de.d3web.utils.Pair;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.include.export.DocumentBuilder.Style;
import de.knowwe.jspwiki.types.FootnoteType;

/**
 * Exporter for exporting links. They will be exported in the same style as the surrounding text.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class FootnoteExporter implements Exporter<FootnoteType> {

	private int lastFootnoteID = 0;
	private final Map<Pair<Article, String>, BigInteger> footnoteIDs =
			new HashMap<>();

	@Override
	public boolean canExport(Section<FootnoteType> section) {
		return true;
	}

	@Override
	public Class<FootnoteType> getSectionType() {
		return FootnoteType.class;
	}

	public static BigInteger getFootnoteID(DocumentBuilder builder, Article article, String footnoteNumber) {
		FootnoteExporter foot = builder.getModel().getExporter(FootnoteExporter.class);
		return foot.getFootnoteIDInternal(builder, article, footnoteNumber);
	}

	private BigInteger getFootnoteIDInternal(DocumentBuilder builder, Article article, String footnoteNumber) {
		Pair<Article, String> key = new Pair<>(article, footnoteNumber.trim());
		BigInteger id = footnoteIDs.get(key);
		if (id == null) {
			XWPFFootnotes footnotes = builder.getDocument().createFootnotes();
			// check until the footnote does not exists yet
			lastFootnoteID++;
			while (footnotes.getFootnoteById(lastFootnoteID) != null) {
				lastFootnoteID++;
			}
			id = BigInteger.valueOf(lastFootnoteID);
			footnoteIDs.put(key, id);
		}
		return id;
	}

	@Override
	public void export(Section<FootnoteType> section, DocumentBuilder manager) throws ExportException {
		// find id for our footnote
		String refNumber = section.get().getFootnoteID(section);
		BigInteger noteId = getFootnoteIDInternal(manager, section.getArticle(), refNumber);

		// create footnote for that id
		CTFtnEdn ctNote = CTFtnEdn.Factory.newInstance();
		ctNote.setId(noteId);
		ctNote.setType(STFtnEdn.NORMAL);
		XWPFFootnotes footnotes = manager.getDocument().createFootnotes();
		XWPFFootnote footnote = footnotes.addFootnote(ctNote);

		// add contents to footer
		XWPFParagraph footPara = footnote.addNewParagraph(CTP.Factory.newInstance());
		footPara.setStyle(Style.footnote.getStyleName());
		XWPFRun footRun = footPara.createRun();
		footRun.setSubscript(VerticalAlign.SUPERSCRIPT);
		footRun.setText(String.valueOf(noteId.intValue()));
		footRun = footPara.createRun();
		footRun.setSubscript(VerticalAlign.BASELINE);
		DocumentBuilder contentBuilder = new ListBuilder(manager, footPara, Style.text);
		contentBuilder.export(section.getChildren());
	}

}
