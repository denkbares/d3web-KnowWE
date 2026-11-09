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
				Exhaust fumes.prompt.en = What is the color of the exhaust fumes?
				Exhaust fumes.prompt.de = Welche Farbe haben die Abgase?
				"Real mileage  /100km".unit = l
				"Real mileage  /100km".init = 8
				%
				""");
		m.addExample("""
				%%Property
				"Check: Battery.".prompt.en = Is the battery voltage above 12 V?
				Exhaust fumes#black.prompt.de = schwarz
				#invisible.prompt.de = unsichtbar
				Flat battery.description.en = \"""
				The battery cannot provide enough energy
				to turn over the starter.
				\"""
				%
				""");
	}

	public PropertyMarkup() {
		super(m);
	}

}
