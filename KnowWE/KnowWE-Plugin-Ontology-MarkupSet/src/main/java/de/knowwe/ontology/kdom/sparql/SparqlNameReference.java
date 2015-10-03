/*
 * Copyright (C) 2013 denkbares GmbH
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

package de.knowwe.ontology.kdom.sparql;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.types.LinkType;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.ontology.sparql.SparqlContentType;
import de.knowwe.tools.ToolMenuDecoratingRenderer;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Type that is a term reference to a sparql name.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 25.12.2014
 */
public class SparqlNameReference extends SimpleReference {

	public SparqlNameReference() {
		super(SparqlNameDefinition.TERM_COMPILER, SparqlNameDefinition.TERM_CLASS);
		setSectionFinder(new RegexSectionFinder(".*"));
		setRenderer(new ToolMenuDecoratingRenderer(StyleRenderer.CHOICE));
	}

	@Override
	public Identifier getTermIdentifier(Section<? extends Term> section) {
		return new Identifier(SparqlNameDefinition.TERM_PREFIX, getTermName(section));
	}

	/**
	 * Returns the actual sparql query that is referenced by the specified referencing section. If
	 * there is no such query, because the reference is not found of does not contain a valid sparql
	 * query, null is returned.
	 *
	 * @param section the referencing section contain the reference name
	 * @return the actual sparql query to be executed
	 */
	public String getQuery(Section<? extends SparqlNameReference> section) {
		Identifier identifier = getTermIdentifier(section);
		Rdf2GoCompiler compiler = Compilers.getCompiler(section, Rdf2GoCompiler.class);
		if (compiler == null) return null;
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		Section<?> sparqlSection = terminologyManager.getTermDefiningSection(identifier);
		Section<SparqlContentType> contentSection = null;
		if (sparqlSection == null) {
			Section<LinkType> linkSection = $(section).ancestor(InlineSparqlMarkup.class).parent().successor(LinkType.class).getFirst();
			if (linkSection != null) {
				String link = linkSection.getText();
				int start = link.lastIndexOf("|");
				if (start < 1) start = 1;
				link = link.substring(start + 1, link.length() - 1);
				Article article = KnowWEUtils.getArticle(section.getWeb(), link);
				if (article != null) {
					contentSection = $(article.getRootSection()).successor(SparqlContentType.class)	.getFirst();
				}
			}
		} else {
			contentSection = Sections.successor(sparqlSection, SparqlContentType.class);
		}
		return (contentSection == null) ? null : contentSection.getText();
	}

}
