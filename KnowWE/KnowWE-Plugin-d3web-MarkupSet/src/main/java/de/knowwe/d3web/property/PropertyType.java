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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.objects.IncrementalMarker;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.StyleRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.subtreeHandler.IncrementalConstraint;
import de.d3web.we.object.ContentDefinition;
import de.d3web.we.object.IDObjectReference;
import de.d3web.we.object.LocaleDefinition;
import de.d3web.we.object.PropertyReference;
import de.d3web.we.user.UserContext;
import de.d3web.we.utils.KnowWEUtils;

/**
 * Adds the PropertyReviseSubtreeHandler to the Property line
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 10.11.2010
 */
public class PropertyType extends AbstractType implements IncrementalMarker, IncrementalConstraint<PropertyType> {

	/**
	 * 
	 * @author volker_belli
	 * @created 15.12.2010
	 */
	private static final class PropertyIDObbjetReferenceRenderer extends KnowWEDomRenderer<IDObjectReference> {

		@Override
		public void render(KnowWEArticle article, Section<IDObjectReference> sec, UserContext user, StringBuilder string) {
			NamedObject object = sec.get().getTermObject(article, sec);
			KnowWEDomRenderer renderer;
			if (object instanceof Question) {
				renderer = StyleRenderer.Question;
			}
			else if (object instanceof QContainer) {
				renderer = StyleRenderer.Questionaire;
			}
			else if (object instanceof Solution) {
				renderer = StyleRenderer.SOLUTION;
			}
			else if (object instanceof Choice) {
				renderer = StyleRenderer.CHOICE;
			}
			else {
				renderer = PlainText.getInstance().getRenderer();
			}
			renderer.render(article, sec, user, string);
		}
	}

	public PropertyType() {
		// all quoted strings, " can be masked by \
		// quoted strings need at least one char
		String quoted = "(?:\"[^\"\\\\]+(?:\\\\.[^\"\\\\]*)*\")";
		// no " . # = and line breaks allowed
		String unquotedName = "(?:[^\".=#\\n\\r])+";
		String name = "(?:" + quoted + "|" + unquotedName + ")";
		String language = "(\\.\\w{2}(?:\\.\\w{2})?)?";
		String idObject = "(" + name + "(?:#" + name + ")?" + ")";
		String leftSide = idObject + "\\.(" + name + ")" + language;
		// no " = and line breaks allowed, dots are allowed
		String unquotedContent = "(?:[^\"=\\n\\r])+";
		// starts and ends with """
		String trippleQuoted = "\"\"\"(?:[^\"]|\"(?!\"\")(?s).)+\"\"\"";
		String content = "(" + quoted + "|" + trippleQuoted + "|" + unquotedContent + ")+";
		String connector = "\\s*=\\s*";
		String pattern = "^ *" + leftSide + connector + content;
		Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);

		setSectionFinder(new RegexSectionFinder(p));

		// Locale
		LocaleDefinition ld = new LocaleDefinition();
		ld.setSectionFinder(new RegexSectionFinder(p, 3));
		this.childrenTypes.add(ld);

		// Content
		ContentDefinition cd = new ContentDefinition();
		cd.setSectionFinder(new RegexSectionFinder(Pattern.compile(connector + content), 1));
		cd.setCustomRenderer(StyleRenderer.PROPERTY);
		this.childrenTypes.add(cd);

		// Property
		PropertyReference pr = new PropertyReference();
		pr.setSectionFinder(new RegexSectionFinder(Pattern.compile("\\.(" + name + ")"), 1));
		this.childrenTypes.add(pr);

		// NamedObject
		IDObjectReference idor = new IDObjectReference();
		idor.setSectionFinder(new RegexSectionFinder(Pattern.compile(idObject + "\\."), 1));

		idor.setCustomRenderer(new PropertyIDObbjetReferenceRenderer());
		this.childrenTypes.add(idor);

		addSubtreeHandler(Priority.LOW, new PropertyReviseSubtreeHandler());
	}

	@Override
	public boolean violatedConstraints(KnowWEArticle article, Section<PropertyType> s) {
		return KnowWEUtils.getTerminologyHandler(
				article.getWeb()).areTermDefinitionsModifiedFor(article);
	}

}
