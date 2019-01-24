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

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.provider.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

public class TurtleLiteralType extends AbstractType implements NodeProvider<TurtleLiteralType> {

	public static final String XSD_PATTERN = "(?:\\^\\^xsd:(\\w+))";
	public static final String LANGUAGE_TAG = "(?:@\\w+(?:[-_]\\w+)?)";
	public static final String LITERAL_SUFFIX = "^(?:" + LANGUAGE_TAG + "|" + XSD_PATTERN + ")";

	private static final Pattern LITERAL_SUFFIX_PATTERN = Pattern.compile(LITERAL_SUFFIX);

	public TurtleLiteralType() {
		this.setSectionFinder(new LiteralTypeFinder());
		this.setRenderer(StyleRenderer.CONTENT);
		this.addChildType(new XSDPart());
		this.addChildType(new AnonymousType("XSDDeclaration",
				new RegexSectionFinder("\\^\\^xsd:\\z"),
				DelegateRenderer.getInstance()));
		this.addChildType(new LanguageTagPart());
		this.addChildType(new LiteralPart());
	}

	public org.eclipse.rdf4j.model.Literal getLiteral(Rdf2GoCore core, Section<? extends TurtleLiteralType> section) {
		Section<LiteralPart> literalPartSection = Sections.child(section,
				LiteralPart.class);
		Section<XSDPart> xsdPartSection = Sections.child(section, XSDPart.class);
		Section<LanguageTagPart> langTagPartSection = Sections.child(section, LanguageTagPart.class);
		assert literalPartSection != null;
		String literal = literalPartSection.get().getLiteral(literalPartSection);
		if (langTagPartSection != null) {
			return core.createLanguageTaggedLiteral(literal,
					langTagPartSection.get().getTag(langTagPartSection));
		}
		IRI xsdType = null;
		if (xsdPartSection != null) {
			xsdType = xsdPartSection.get().getXSDType(xsdPartSection);
		}
		if (xsdType == null) {
			// sectionizer takes care that this has to be plain type
			return core.createLiteral(literal);
		}
		return core.createLiteral(literal, xsdType);
	}

	private static class LiteralTypeFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			// try triple quotes
			int firstIndex = text.indexOf("\"\"\"");
			int lastIndex = text.lastIndexOf("\"\"\"");
			boolean isTripleQuoted = true;
			if (!validStartAndEnd(firstIndex, lastIndex)) {
				isTripleQuoted = false;
				// try normal double quotes
				firstIndex = Strings.indexOfUnquoted(text, "\"");
				if (firstIndex >= 0) {
					lastIndex = Strings.indexOf(text, firstIndex + 1, Strings.UNQUOTED, "\"");
				}
			}
			if (!validStartAndEnd(firstIndex, lastIndex)) {
				// try single quotes
				firstIndex = Strings.indexOfUnquoted(text, "'");
				if (firstIndex >= 0) {
					lastIndex = Strings.indexOf(text, firstIndex + 1, Strings.UNQUOTED, "'");
				}
			}
			if (validStartAndEnd(firstIndex, lastIndex)) {
				// triple quotes, check if there is also a language/xsd suffix
				if (isTripleQuoted) { // move index after the quote
					lastIndex += 3; // triple quotes, just add length
				}
				else {
					lastIndex += 1; // normal or single quotes, add offset while searching + 1
				}
				Matcher matcher = LITERAL_SUFFIX_PATTERN.matcher(text.substring(lastIndex));
				if (matcher.find()) {
					lastIndex += matcher.end();
				}
				return Collections.singletonList(new SectionFinderResult(firstIndex, lastIndex));
			}
			return null;
		}

		private boolean validStartAndEnd(int firstIndex, int lastIndex) {
			return firstIndex != lastIndex || firstIndex >= 0;
		}
	}

	private static class LiteralPart extends AbstractType {

		public LiteralPart() {
			this.setSectionFinder(AllTextFinder.getInstance());
		}

		public String getLiteral(Section<LiteralPart> section) {
			return Rdf2GoCore.unquoteTurtleLiteral(section.getText());
		}
	}

	private static class LanguageTagPart extends AbstractType {

		public LanguageTagPart() {
			this.setSectionFinder(new RegexSectionFinder(LANGUAGE_TAG + "\\z"));
		}

		public String getTag(Section<LanguageTagPart> section) {
			return section.getText().substring(1);
		}
	}

	private static class XSDPart extends AbstractType {

		public XSDPart() {
			this.setSectionFinder(new RegexSectionFinder(Pattern.compile(XSD_PATTERN + "\\z"), 1));
		}

		public IRI getXSDType(Section<XSDPart> section) {
			return SimpleValueFactory.getInstance().createIRI(XMLSchema.NAMESPACE + section.getText());
		}
	}

	@Override
	public Value getNode(Section<? extends TurtleLiteralType> section, Rdf2GoCompiler core) {
		return getLiteral(core.getRdf2GoCore(), section);
	}

}
