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

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.sparql.SparqlMarkupType;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.tools.ToolMenuDecoratingRenderer;

/**
 * Type that defines a sparql name as a term.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 27.12.2014
 */
public class SparqlNameDefinition extends SimpleDefinition {

	// TODO: change compiler class back to Rdf2GoCompiler.class when script registration allows super-classes
	// TODO: move this class and SparqlNameReference to Module "KnowWE-Plugin-Rdf2GoSemanticCore"
	public static final Class<? extends Rdf2GoCompiler> TERM_COMPILER = OntologyCompiler.class;
	public static final Class<?> TERM_CLASS = SparqlMarkupType.class;
	public static final String TERM_PREFIX = "SPARQL";

	public SparqlNameDefinition() {
		super(TERM_COMPILER, TERM_CLASS, Priority.ABOVE_DEFAULT);
		setSectionFinder(new AllTextFinderTrimmed());
		setRenderer(new ToolMenuDecoratingRenderer(StyleRenderer.CHOICE));
	}

	@Override
	public Identifier getTermIdentifier(TermCompiler compiler, Section<? extends Term> section) {
		return new Identifier(TERM_PREFIX, getTermName(section));
	}

}
