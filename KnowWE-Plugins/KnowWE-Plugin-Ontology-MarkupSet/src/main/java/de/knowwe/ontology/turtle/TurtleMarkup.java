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
package de.knowwe.ontology.turtle;

import java.util.regex.Pattern;

import com.denkbares.strings.QuoteSet;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyMarkup;

/**
 * @author Jochen Reutelshöfer
 * @created 08.07.2013
 */
public class TurtleMarkup extends DefaultMarkupType {

	public static final QuoteSet[] TURTLE_QUOTES = new QuoteSet[] {
			QuoteSet.TRIPLE_QUOTES,
			new QuoteSet('"'), new QuoteSet('\''),
			new QuoteSet('(', ')'),
			new QuoteSet('[', ']'), new QuoteSet('<', '>') };

	private static final DefaultMarkup MARKUP;
	public static final String COMPILE = "compile";

	static {
		MARKUP = new DefaultMarkup("Turtle");
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.ignoreAnnotation("prefix");
		MARKUP.addContentType(() -> new TurtlePrefixType());
		MARKUP.addContentType(() -> new TurtleContent());
		MARKUP.addAnnotation(COMPILE, false, "true", "false");

		MARKUP.setDocumentation("""
				Defines RDF statements in Turtle syntax that are added to the ontologies compiling the package of the markup.
				<p>Each sentence consists of a subject, predicate-object pairs separated by <code>;</code>, objects
				separated by <code>,</code>, and ends with a <code>.</code>. Literals may carry a language tag
				(<code>"text"@en</code>) or an XSD datatype (<code>"42"^^xsd:int</code>), <code>a</code> abbreviates
				<code>rdf:type</code>.</p>
				<p>Namespace abbreviations are not declared with <code>@prefix</code> inside the markup (such lines are
				ignored with a warning), but with the markup <code>%%Namespace</code> or the
				<code>@defaultNamespace</code> of the <code>%%Ontology</code>.</p>
				""");
		MARKUP.getAnnotation(PackageManager.PACKAGE_ATTRIBUTE_NAME).setExample("@package: demo");
		MARKUP.getAnnotation(COMPILE)
				.setDocumentation("Set to <code>false</code> to keep the statements of this markup out of the ontology " +
								  "without removing the markup (default is <code>true</code>).")
				.setExample("@compile: false");
		MARKUP.addExample("""
				%%Turtle
				car:battery a car:Component ;
				  rdfs:label "Battery"@en , "Batterie"@de ;
				  car:partOf car:electricalSystem .
				%
				""");
		MARKUP.addExample("""
				%%Turtle
				car:Component a owl:Class ;
				  rdfs:label "Component"@en .

				car:partOf a owl:ObjectProperty ;
				  rdfs:domain car:Component ;
				  rdfs:range car:Component .

				@package: demo
				%
				""");
	}

	public TurtleMarkup() {
		this(MARKUP);
	}

	public TurtleMarkup(DefaultMarkup markup) {
		super(markup);
	}

	private static class TurtlePrefixType extends AbstractType {

		public TurtlePrefixType() {
			this.setSectionFinder(new RegexSectionFinder("^\\p{Blank}*@prefix.+?$", Pattern.MULTILINE));
			this.addCompileScript(new OntologyCompileScript<TurtlePrefixType>() {

				@Override
				public void compile(OntologyCompiler compiler, Section<TurtlePrefixType> section) throws CompilerMessage {
					throw new CompilerMessage(Messages.warning("Ignoring '" + section.getText()
							+ "', please use markup %%" + OntologyMarkup.MARKUP.getName() + " to specify namespaces."));
				}

				@Override
				public void destroy(OntologyCompiler compiler, Section<TurtlePrefixType> section) {
					// nothing to do, message will be destroyed with section
				}
			});
		}
	}
}
