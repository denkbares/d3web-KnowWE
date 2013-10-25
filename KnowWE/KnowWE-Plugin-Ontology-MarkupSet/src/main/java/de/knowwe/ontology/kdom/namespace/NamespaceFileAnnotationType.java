/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.ontology.kdom.namespace;

import java.io.IOException;
import java.util.Collection;

import org.ontoware.rdf2go.exception.ModelRuntimeException;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.ontology.kdom.TerminologyHelper;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 05.03.2013
 */
public class NamespaceFileAnnotationType extends AbstractType {

	public NamespaceFileAnnotationType() {
		this.addChildType(new AbbreviationPrefixReference());
		this.addChildType(new FileNameType());
		this.setSectionFinder(new AllTextSectionFinder());
		this.addSubtreeHandler(Priority.HIGH, new ReadOntologyFileHandler());
	}

	private class ReadOntologyFileHandler extends SubtreeHandler<NamespaceFileAnnotationType> {

		@Override
		public Collection<Message> create(Article article, Section<NamespaceFileAnnotationType> section) {
			Section<FileNameType> fileNameSection = Sections.findChildOfType(section,
					FileNameType.class);
			if (fileNameSection == null) {
				return Messages.asList(Messages.error("No file name found in annotation '"
						+ section.getText()
						+ "'"));
			}
			Section<AbbreviationReference> abbrevSection = Sections.findSuccessor(section,
					AbbreviationReference.class);
			if (abbrevSection == null) {
				return Messages.asList(Messages.error("No namespace abbreviation found in annotation '"
						+ section.getText()
						+ "'"));
			}
			if (abbrevSection.hasErrorInSubtree()) {
				return Messages.noMessage();
			}
			String path = createPath(section, fileNameSection);
			WikiAttachment attachment = null;
			try {
				attachment = Environment.getInstance().getWikiConnector().getAttachment(
						path);
			}
			catch (IOException e) {
				return Messages.asList(Messages.error("Error while retrieving attachment '"
						+ fileNameSection.getText()
						+ "': " + e.getMessage()));
			}
			Rdf2GoCore core = Rdf2GoCore.getInstance(article);
			if (core == null) {
				return Messages.asList(Messages.error("No ontology repository found '"
						+ section.getText()
						+ "'"));
			}
			try {
				core.readFrom(attachment.getInputStream());
			}
			catch (ModelRuntimeException e) {
				return Messages.asList(Messages.error("Error while importing ontology from '"
						+ fileNameSection.getText()
						+ "': " + e.getMessage()));
			}
			catch (IOException e) {
				return Messages.asList(Messages.error("Error while importing ontology from '"
						+ fileNameSection.getText()
						+ "': " + e.getMessage()));
			}

			TerminologyManager terminologyManager = KnowWEUtils.getTerminologyManager(article);
			Section<?> abbrevDefSection = terminologyManager.getTermDefiningSection(abbrevSection.get().getTermIdentifier(
					abbrevSection));
			if (abbrevDefSection == null) {
				return Messages.noMessage();
			}
			Section<NamespaceAbbreviationDefinition> nsAbbrevDefSection = Sections.findAncestorOfType(
					abbrevDefSection, NamespaceAbbreviationDefinition.class);
			String namespace = nsAbbrevDefSection.get().getNamespace(nsAbbrevDefSection);

			new FileTerminologyHelper(abbrevSection.getText()).registerTerminology(article,
					section, namespace);
			return Messages.noMessage();
		}

		private String createPath(Section<NamespaceFileAnnotationType> section, Section<FileNameType> fileNameSection) {
			return section.getTitle() + "/" + fileNameSection.getText();
		}
	}

	private static class FileNameType extends AbstractType {

		public FileNameType() {
			this.setSectionFinder(new AllTextSectionFinder());
		}
	}

	private class FileTerminologyHelper extends TerminologyHelper {

		private final String abbreviation;

		public FileTerminologyHelper(String abbreviation) {
			this.abbreviation = abbreviation;
		}

		@Override
		protected String getAbbreviation(String string) {
			return abbreviation;
		}

	}

}
