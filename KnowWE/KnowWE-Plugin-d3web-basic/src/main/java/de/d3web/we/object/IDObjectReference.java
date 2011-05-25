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
package de.d3web.we.object;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.TermReference;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.Patterns;

/**
 * Type for {@link NamedObject} references
 * <p/>
 * FIXME: This is not really a TermReference, it needs a redesign.
 * <p/>
 * <b>Problems:</b>
 * <p/>
 * - There is no definition counterpart for this reference... definitions and
 * references are matched by their term name (getTermName(Section)), but there
 * is no definition with the term name given here. <br/>
 * - Also the term object should normally be retrieved via the term definition.
 * The getTermObjectFallback()-method is designed to help with compatibility
 * issues with old markups.
 * 
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 11.11.2010
 */
public class IDObjectReference extends D3webTermReference<NamedObject> {

	public IDObjectReference() {
		super(NamedObject.class);
		this.subtreeHandler.clear();
	}

	@Override
	public String getTermName(Section<? extends KnowWETerm<NamedObject>> s) {
		return s.getOriginalText();
	}

	@Override
	public NamedObject getTermObjectFallback(KnowWEArticle article, Section<? extends TermReference<NamedObject>> s) {
		if (s.get() instanceof IDObjectReference) {
			// # not allowed
			String unquotedNamePattern = "(?:[^#])+";
			String namePattern = "(" + Patterns.quoted + "|" + unquotedNamePattern + ")";
			String idObjectPattern = namePattern + "(?:#" + namePattern + ")?";
			Pattern p = Pattern.compile(idObjectPattern);
			Matcher matcher = p.matcher(s.get().getTermName(s));
			if (!matcher.matches()) return null;
			String idObjectName = KnowWEUtils.trimQuotes(matcher.group(1));
			String choiceString = matcher.group(2);
			if (choiceString != null) {
				choiceString = KnowWEUtils.trimQuotes(matcher.group(2));
			}
			KnowledgeBase kb = D3webModule.getKnowledgeRepresentationHandler(
					article.getWeb()).getKB(article.getTitle());
			NamedObject idObject = findNamedObjectByName(idObjectName, kb);
			if (idObject instanceof QuestionChoice && choiceString != null) {
				QuestionChoice qc = (QuestionChoice) idObject;
				idObject = null;
				for (Choice c : qc.getAllAlternatives()) {
					if (c.getName().equalsIgnoreCase(choiceString)) {
						return c;
					}
				}
			}
			return idObject;
		}
		return null;
	}

	/**
	 * Finds the {@link TerminologyObject} with the specified name. This method
	 * is case insensitive
	 * 
	 * @created 10.11.2010
	 * @param name Name of the {@link TerminologyObject}
	 * @return {@link TerminologyObject} with the specified name
	 */
	private static NamedObject findNamedObjectByName(String name, KnowledgeBase knowledgeBase) {
		if (name.equals("KNOWLEDGEBASE")) return knowledgeBase;
		List<TerminologyObject> objects = new LinkedList<TerminologyObject>();
		objects.addAll(knowledgeBase.getManager().getQContainers());
		objects.addAll(knowledgeBase.getManager().getSolutions());
		objects.addAll(knowledgeBase.getManager().getQuestions());
		for (TerminologyObject object : objects) {
			if (object.getName().equalsIgnoreCase(name)) {
				return object;
			}
		}
		return null;
	}

	@Override
	public String getTermObjectDisplayName() {
		return "Object";
	}

}
