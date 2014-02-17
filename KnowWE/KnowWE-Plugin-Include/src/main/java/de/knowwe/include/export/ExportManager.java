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
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.openxml4j.util.Nullable;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.renderer.RenderKDOMType;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.include.IncludeMarkup;
import de.knowwe.include.InnerWikiReference;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Manages the export of the included wiki pages into a specific export
 * artifact.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class ExportManager {

	private Section<?> section;
	private Set<Article> includedArticles = null;

	public ExportManager(Section<?> section) {
		this.section = section;
	}

	List<Exporter<?>> createExporters() {
		List<Exporter<?>> exporters = new LinkedList<Exporter<?>>();

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

		return Collections.unmodifiableList(exporters);
	}

	/**
	 * Returns the articles of the the specified section and all other sections
	 * included by the specified section or any sub-sections of the specified
	 * sections. If the specified section (or any of it's sub-section) include
	 * an other include the indirectly included sections of articles are also
	 * checked iteratively to any depth.
	 * 
	 * @created 16.02.2014
	 * @return all articles used by this export
	 */
	public Set<Article> getIncludedArticles() {
		if (includedArticles == null) {
			// for security reasons we have to check all visited
			// sections instead of only the articles
			// because there might be multiple different includes
			// in one single article
			Set<Section<?>> visited = new HashSet<Section<?>>();
			initIncludedArticles(section, visited);

			// but after that we take only the sections' articles
			includedArticles = Sections.getArticles(visited);
		}
		return Collections.unmodifiableSet(includedArticles);
	}

	private void initIncludedArticles(Section<?> section, Set<Section<?>> visited) {
		if (visited.add(section)) {
			List<Section<InnerWikiReference>> references =
					Sections.successors(section, InnerWikiReference.class);
			for (Section<InnerWikiReference> reference : references) {
				List<Section<?>> targets = reference.get().getIncludedSections(reference);
				for (Section<?> target : targets) {
					initIncludedArticles(target, visited);
				}
			}
		}
	}

	/**
	 * Returns the most recent date the root section's article or any of the
	 * included articles will have been modified.
	 * 
	 * @created 16.02.2014
	 * @return the last modified date of the inclusion of all affected sections
	 */
	public Date getLastModified() {
		Date maxDate = null;
		for (Article article : getIncludedArticles()) {
			Date date = KnowWEUtils.getLastModified(article);
			if (maxDate == null || date.after(maxDate)) {
				maxDate = date;
			}
		}
		return maxDate;
	}

	/**
	 * Checks if the version number requires an update, because a version number
	 * is specified but this article is older than the newest article that will
	 * be included.
	 * 
	 * @created 16.02.2014
	 */
	public boolean isNewVersionRequired() {
		if (!(section.get() instanceof IncludeMarkup)) return false;
		Section<IncludeMarkup> include = Sections.cast(section, IncludeMarkup.class);
		Section<? extends AnnotationContentType> versionSection =
				DefaultMarkupType.getAnnotationContentSection(include,
						IncludeMarkup.ANNOTATION_VERSION);
		if (versionSection == null) return false;

		// check dates if update is required
		Date thisDate = KnowWEUtils.getLastModified(section.getArticle());
		Date lastDate = getLastModified();
		return thisDate.before(lastDate);
	}

	public ExportModel createExport() throws IOException {
		// detect stream for word template
		Section<AttachmentType> attach = Sections.successor(section, AttachmentType.class);
		InputStream stream = (attach == null)
				? createDefaultTemplateStream()
				: createAttachmentStream(attach);

		// create builder and export the section
		try {
			ExportModel model = new ExportModel(this, stream);
			DefaultBuilder builder = new DefaultBuilder(model);
			if (section.get() instanceof IncludeMarkup) {
				updateDocumentInfo(Sections.cast(section, IncludeMarkup.class), model);
			}
			builder.export(section);
			// initialize some core properties
			CoreProperties coreProperties = builder.getDocument().getProperties().getCoreProperties();
			coreProperties.setModified(new Nullable<Date>(getLastModified()));
			return model;
		}
		finally {
			stream.close();
		}
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

	private void updateDocumentInfo(Section<IncludeMarkup> section, ExportModel model) {
		String project = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_PROJECT);
		if (!Strings.isBlank(project)) {
			model.setProperty("project", project);
		}
		String title = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_TITLE);
		if (!Strings.isBlank(title)) {
			model.setProperty("title", title);
		}
		String author = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_AUTHOR);
		if (!Strings.isBlank(author)) {
			model.setProperty("author", author);
		}
		String version = DefaultMarkupType.getAnnotation(section, IncludeMarkup.ANNOTATION_VERSION);
		if (!Strings.isBlank(version)) {
			model.setProperty("version", version);
		}
	}
}
