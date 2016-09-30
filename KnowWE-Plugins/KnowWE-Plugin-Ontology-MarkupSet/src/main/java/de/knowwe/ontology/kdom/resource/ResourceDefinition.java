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
package de.knowwe.ontology.kdom.resource;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

import static de.knowwe.core.kdom.parsing.Sections.$;

public class ResourceDefinition extends SimpleDefinition {

	private static final String IDENTIFIER_KEY = "identifierKey";

	public ResourceDefinition(Class<?> termClass) {
		super(OntologyCompiler.class, termClass);
		this.setSectionFinder(new AllTextFinderTrimmed());
		this.setRenderer(StyleRenderer.Question);
		this.addCompileScript(Priority.HIGHER, new OntologyCompileScript<ResourceDefinition>() {

			@Override
			public void compile(OntologyCompiler compiler, Section<ResourceDefinition> section) throws CompilerMessage {
				registerCompatibilityIdentifier(compiler, section);
			}

			@Override
			public void destroy(OntologyCompiler compiler, Section<ResourceDefinition> section) {
				unregisterCompatibilityIdentifier(compiler, section);
			}
		});

	}

	public static void registerCompatibilityIdentifier(OntologyCompiler compiler, Section<? extends Term> section) {
		handleCompatibilityRegistration(compiler, section, true);
	}

	public static void unregisterCompatibilityIdentifier(OntologyCompiler compiler, Section<? extends Term> section) {
		handleCompatibilityRegistration(compiler, section, false);
	}

	private static void handleCompatibilityRegistration(OntologyCompiler compiler, Section<? extends Term> section, boolean add) {
		Identifier compatibilityIdentifier = getCompatibilityIdentifier(compiler, section);
		if (compatibilityIdentifier != null) {
			if (add) {
				compiler.getTerminologyManager()
						.registerTermDefinition(compiler, section, section.get().getTermObjectClass(section),
								compatibilityIdentifier);
			}
			else {
				compiler.getTerminologyManager()
						.unregisterTermDefinition(compiler, section, section.get().getTermObjectClass(section),
								compatibilityIdentifier);
			}
		}
	}

	private static Identifier getCompatibilityIdentifier(OntologyCompiler compiler, Section<? extends Term> section) {
		Identifier identifier = section.get().getTermIdentifier(section);
		String abbreviation = identifier.getPathElementAt(0);
		if (!abbreviation.isEmpty() && !Rdf2GoCore.LNS_ABBREVIATION.equals(abbreviation)) return null;
		Rdf2GoCore core = compiler.getRdf2GoCore();
		if (core.getLocalNamespace().equals(core.getNamespaces().get(""))) {
			String termName = section.get().getTermName(section);
			if (abbreviation.isEmpty()) {
				return new Identifier(Rdf2GoCore.LNS_ABBREVIATION, termName);
			}
			else {
				return new Identifier("", termName);
			}
		}
		return null;
	}

	@Override
	public Identifier getTermIdentifier(Section<? extends Term> section) {
		Identifier identifier = (Identifier) section.getObject(IDENTIFIER_KEY);
		if (identifier == null) {
			String abbreviation = $(section).ancestor(AbbreviatedResourceDefinition.class)
					.mapFirst(AbbreviatedResourceDefinition::getAbbreviation);
			identifier = new Identifier(abbreviation, getTermName(section));
			section.storeObject(IDENTIFIER_KEY, identifier);
		}
		return identifier;
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		// we don't want resource to be quoted by interface's default implementation
		return newIdentifier.getLastPathElement();
	}
}
