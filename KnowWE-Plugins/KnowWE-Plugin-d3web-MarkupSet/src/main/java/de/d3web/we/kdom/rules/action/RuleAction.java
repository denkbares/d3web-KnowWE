/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
import java.util.Collection;
import java.util.List;

import com.denkbares.strings.Strings;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.session.Session;
import de.d3web.we.kdom.action.D3webRuleAction;
import de.d3web.we.kdom.rules.RuleCompileScript;
import de.d3web.we.kdom.rules.RuleType;
import de.d3web.we.kdom.ruletable.RuleTableMarkup;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.EndLineComment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.StyleRenderer;

public class RuleAction extends AbstractType {

	public RuleAction() {
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		EndLineComment comment = new EndLineComment();
		comment.setRenderer(StyleRenderer.COMMENT);
		this.addChildType(comment);
		List<Type> actions = RuleType.getActions();
		for (Type action : actions) {
			this.addChildType(action);
		}
		setRenderer((section, user, result) -> {
			result.appendHtml("<div class='RuleAction'>");
			ruleActionRenderer(section, user, result);
			result.appendHtml("</div>");
		});
	}

	private void ruleActionRenderer(Section<?> section, UserContext user, RenderResult result) {
		D3webCompiler compiler = Compilers.getCompiler(section, D3webCompiler.class);
		Session session = D3webUtils.getExistingSession(compiler, user);
		List<String> classes = new ArrayList<>();
		classes.add("RuleAction");
		if (session != null) {
			Section<D3webRuleAction> actionSection = Sections.successor(section, D3webRuleAction.class);
			Collection<Rule> defaultRules = RuleCompileScript.getRules(compiler, actionSection, RuleCompileScript
					.DEFAULT_RULE_STORE_KEY);
			if (!defaultRules.isEmpty()) {
				Rule defaultRule = defaultRules.iterator().next();
				Condition condition = defaultRule.getCondition();
				classes.add(RuleTableMarkup.evaluateSessionCondition(session, condition));
			}
		}
		result.appendHtml("<span id='" + section.getID() + "' class='" + Strings.concat(" ", classes) + "'>");
		DelegateRenderer.getInstance().render(section, user, result);
		result.appendHtml("</span>");
	}
}
