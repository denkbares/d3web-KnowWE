/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.knowwe.d3web.property;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Markup the add properties to an NamedObject
 * 
 * @author Albrecht Striffler, Markus Friedrich, (denkbares GmbH)
 * @created 10.11.2010
 */
public class PropertyMarkup extends DefaultMarkupType {

	private static final DefaultMarkup m;

	static {
		m = new DefaultMarkup("Property");
		m.addContentType(() -> new PropertyDeclarationType());
		PackageManager.addPackageAnnotation(m);

		m.setDocumentation("""
				Sets properties (such as prompts, descriptions, units or initial values) of objects of the knowledge base.
				<p>Each line has the form <code>Object.property = value</code>; add a locale after the property
				(<code>Object.prompt.en = …</code>) to set a language-specific value. Object names containing dots or
				other special characters are written in quotes. Answers of a choice question are referenced as
				<code>Question#Answer</code>; <code>#Answer</code> without a question applies the property to the
				answer of that name in all questions. Values spanning several lines are enclosed in triple quotes
				(<code>&quot;&quot;&quot;…&quot;&quot;&quot;</code>).</p>
				<p>The special object <code>KNOWLEDGEBASE</code> refers to the knowledge base itself.</p>
				""");
		m.addExample("""
				%%Property
				UB1.prompt.de = Bordnetzspannung ablesen und eingeben.
				UB1.prompt.en = Read power supply and enter.
				UB1.unit = V DC
				UB1.init = 28
				%
				""");
		m.addExample("""
				%%Property
				"Hauptdisplay@defekt".prompt.en = Driver main display defective, replace unit
				Betriebszustand#not_known.prompt.en = Unknown
				KNOWLEDGEBASE.noErrorSolution = NoErrorSolution
				NoErrorSolution.description.en = \"""
				The selected function is not defective,
				or the error occurs only sporadically.
				\"""
				%
				""");
	}

	public PropertyMarkup() {
		super(m);
	}

}
