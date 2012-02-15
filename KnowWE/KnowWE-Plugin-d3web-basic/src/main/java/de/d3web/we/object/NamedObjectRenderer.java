/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.KnowWEDomRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.02.2012
 */
public class NamedObjectRenderer extends KnowWEDomRenderer<D3webTermReference<NamedObject>> {

	@SuppressWarnings({
			"rawtypes", "unchecked" })
	@Override
	public void render(KnowWEArticle article, Section<D3webTermReference<NamedObject>> sec, UserContext user, StringBuilder string) {
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
		else if (object instanceof KnowledgeBase) {
			renderer = StyleRenderer.Questionaire;
		}
		else {
			renderer = PlainText.getInstance().getRenderer();
		}
		renderer.render(article, sec, user, string);
	}
}
