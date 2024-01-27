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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.kdom.rules.Indent;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.LocaleType;
import de.knowwe.core.kdom.objects.IncrementalTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.AnchorRenderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.utils.Patterns;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * One Property definition inside the PropertyMarkup.
 *
 * @author Markus Friedrich, Albrecht Striffler (denkbares GmbH)
 * @created 10.11.2010
 */
public class PropertyDeclarationType extends AbstractType {

	public static final String QUOTED_NAME = Patterns.QUOTED;
	public static final String UNQUOTED_NAME = "[^\".=#\\n\\r]*";
	public static final String NAME = "(?:" + QUOTED_NAME + "|" + UNQUOTED_NAME + ")";

	public PropertyDeclarationType() {

		String tripleQuotes = "\"\"\"";
		String noTripleQuotes = "(?!" + tripleQuotes + ")";
		String anyCharButNoTripleQuotes = "(?:" + noTripleQuotes + ".)";

		String singleLinePropertyDeclaration = anyCharButNoTripleQuotes + "+?$\\s*"
				+ "(?!\\s*" + tripleQuotes + ")";
		// the singleLinePropertyDeclaration is a line that does not contain
		// triple quotes and also is not followed by a line that starts (maybe
		// after some white spaces) with a triple quote

		String multiLinePropertyDeclaration = ".+?" + tripleQuotes + ".+?" + tripleQuotes;

		String propertyDeclaration = "^\\s*("
				+ singleLinePropertyDeclaration + "|"
				+ multiLinePropertyDeclaration + ")\\s*?(^|\\z)";

		Pattern p = Pattern.compile(propertyDeclaration, Pattern.MULTILINE + Pattern.DOTALL);
		setSectionFinder(new RegexSectionFinder(p, 1));

		this.addChildType(new PropertyMarkupObjectReference());

		this.addChildType(new PropertyType());
		this.addChildType(new LocaleType("."));
		this.addChildType(new PropertyContentType());
		this.addChildType(new Indent());

		addCompileScript(new PropertyDeclarationHandler());

		/*
		 We need a second pass of the handler for named objects, that are not yet created at default priority.
		 An example for such named objects are Flows. They are created with all completed nodes and edges. The nodes
		 and edges in turn contain actions which may rely on certain properties again (like units for QuestionNums).
		 Because Flow ultimately may have to wait for the creation of some properties, they are created after the first
		 pass. The second pass then will fix those property declarations for named objects like Flows, that could not
		 be created at the first pass, because they had to wait for this first pass...
		 */
		addCompileScript(Priority.LOWEST, new PropertyDeclarationHandler());

		this.setRenderer(AnchorRenderer.getDelegateInstance());
	}

	/**
	 * Returns the identifiers that are referenced by the property. Note that there can be referenced multiple
	 * identifiers, using wildcards for questions.
	 */
	@NotNull
	public List<Identifier> getTermIdentifiers(TermCompiler compiler, Section<PropertyDeclarationType> section) {
		return $(section).successor(PropertyObjectReference.class)
				.mapOptional(ref -> ref.get().getTermIdentifiers(compiler, ref)).orElse(Collections.emptyList());
	}

	/**
	 * Returns the NamedObjects that are referenced by the property. Note that there can be referenced multiple
	 * NamedObjects, using wildcards for questions.
	 */
	@NotNull
	public List<NamedObject> getTermObjects(D3webCompiler compiler, Section<PropertyDeclarationType> section) {
		return $(section).successor(PropertyObjectReference.class)
				.mapOptional(ref -> ref.get().getTermObjects(compiler, ref)).orElse(Collections.emptyList());
	}

	/**
	 * Returns the property that is set by this markup, or null if the property is not correctly be defined.
	 */
	@SuppressWarnings("rawtypes")
	@Nullable
	public Property getProperty(Section<PropertyDeclarationType> section) {
		return $(section).successor(PropertyType.class).mapFirst(PropertyType::getProperty);
	}

	/**
	 * Returns the denoted locale, the property is set for, or Locale.ROOT if no locale is specified.
	 */
	@NotNull
	public Locale getLocale(Section<PropertyDeclarationType> section) {
		return $(section).successor(LocaleType.class).mapOptional(LocaleType::getLocale).orElse(Locale.ROOT);
	}

	/**
	 * Returns the unparsed content string that is assigned to the property. If no valid content is specified, null is
	 * returned.
	 */
	@Nullable
	public String getValueString(Section<PropertyDeclarationType> section) {
		return $(section).successor(PropertyContentType.class).mapFirst(PropertyContentType::getPropertyContent);
	}

	private static class PropertyMarkupObjectReference extends PropertyObjectReference {


		@Override
		protected Property<?> getProperty(Section<PropertyObjectReference> reference) {
			return $(reference).ancestor(PropertyDeclarationType.class)
					.successor(PropertyType.class)
					.mapFirst(p -> p.get().getProperty(p));
		}

		@Override
		protected Locale getLocale(Section<PropertyObjectReference> reference) {
			return $(reference).ancestor(PropertyDeclarationType.class)
					.successor(LocaleType.class)
					.map(l -> l.get().getLocale(l)).findFirst().orElse(Locale.ROOT);
		}

		@Override
		protected String getPropertyValue(Section<PropertyObjectReference> reference) {
			return $(reference).ancestor(PropertyDeclarationType.class)
					.successor(PropertyContentType.class)
					.mapFirst(s -> s.get().getPropertyContent(s));
		}

		@Override
		public Sections<?> getDependingSections(IncrementalCompiler compiler, Section<IncrementalTerm> section, Class<?>... scriptFilter) {
			return $(section).ancestor(PropertyDeclarationType.class);
		}
	}
}
