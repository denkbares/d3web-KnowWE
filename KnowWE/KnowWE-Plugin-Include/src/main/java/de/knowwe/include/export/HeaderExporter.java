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

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTMarkupRange;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;
import de.knowwe.include.export.DocumentBuilder.Style;
import de.knowwe.jspwiki.types.HeaderType;

/**
 * Exports header of the document for any level.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class HeaderExporter implements Exporter<HeaderType> {

	private long bookmarkIndex = 0;
	private int lastUsedHeadingLevel = 0;

	@Override
	public Class<HeaderType> getSectionType() {
		return HeaderType.class;
	}

	@Override
	public boolean canExport(Section<HeaderType> section) {
		return true;
	}

	@Override
	public void export(Section<HeaderType> section, DocumentBuilder builder) throws ExportException {
		String headerText = section.get().getHeaderText(section);
		int marks = section.get().getMarkerCount();
		exportHeading(headerText, 4 - marks, getCrossReferenceID(section),
				section.getArticle(), builder);
	}

	public static void export(String headerText, int headingLevel, String refID, Article article, DocumentBuilder builder) {
		HeaderExporter exporter = builder.getModel().getExporter(HeaderExporter.class);
		exporter.exportHeading(headerText, headingLevel, refID, article, builder);
	}

	private void exportHeading(String headerText, int headingLevel, String refID, Article article, DocumentBuilder builder) {
		Style style = Style.heading(headingLevel);
		builder.getNewParagraph(style);
		checkHeaderLevel(headerText, article, builder);
		createCrossReferenceRun(refID, builder).setText(headerText);
		builder.closeParagraph();
	}

	private void checkHeaderLevel(String headerText, Article article, DocumentBuilder builder) {
		// if we are using a header style, check that the
		// level increase does not produce a gap by
		// adding multiple levels at one
		Style style = Style.getByStyleName(builder.getParagraph().getStyle());
		int level = style.getHeadingLevel();
		if (level != 0) {
			// the level max decrease by any number,
			// but increasing at a max of one
			if (lastUsedHeadingLevel != 0 && level > lastUsedHeadingLevel + 1) {
				builder.getModel().addMessage(Messages.warning("" +
						"The heading '" + headerText + "' " +
						"of article '" + article.getTitle() + "' " +
						"increases multiple levels at once."));
			}
			lastUsedHeadingLevel = level;
		}
	}

	public static XWPFRun createCrossReferenceRun(String refID, DocumentBuilder builder) {
		XWPFParagraph paragraph = builder.getParagraph();
		if (refID == null) {
			return paragraph.createRun();
		}
		else {
			HeaderExporter self = builder.getModel().getExporter(HeaderExporter.class);
			BigInteger id = BigInteger.valueOf(self.bookmarkIndex++);
			CTP ctp = paragraph.getCTP();
			CTBookmark start = ctp.addNewBookmarkStart();
			start.setId(id);
			start.setName(refID);
			XWPFRun run = paragraph.createRun();
			CTMarkupRange end = ctp.addNewBookmarkEnd();
			end.setId(id);
			return run;
		}
	}

	public static String getCrossReferenceID(Section<?> headerOrRootSection) {
		if (headerOrRootSection == null) return null;
		return "_Ref" + Long.parseLong(headerOrRootSection.getID(), 16);
	}
}
