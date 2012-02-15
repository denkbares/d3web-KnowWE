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

import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.KnowWEDomRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.SplitUtility;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;
import de.knowwe.kdom.sectionFinder.OneOfStringEnumUnquotedFinder;

/**
 * 
 * @author Jochen
 * @param <T>
 * @created 30.07.2010
 */
public abstract class BracketsAction<T extends Type> extends D3webRuleAction<T> {

	protected static final String OPEN = "[";
	protected static final String CLOSE = "]";

	public BracketsAction(final String[] alternativeKeys) {
		this.setRenderer(new KnowWEDomRenderer<Type>() {

			@Override
			public void render(KnowWEArticle article, Section<Type> sec, UserContext user, StringBuilder string) {
				StringBuilder b = new StringBuilder();
				DelegateRenderer.getInstance().render(article, sec, user, b);
				string.append(b.toString().replaceAll("\\[", "~["));
				
			}
		});
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
	protected abstract Type getObjectReference();

}
