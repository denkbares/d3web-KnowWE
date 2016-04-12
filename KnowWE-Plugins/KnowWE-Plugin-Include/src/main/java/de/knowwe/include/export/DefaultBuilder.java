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

import java.util.Collection;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;

/**
 * @author Volker Belli (denkbares GmbH)
 * @created 09.02.2014
 */
public class DefaultBuilder implements DocumentBuilder {

	private final ExportModel model;
	protected XWPFParagraph paragraph = null;

	private boolean code;
	private boolean bold;
	private boolean italic;
	private boolean suppressHeaderNumbering = false;
	private int level = 0;

	/**
	 * Creates a new default builder for the default template.
	 */
	public DefaultBuilder(ExportModel model) {
		this.model = model;
	}

	@Override
	public ExportModel getModel() {
		return model;
	}

	@Override
	public void setBold(boolean bold) {
		this.bold = bold;
	}

	@Override
	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	@Override
	public void setCode(boolean code) {
		this.code = code;
	}

	@Override
	public void setSuppressHeaderNumbering(boolean suppressHeaderNumbering) {
		this.suppressHeaderNumbering = suppressHeaderNumbering;
	}

	@Override
	public boolean isSuppressHeaderNumbering() {
		return suppressHeaderNumbering;
	}

	@Override
	public void incHeaderLevel(int delta) {
		level += delta;
	}

	protected XWPFParagraph createParagraph() {
		return getDocument().createParagraph();
	}

	@Override
	public XWPFDocument getDocument() {
		return model.getDocument();
	}

	@Override
	public void export(Collection<Section<? extends Type>> sections) throws ExportException {
		for (Section<?> section : sections) {
			export(section);
		}
	}

	@Override
	public void export(Section<?> section) throws ExportException {
		// try to export section
		for (Exporter<?> export : model.getExporters()) {
			try {
				if (exportSection(section, export)) {
					model.notifyExported(section);
					return;
				}
			}
			catch (ExportException e) {
				Log.warning("word export reported an error", e);
				model.addMessage(Messages.error(e));
			}
		}

		// if not, export all child sections
		export(section.getChildren());
		model.notifyExported(section);
	}

	private <T extends Type> boolean exportSection(Section<?> section, Exporter<T> export) throws ExportException {
		if (!export.getSectionType().isInstance(section.get())) return false;
		Section<T> castedSection = Sections.cast(section, export.getSectionType());
		if (!export.canExport(castedSection)) return false;
		export.export(castedSection, this);
		return true;
	}

	@Override
	public void closeParagraph() {
		paragraph = null;
	}

	@Override
	public XWPFParagraph getParagraph() {
		if (paragraph == null) {
			paragraph = createParagraph();
			applyStyle(getDefaultStyle());
		}
		return paragraph;
	}

	protected Style getDefaultStyle() {
		return Style.text;
	}

	protected Style mapStyle(Style style) {
		int l = style.getHeadingLevel();
		return (l == 0) ? style : Style.heading(l + level);
	}

	@Override
	public XWPFParagraph getParagraph(Style style) {
		return hasStyle(style) ? paragraph : getNewParagraph(style);
	}

	@Override
	public XWPFParagraph getNewParagraph() {
		closeParagraph();
		return getParagraph();
	}

	@Override
	public XWPFParagraph getNewParagraph(Style style) {
		getNewParagraph();
		applyStyle(style);
		return paragraph;
	}

	private boolean hasStyle(Style style) {
		String currentStyleName = paragraph == null ? null : paragraph.getStyle();
		String otherStyleName = mapStyle(style).getStyleName();
		return Strings.equalsIgnoreCase(currentStyleName, otherStyleName);
	}

	protected void applyStyle(Style style) {
		paragraph.setStyle(mapStyle(style).getStyleName());
		if (suppressHeaderNumbering) {
			paragraph.setNumID(null);
		}
	}

	@Override
	public XWPFRun append(String text) {
		XWPFRun run = getParagraph().createRun();
		return append(text, run);
	}

	@Override
	public XWPFRun append(Style style, String text) {
		XWPFRun run = getParagraph(style).createRun();
		return append(text, run);
	}

	@Override
	public void appendLineBreak() {
		getParagraph().createRun().addCarriageReturn();
	}

	private XWPFRun append(String text, XWPFRun run) {
		if (bold) run.setBold(true);
		if (italic) run.setItalic(true);
		if (code) run.setFontFamily("Courier New");
		run.setText(text);
		return run;
	}
}
