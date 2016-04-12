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

import org.apache.poi.xwpf.usermodel.XWPFHyperlinkRun;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.jspwiki.types.LinkType;

/**
 * Exporter for exporting links. They will be exported in the same style as the
 * surrounding text.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class LinkExporter implements Exporter<LinkType> {

	@Override
	public boolean canExport(Section<LinkType> section) {
		// export everything except footnote references
		return !LinkType.isFootnote(section);
	}

	@Override
	public Class<LinkType> getSectionType() {
		return LinkType.class;
	}

	/**
	 * Returns if the link can be exported as a cross reference to a specific
	 * section. If there is a section we shall export as link, the target
	 * section is returned. Otherwise null is returned. We do not export as a
	 * cross reference in one of the following cases:
	 * <ol>
	 * <li>the linked header or definition is not part of the export
	 * <li>the linked header or definition is not found at all
	 * </ol>
	 * 
	 * @created 19.02.2014
	 * @param manager the export manager of this export
	 * @param section the link section
	 * @return if the link can be exported as real reference
	 */
	private Section<?> canExportAsReference(ExportManager manager, Section<LinkType> section) {
		// find the target section to be linked
		Section<?> target = LinkType.getReferencedSection(section);
		if (target == null) return null;
		return manager.isContained(target) ? target : null;
	}

	@Override
	public void export(Section<LinkType> section, DocumentBuilder builder) throws ExportException {
		ExportUtils.addRequiredSpace(builder);
		Section<?> target = canExportAsReference(builder.getModel().getManager(), section);
		if (target != null) {
			String refID = HeaderExporter.getCrossReferenceID(target);

			// Variant 1:
			// Export as hyperlinks to anchor
			// --> Text of link is defined as by wiki user

			XWPFParagraph paragraph = builder.getParagraph();
			CTP ctp = paragraph.getCTP();
			CTHyperlink hyperlink = ctp.addNewHyperlink();
			hyperlink.setAnchor(refID);
			XWPFHyperlinkRun run = new XWPFHyperlinkRun(hyperlink, hyperlink.addNewR(), paragraph);
			run.setText(LinkType.getDisplayText(section));

			// Variant 2:
			// Export as word-field (REF ... \h)
			// --> More word-like,
			// but reference text is always updated to the header title

			// CTP p = builder.getParagraph().getCTP();
			// p.addNewPPr();
			// CTR run = p.addNewR();
			// run.addNewRPr().addNewNoProof();
			// run.addNewFldChar().setFldCharType(STFldCharType.BEGIN);
			// // pageref run
			// run = p.addNewR();
			// run.addNewRPr().addNewNoProof();
			// CTText text = run.addNewInstrText();
			// text.setSpace(Space.PRESERVE);
			// // bookmark reference
			// text.setStringValue(" REF " + refID + " \\h ");
			// p.addNewR().addNewRPr().addNewNoProof();
			// run = p.addNewR();
			// run.addNewRPr().addNewNoProof();
			// run.addNewFldChar().setFldCharType(STFldCharType.SEPARATE);
			// // page number run
			// run = p.addNewR();
			// run.addNewRPr().addNewNoProof();
			// run.addNewT().setStringValue(LinkType.getDisplayText(section));
			// run = p.addNewR();
			// run.addNewRPr().addNewNoProof();
			// run.addNewFldChar().setFldCharType(STFldCharType.END);
		}
		else {
			builder.append(LinkType.getDisplayText(section));
		}
	}
}
