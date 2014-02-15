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
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.renderer.RenderKDOMType;
import de.knowwe.core.report.Message;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.include.IncludeMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Manages the export of the included wiki pages into a specific export
 * artifact.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class ExportManager {

	private List<Exporter<?>> exporters = new LinkedList<Exporter<?>>();
	private List<Message> messages = Collections.emptyList();

	public ExportManager() {
		// add exporters
		exporters.add(new WikiBookPropertyExporter());
		exporters.add(new TOCExporter());
		exporters.add(new BoldExporter());
		exporters.add(new ItalicExporter());
		exporters.add(new LinkExporter());
		exporters.add(new WikiTextExporter());
		exporters.add(new PlainTextExporter());
		exporters.add(new HeaderExporter());
		exporters.add(new IncludeExporter());
		exporters.add(new ParagraphExporter());
		exporters.add(new ListExporter());
		exporters.add(new TableExporter());
		exporters.add(new ImageExporter());

		// add special exporters
		exporters.add(new InlineDefinitionExporter());
		exporters.add(new InlineDefinitionExporter("RESP_?\\d*"));
		exporters.add(new DefinitionExporter());

		// some types to be skipped
		exporters.add(new HideExporter<RenderKDOMType>(RenderKDOMType.class));

		// default exporter
		exporters.add(new DefaultMarkupExporter());
	}

	public List<Exporter<?>> getExporters() {
		return Collections.unmodifiableList(exporters);
	}

	public XWPFDocument createDocument(Section<?> section) throws IOException {
		// detect stream for word template
		Section<AttachmentType> attach = Sections.successor(section, AttachmentType.class);
		InputStream stream = (attach == null)
				? createDefaultTemplateStream()
				: createAttachmentStream(attach);

		// create builder and export the section
		try {
			DefaultBuilder builder = new DefaultBuilder(this, stream);
			if (section.get() instanceof IncludeMarkup) {
				updateDocumentInfo(Sections.cast(section, IncludeMarkup.class), builder);
			}
			builder.export(section);
			messages = builder.getMessages();
			return builder.getDocument();
		}
		finally {
			stream.close();
		}
	}

	public List<Message> getMessages() {
		return messages;
	}

	public static InputStream createAttachmentStream(Section<AttachmentType> attachment) throws IOException {
		WikiAttachment attach = AttachmentType.getAttachment(attachment);
		if (attach == null) {
			throw new ExportException("Attachment '" + attachment.getText() + "' not found");
		}
		return attach.getInputStream();
	}

	public static InputStream createDefaultTemplateStream() {
		return ExportManager.class.getResourceAsStream("/de/knowwe/include/export/template.docx");
	}

	private void updateDocumentInfo(Section<IncludeMarkup> section, DefaultBuilder builder) {
		String project = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_PROJECT);
		if (!Strings.isBlank(project)) {
			builder.setProperty("project", project);
		}
		String title = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_TITLE);
		if (!Strings.isBlank(title)) {
			builder.setProperty("title", title);
		}
		String author = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_AUTHOR);
		if (!Strings.isBlank(author)) {
			builder.setProperty("author", author);
		}
	}
}
