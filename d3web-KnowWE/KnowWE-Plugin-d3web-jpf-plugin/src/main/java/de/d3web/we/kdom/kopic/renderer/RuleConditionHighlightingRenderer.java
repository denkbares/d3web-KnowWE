/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.kopic.renderer;

import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.inference.condition.NoAnswerException;
import de.d3web.core.inference.condition.UnknownAnswerException;
import de.d3web.core.session.Session;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Annotation.Finding;
import de.d3web.we.kdom.condition.ComplexFinding;
import de.d3web.we.kdom.condition.ComplexFindingBraced;
import de.d3web.we.kdom.condition.old.Conjunct;
import de.d3web.we.kdom.condition.old.Disjunct;
import de.d3web.we.kdom.renderer.FontColorBackgroundRenderer;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Highlights the Rules in Kopic-Sections according to the Session.
 *
 * @author Johannes Dienst
 *
 */
public class RuleConditionHighlightingRenderer extends KnowWEDomRenderer {

	private static RuleConditionHighlightingRenderer instance;

	/**
	 * Singleton.
	 *
	 * @return
	 */
	public static RuleConditionHighlightingRenderer getInstance() {
		if (instance == null)
			instance = new RuleConditionHighlightingRenderer();
		return instance;
	}

	/**
	 * Private Constructor.
	 */
	private RuleConditionHighlightingRenderer() {
	}

	// @Override
	// public String render(Section sec, KnowWEUserContext user) {
	//
	// // get the rule: Eval it and highlight the condition
	// Section rule = KnowWEObjectTypeUtils.getAncestorOfType(sec, Rule.class);
	// String kbRuleId = (String) KnowWEUtils.getStoredObject(sec.getWeb(),
	// sec.getTitle(), rule.getId(), Rule.KBID_KEY);
	//
	// // Get KnowledgeServiceSession containing the Session
	// String sessionId = sec.getTitle() + ".." +
	// KnowWEEnvironment.generateDefaultID(sec.getTitle());
	// Broker broker = D3webModule.getBroker(user.getUsername(), sec.getWeb());
	// KnowledgeServiceSession kss =
	// broker.getSession().getServiceSession(sessionId);
	//
	// if (kss instanceof D3webKnowledgeServiceSession) {
	//
	// // Get the Session
	// D3webKnowledgeServiceSession d3webKSS = (D3webKnowledgeServiceSession)
	// kss;
	// Session session = d3webKSS.getXpsCase();
	//
	// // Get the RuleComplex with kbRuleId
	// Collection<KnowledgeSlice> slices =
	// session.getKnowledgeBase().getAllKnowledgeSlices();
	// for (KnowledgeSlice slice : slices) {
	//
	// if (slice.getId().equals(kbRuleId)) {
	// RuleComplex rc = (RuleComplex) slice;
	// return this.renderConditionLine(sec, rc, session, user);
	// }
	// }
	// }
	// return null;
	// }

	@Override
	public void render(KnowWEArticle article, Section sec,
			KnowWEUserContext user, StringBuilder result) {

		// get the rule: Eval it and highlight the condition
		Section ruleSection = sec.findAncestorOfType(
				de.d3web.we.kdom.rules.Rule.class);
		String kbRuleId = (String) KnowWEUtils.getStoredObject(sec.getWeb(),
				sec.getTitle(), ruleSection.getID(),
				de.d3web.we.kdom.rules.Rule.KBID_KEY);

		Session session = D3webUtils.getSession(article.getTitle(), user, article.getWeb());
		Rule rule = null;

		if (session != null) {
			Collection<KnowledgeSlice> slices = session.getKnowledgeBase()
					.getAllKnowledgeSlices();
			for (KnowledgeSlice slice : slices) {
				if (slice != null) {
					if (slice instanceof RuleSet) {
						RuleSet rs = (RuleSet) slice;
						for (Rule r : rs.getRules()) {
							String id = r.getId();
							if (id != null && id.equals(kbRuleId)) {
								rule = r;
							}
						}
					}

				}
			}
		}
		if (rule != null) this.renderConditionLine(article, sec, rule, session, user,
				result);
		else result.append(sec.getOriginalText());
	}

	/**
	 * Renders a ConditionLine. Due to a ConditionLine atm contains many
	 * Children like ComplexFinding/Finding/ComplexFindingBraced... and not just
	 * RuleCondition everything has to be done here. TODO: A ConditionLine
	 * should only contain RuleCondition. So a special Renderer only for this
	 * section can be written.
	 *
	 * @param sec
	 * @param rc
	 * @param session
	 * @return
	 */
	// private String renderConditionLine(Section sec, RuleComplex rc, Session
	// session,
	// KnowWEUserContext user) {
	//
	// StringBuffer buffi = new StringBuffer();
	//
	// KnowWEObjectType type;
	// for (Section child : sec.getChildren()) {
	// type = child.getObjectType();
	// if (type instanceof Finding || type instanceof ComplexFinding
	// || type instanceof ComplexFindingBraced || type instanceof Conjunct
	// || type instanceof Disjunct)
	// buffi.append(this.highlightCondition(child, rc, session, user));
	// else
	// buffi.append(DelegateRenderer.getInstance().render(child, user));
	// }
	//
	// return buffi.toString();
	// }

	private void renderConditionLine(KnowWEArticle article, Section sec,
			Rule rc, Session session, KnowWEUserContext user,
			StringBuilder buffi) {

		KnowWEObjectType type;
		List<Section> children = sec.getChildren();
		for (Section child : children) {
			type = child.getObjectType();
			if (type instanceof Finding || type instanceof ComplexFinding
					|| type instanceof ComplexFindingBraced
					|| type instanceof Conjunct || type instanceof Disjunct) buffi.append(this.highlightCondition(
					article, child, rc,
					session, user));
			else {
				StringBuilder b = new StringBuilder();
				DelegateRenderer.getInstance().render(article, child, user, b);
				buffi.append(b.toString());
			}
		}
	}

	/**
	 * Renders the Condition with highlighting.
	 *
	 * @param sec
	 * @param rc
	 * @param session
	 * @return
	 */
	private String highlightCondition(KnowWEArticle article, Section sec,
			Rule rc, Session session, KnowWEUserContext user) {

		StringBuilder buffi = new StringBuilder();
		boolean braced = false;
		if (sec.getObjectType() instanceof ComplexFindingBraced) {
			braced = true;
			buffi.append("(");
			sec = (Section) sec.getChildren().get(1);
		}

		try {
			if (rc.getCondition().eval(session)) FontColorBackgroundRenderer.getRenderer(
					FontColorRenderer.COLOR5, "#33FF33").render(article,
					sec, user, buffi);
			else FontColorBackgroundRenderer.getRenderer(
					FontColorRenderer.COLOR5, "#FF9900").render(article,
					sec, user, buffi);
		}
		catch (NoAnswerException e) {
			FontColorBackgroundRenderer.getRenderer(FontColorRenderer.COLOR5,
					null).render(article, sec, user, buffi);
		}
		catch (UnknownAnswerException e) {
			FontColorBackgroundRenderer.getRenderer(FontColorRenderer.COLOR5,
					null).render(article, sec, user, buffi);
		}
		if (braced)
			buffi.append(")");

		return buffi.toString();
	}
}