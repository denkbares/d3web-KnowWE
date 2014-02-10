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

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import de.knowwe.core.kdom.parsing.Section;

/**
 * Manages the export of the included wiki pages into a specific export
 * artifact.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class ExportManager {

	private List<Exporter<?>> exporters = new LinkedList<Exporter<?>>();

	public ExportManager() {
		// add exporters
		exporters.add(new BoldExporter());
		exporters.add(new ItalicExporter());
		exporters.add(new WikiTextExporter());
		exporters.add(new PlainTextExporter());
		exporters.add(new HeaderExporter());
		exporters.add(new IncludeExporter());
		exporters.add(new ParagraphExporter());
		exporters.add(new ListExporter());
		exporters.add(new TableExporter());
		exporters.add(new ImageExporter());

		// default exporter
		exporters.add(new DefaultMarkupExporter());
	}

	public List<Exporter<?>> getExporters() {
		return Collections.unmodifiableList(exporters);
	}

	public XWPFDocument createDocument(Section<?> section) throws IOException {
		DefaultBuilder builder = new DefaultBuilder(this);
		builder.export(section);
		return builder.getDocument();
	}

}
