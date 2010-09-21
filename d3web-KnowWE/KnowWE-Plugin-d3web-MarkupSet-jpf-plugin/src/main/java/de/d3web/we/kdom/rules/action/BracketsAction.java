/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom.rules.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.indication.ActionContraIndication;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.rules.action.ContraIndicationAction.QuestionReferenceInBrackets;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalSectionFinder;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.OneOfStringEnumUnquotedFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;

/**
 * 
 * @author Jochen
 * @param <T>
 * @created 30.07.2010
 */
public abstract class BracketsAction<T extends KnowWEObjectType> extends D3webRuleAction<T> {

	protected static final String OPEN = "[";
	protected static final String CLOSE = "]";

	public BracketsAction(final String[] alternativeKeys) {
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				for (String string : alternativeKeys) {
					if (text.startsWith(string)) {
						if (SplitUtility.containsUnquoted(text, OPEN)
								&& SplitUtility.containsUnquoted(text, CLOSE)) {
							return true;
						}
					}
				}

				return false;
			}
		};

		AnonymousType negKey = new AnonymousType(this.getClass().getSimpleName() + "key");
		negKey.setSectionFinder(new OneOfStringEnumUnquotedFinder(alternativeKeys));
		this.addChildType(negKey);

		this.addChildType(getObjectReference());

	}

	/**
	 * 
	 * @created 30.07.2010
	 * @return
	 */
	protected abstract KnowWEObjectType getObjectReference();

}
