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

import org.ontoware.rdf2go.model.node.Node;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.ontology.kdom.namespace.AbbreviationReference;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceReference;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.ontology.kdom.resource.ResourceReference;
import de.knowwe.ontology.turtle.compile.NodeProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;

import static de.knowwe.core.kdom.parsing.Sections.$;

public class EncodedTurtleURI extends AbstractType implements NodeProvider<EncodedTurtleURI> {

	public EncodedTurtleURI() {
		this.setSectionFinder(new ExpressionInBracketsFinder('<', '>'));

		Renderer renderer = (section, user, result) -> result.append(Strings.encodeHtml(section.getText()));

		this.addChildType(new AnonymousType("LongURIOpeningType", new RegexSectionFinder(Pattern.compile("^<")), renderer));
		this.addChildType(new AnonymousType("LongURIClosingType", new RegexSectionFinder(Pattern.compile(">$")), renderer));

		AbbreviatedResourceReference abbreviatedResourceReference = new AbbreviatedResourceReference();
		abbreviatedResourceReference.setSectionFinder(new RegexSectionFinder(Pattern.compile("^\\w+:(?!//).+$")));
		abbreviatedResourceReference.replaceChildType(ResourceReference.class, new EncodedResourceReference());
		this.addChildType(abbreviatedResourceReference);

		// should only find something, if it was not an abbreviated encoded uri
		this.addChildType(new AnonymousType("LongEncodedTurtleURI", AllTextFinder.getInstance(), renderer));
	}

	private String getURI(Section<EncodedTurtleURI> section) {
		return section.getText().substring(1, section.getText().length() - 1);
	}

	@Override
	public Node getNode(Section<EncodedTurtleURI> section, Rdf2GoCompiler core) {
		String uri = getURI(section);
		return core.getRdf2GoCore().createURI(uri);
	}

	private static class EncodedResourceReference extends ResourceReference {

		public EncodedResourceReference() {
			super(Resource.class);
		}

		@Override
		public String getTermName(Section<? extends Term> section) {
			return Strings.decodeURL(section.getText());
		}

		@Override
		public Identifier getTermIdentifier(Section<? extends Term> section) {
			Identifier identifier = (Identifier) section.getObject(IDENTIFIER_KEY);
			if (identifier == null) {
				String abbreviation = $(section).ancestor(EncodedTurtleURI.class)
						.successor(AbbreviationReference.class)
						.mapFirst(Term::getTermName);
				identifier = new Identifier(abbreviation, getTermName(section));
				section.storeObject(IDENTIFIER_KEY, identifier);
			}
			return identifier;
		}

		@Override
		public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
			return Strings.encodeURL(super.getSectionTextAfterRename(section, oldIdentifier, newIdentifier));
		}

	}

}
