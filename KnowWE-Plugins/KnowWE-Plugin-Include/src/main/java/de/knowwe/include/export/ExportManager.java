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

import com.denkbares.progress.ParallelProgress;
import com.denkbares.progress.ProgressListener;
import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.renderer.RenderKDOMType;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.include.IncludeMarkup;
import de.knowwe.include.WikiReference;
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

	static final String MSG_CREATE = "Creating word file";
	static final String MSG_SAVE = "Saving word file";

	private final Section<?> section;
	private Set<Section<?>> includedSections = null;
	private Set<Article> includedArticles = null;

	public ExportManager(Section<?> section) {
		this.section = section;
	}

	List<Exporter<?>> createExporters() {
		// please note that it is essential to create new instances
		// for each export process, because the exporters may
		// store information about the export process or the document
		List<Exporter<?>> exporters = new LinkedList<>();

		// add exporters
		exporters.add(new WikiBookPropertyExporter());
		exporters.add(new TOCExporter());
		exporters.add(new BoldExporter());
		exporters.add(new ItalicExporter());
		exporters.add(CodeExporter.VERBATIM);
		exporters.add(CodeExporter.PRETTIFY);
		exporters.add(new CodeStyleExporter());
		exporters.add(new LinkExporter());
		exporters.add(new FootnoteExporter());
		exporters.add(new FootnoteReferenceExporter());
		exporters.add(new WikiTextExporter());
		exporters.add(new PlainTextExporter());
		exporters.add(new HeaderExporter());
		exporters.add(new IncludeExporter());
		exporters.add(new ParagraphExporter());
		exporters.add(new ListExporter());
		exporters.add(new TableExporter());
		exporters.add(new ImageExporter());

		// Some known markups
		exporters.add(new TodoExporter());

		// add special exporters
		exporters.add(new InlineDefinitionExporter());
		exporters.add(new DefinitionExporter());

		// some types to be skipped
		exporters.add(new HideExporter<>(RenderKDOMType.class));

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
			// but after that we take only the sections' articles
			includedArticles = Sections.collectArticles(getIncludedSections());
		}
		return Collections.unmodifiableSet(includedArticles);
	}

	/**
	 * Returns the Sections of the this import and all other sections included
	 * by that section. If any included (sub-)section includes another include
	 * the indirectly included sections are also checked iteratively to any
	 * depth.
	 * <p>
	 * In the contained set there are only that sections that are directly
	 * included. Therefore, e.g. to check if a section is contained in the
	 * exported result, check if the section or any ancestor section is on the
	 * set of included sections.
	 * 
	 * @created 16.02.2014
	 * @return all sections included in this export
	 * @see #isContained(Section)
	 */
	public Set<Section<?>> getIncludedSections() {
		if (includedSections == null) {
			// for security reasons we have to check all visited
			// sections instead of only the articles
			// because there might be multiple different includes
			// in one single article
			Set<Section<?>> visited = new HashSet<>();
			initIncludedSections(section, visited);

			// but after that we take only the sections' articles
			includedSections = visited;
		}
		return Collections.unmodifiableSet(includedSections);
	}

	private void initIncludedSections(Section<?> section, Set<Section<?>> visited) {
		if (visited.add(section)) {
			List<Section<WikiReference>> references =
					Sections.successors(section, WikiReference.class);
			for (Section<WikiReference> reference : references) {
				List<Section<?>> targets = reference.get().getIncludedSections(reference);
				for (Section<?> target : targets) {
					initIncludedSections(target, visited);
				}
			}
		}
	}

	/**
	 * Returns if the specified section is contained in this export and
	 * therefore will be part of the exported document.
	 * <p>
	 * Please note, that depending on the section and it's ancestor sections,
	 * the exporter of the section may not directly be called, but some ancestor
	 * exporter will export the whole section subtree without delegate this to
	 * it's successors. This will not been examined by this method.
	 * 
	 * @created 20.02.2014
	 * @param section the section to be check if exported
	 * @return if the section will be part of the export
	 */
	public boolean isContained(Section<?> section) {
		Set<Section<?>> included = getIncludedSections();
		for (Section<?> s = section; s != null; s = s.getParent()) {
			if (included.contains(s)) return true;
		}
		return false;
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

	public ExportModel createExport(ProgressListener listener) throws IOException {
		ParallelProgress progress = new ParallelProgress(listener, 3f, 7f);
		progress.updateProgress(0, 0f, MSG_CREATE);

		// detect stream for word template
		Section<AttachmentType> attach = Sections.successor(section, AttachmentType.class);

		// create builder and export the section
		try (InputStream stream = (attach == null)
				? createDefaultTemplateStream()
				: createAttachmentStream(attach)) {
			ExportModel model = new ExportModel(this, stream, progress.getSubTaskProgressListener(1));
			DefaultBuilder builder = new DefaultBuilder(model);
			progress.updateProgress(0, 0.2f);
			if (section.get() instanceof IncludeMarkup) {
				updateDocumentInfo(Sections.cast(section, IncludeMarkup.class), model);
			}
			progress.updateProgress(0, 0.9f);
			builder.export(section);
			// initialize some core properties
			CoreProperties coreProperties = builder.getDocument().getProperties().getCoreProperties();
			coreProperties.setModified(new Nullable<>(getLastModified()));
			progress.updateProgress(1f, MSG_SAVE);
			return model;
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
