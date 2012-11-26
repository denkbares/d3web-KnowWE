package de.knowwe.kbrenderer;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.knowledge.terminology.info.Property.Autosave;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.utilities.Triple;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.we.utils.D3webUtils;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.XCLRelationType;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.taghandler.AbstractHTMLTagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.kbrenderer.verbalizer.VerbalizationManager;
import de.knowwe.kbrenderer.verbalizer.Verbalizer;

public class KBRenderer extends AbstractHTMLTagHandler {

	private Map<Rule, String> renderedRulesCache = new HashMap<Rule, String>();

	private final Map<String, Object> parameterMap = new HashMap<String, Object>();

	public KBRenderer() {
		super("renderKnowledge");
		this.setMaskJSPWikiSyntax(true);
		parameterMap.put(Verbalizer.IS_SINGLE_LINE, Boolean.TRUE);
	}

	@Override
	public String getDescription(UserContext user) {
		return D3webUtils.getD3webBundle(user).getString(
				"KnowWE.KBRenderer.description");
	}

	@Override
	public String renderHTML(String topic, UserContext user,
			Map<String, String> values, String web) {
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(web, topic);
		return renderHTML(web, topic, user, kb);
	}

	public String renderHTML(String web, String topic, UserContext user, KnowledgeBase kb) {

		ResourceBundle rb = D3webUtils.getD3webBundle(user);

		StringBuilder text = new StringBuilder(
				"<div id=\"knowledge-panel\" class=\"panel\"><h3>"
						+ rb.getString("KnowWE.KBRenderer.header") + "</h3>\n\n");
		text.append("<div>");
		text.append("<p>");
		if (kb != null) {

			/*
			 * Render Solutions
			 */

			Solution diagnosis = kb.getRootSolution();
			boolean appendedSolutionsHeadline = false;
			if (diagnosis.getName().equals("P000")) {
				if (diagnosis.getChildren().length > 0) {
					text.append("<strong>"
							+ rb.getString("KnowWE.KBRenderer.solutions")
							+ ":</strong><p></p>\n\n");
					appendedSolutionsHeadline = true;
				}
				// Get all Children
				TerminologyObject[] getRoots = diagnosis.getChildren();
				for (TerminologyObject t1 : getRoots) {
					// Look for children @ depth 1
					if (t1.getParents().length == 1) {
						text.append("<span style=\"color: rgb(150, 110, 120);\">"
								+ t1.getName() + "</span><br/>\n");
						// Get their childrens and build up the tree recursively
						text.append(getAll(t1.getChildren(), 1, topic, user));
						text.append("<br/>\n");
					}
				}
			}
			text.append("<p></p>");

			/*
			 * Render Rules
			 */
			List<KnowledgeSlice> rules = new ArrayList<KnowledgeSlice>(kb
					.getAllKnowledgeSlices());

			boolean appendedRulesHeadline = false;
			// HashSet for checking duplicate rules
			HashSet<de.d3web.core.inference.Rule> duplRules = new HashSet<de.d3web.core.inference.Rule>();
			for (KnowledgeSlice knowledgeSlice : rules) {
				if (knowledgeSlice instanceof RuleSet) {
					RuleSet rs = (RuleSet) knowledgeSlice;
					for (de.d3web.core.inference.Rule r : rs.getRules()) {
						// add every rule once
						duplRules.add(r);
						if (!appendedRulesHeadline) {
							if (appendedSolutionsHeadline) {
								text.append("<br/>\n");
							}
							text.append("<strong>"
									+ rb.getString("KnowWE.KBRenderer.rules")
									+ ":</strong><p></p>\n\n");
							appendedRulesHeadline = true;
						}
					}
				}
			}
			// List with all Rules (no duplicates)
			List<de.d3web.core.inference.Rule> sort = new ArrayList<de.d3web.core.inference.Rule>(
					duplRules);
			// Sort rules
			Collections.sort(sort, new RuleComparator());
			for (de.d3web.core.inference.Rule r : sort) {
				text.append(renderRule(r, parameterMap));
			}
			text.append("<p></p>\n");
			renderedRulesCache = new HashMap<Rule, String>();

			/*
			 * Render DiaFlux Models
			 */
			List<Flow> flows = kb.getManager().getObjects(Flow.class);
			if (!flows.isEmpty()) {
				StringBuilder bob = new StringBuilder();
				int totalEdgeCount = 0, totalNodeCount = 0;
				for (Flow flow : flows) {
					int nodeCount = flow.getNodes().size();
					int edgeCount = flow.getEdges().size();
					totalNodeCount += nodeCount;
					totalEdgeCount += edgeCount;
					bob.append("Flow: '" + flow.getName() + "' (" + nodeCount + " Nodes, "
							+ edgeCount + " Edges)<br>");
				}
				text.append("<p><strong>DiaFlux (" + flows.size() + " Flows, " + totalNodeCount
						+ " Nodes, " + totalEdgeCount + " Edges):</strong><p></p>\n\n");
				text.append(bob);
			}

			/*
			 * Questions
			 */
			List<QContainer> questions = new ArrayList<QContainer>(kb.getManager().getQContainers());
			KnowledgeBaseUtils.sortQContainers(questions);
			boolean appendedQuestionHeadline = false;
			for (QContainer q1 : questions) {
				if (q1.getName() != null && !q1.getName().equals("Q000")) {

					if (!appendedQuestionHeadline) {
						if (appendedSolutionsHeadline || appendedRulesHeadline) {
							text.append("<br/>\n");
						}
						text.append("<strong>"
								+ rb.getString("KnowWE.KBRenderer.questions")
								+ ":</strong><br/><p></p>");
						appendedQuestionHeadline = true;
					}
					if (q1 instanceof QContainer) {
						text.append("<span style=\"color: rgb(128, 128, 0);\">"
								+ q1.getName() + "</span><br/>");
						// Build up question tree recursively
						text.append(getAll(q1.getChildren(), 1, topic, user));
						text.append("<br/>\n");
					}
				}
			}
			text.append("<p></p>");

			// Covering List
			Collection<XCLModel> xclRels = kb.getAllKnowledgeSlicesFor(XCLModel.KNOWLEDGE_KIND);
			boolean appendedXCLHeadline = false;
			for (XCLModel model : xclRels) {
				if (!appendedXCLHeadline) {
					if (appendedSolutionsHeadline || appendedRulesHeadline
							|| appendedQuestionHeadline) {
						text.append("<br/>\n");
					}
					text.append("<strong>"
							+ rb.getString("KnowWE.KBRenderer.xclModels")
							+ ":</strong>");
					appendedXCLHeadline = true;
				}

				text.append("<p></p>\n\n" + model.getSolution().getName()
						+ ":<br/>\n");

				Map<XCLRelationType, Collection<XCLRelation>> relationMap = model
						.getTypedRelations();

				for (Entry<XCLRelationType, Collection<XCLRelation>> entry : relationMap
						.entrySet()) {
					XCLRelationType type = entry.getKey();
					Collection<XCLRelation> relations = entry.getValue();
					for (XCLRelation rel : relations) {
						Condition cond = rel.getConditionedFinding();
						String weight = "";
						if (type == XCLRelationType.explains) {
							weight = "[" + rel.getWeight() + "]";
						}

						text.append(type.getName() + weight + ": ");
						text.append("&nbsp;&nbsp;&nbsp;"
								+ VerbalizationManager.getInstance()
										.verbalize(
												cond,
												VerbalizationManager.RenderingFormat.PLAIN_TEXT,
												parameterMap));
						text.append(" <br/>\n");
					}
				}
			}
		}
		else {
			text.append("<p class=\"box error\">"
					+ rb.getString("KnowWE.KBRenderer.error") + "</p>");
		}

		return text.append("</p></div></div>").toString();
	}

	private String renderRule(Rule r, Map<String, Object> parameterMap) {
		String renderedRule = renderedRulesCache.get(r);
		if (renderedRule != null) return renderedRule;

		StringBuilder text = new StringBuilder();
		text.append("Rule: "
				+ VerbalizationManager.getInstance().verbalize(
						r.getCondition(), VerbalizationManager.RenderingFormat.PLAIN_TEXT));
		text.append(" --> ");
		text.append(VerbalizationManager.getInstance().verbalize(
				r.getAction(), VerbalizationManager.RenderingFormat.HTML, parameterMap));

		if (r.getException() != null) {
			text.append(" EXCEPT ");
			text.append(VerbalizationManager.getInstance().verbalize(
					r.getException(), VerbalizationManager.RenderingFormat.PLAIN_TEXT, parameterMap));
		}
		text.append("<br/>\n");

		renderedRule = text.toString();
		renderedRulesCache.put(r, renderedRule);

		return renderedRule;
	}

	/**
	 * 
	 * Returns all children in an hierarchically view from a given list of
	 * terminology objects.
	 * 
	 * @param nodes the nodes from which you want to have all children
	 *        displayed.
	 * @param save List for saving already visited nodes, to avoid an infinite
	 *        loop in case of periodic appearing objects
	 * @param depth the depth of the recursion, which is needed for the
	 *        hierarchically view.
	 * @return all children from the given objects, including their properties.
	 */
	private String getAll(TerminologyObject[] nodes,
			ArrayList<TerminologyObject> save, int depth, String title, UserContext user) {
		StringBuffer result = new StringBuffer();
		StringBuffer prompt = new StringBuffer();
		StringBuffer property = new StringBuffer();
		for (TerminologyObject t1 : nodes) {
			if (t1 instanceof TerminologyObject
					&& (t1).getInfoStore() != null) {
				// Append the prompt for questions
				String longname = t1.getInfoStore().getValue(MMInfo.PROMPT);
				if (t1 instanceof Question && longname != null) {
					prompt.append("&#126; " + longname);
				}
				// Append the properties
				InfoStore rUnit = t1.getInfoStore();
				for (Triple<Property<?>, Locale, Object> p1 : rUnit.entries()) {
					if (p1.getA().hasState(Autosave.mminfo)) {
						property.append(" " + p1.getA().getName() + ": "
								+ rUnit.getClass());
					}
				}
			}
			for (int i = 0; i < depth; i++) {
				result.append("-");
			}
			if (t1 instanceof QuestionChoice) {
				if (t1 instanceof QuestionMC) {
					result.append(getTermHTML(prompt, property, t1, "[mc]", title, user));
				}
				else {
					result.append(getTermHTML(prompt, property, t1, "[oc]", title, user));
				}
				for (Choice c1 : ((QuestionChoice) t1).getAllAlternatives()) {
					for (int i = 0; i < depth + 1; i++) {
						result.append("-");
					}
					result.append("<span style=\"color: rgb(0, 0, 255);\">"
							+ c1.toString() + "</span><br/>\n");
				}
			}
			else if (t1 instanceof QuestionText) {
				result.append(getTermHTML(prompt, property, t1, "[text]", title, user));
			}
			else if (t1 instanceof QuestionNum) {
				result.append(getTermHTML(prompt, property, t1, "[num]", title, user));
			}
			else if (t1 instanceof QuestionDate) {
				result.append(getTermHTML(prompt, property, t1, "[date]", title, user));
			}
			else if (t1 instanceof Solution) {
				result.append("<span style=\"color: rgb(150, 110, 120);\">"
						+ VerbalizationManager.getInstance().verbalize(t1,
								VerbalizationManager.RenderingFormat.HTML) + "</span><br/>\n");
			}
			else if (t1 instanceof QContainer) {
				result.append("<span style=\"color: rgb(128, 128, 0);\">"
						+ t1.getName() + "</span><br/>");
			}
			// Reset the prompt & property buffer for every object
			prompt = new StringBuffer();
			property = new StringBuffer();
			if (t1.getChildren().length > 0 && !save.contains(t1)) {
				save.add(t1);
				depth++;
				result.append(getAll(t1.getChildren(), save, depth, title, user));
				depth--;
			}
		}
		return result.toString();
	}

	private String getTermHTML(StringBuffer prompt, StringBuffer property, TerminologyObject t1, String typeDeclaration, String title, UserContext user) {
		TerminologyManager terminologyManager = Environment.getInstance().getTerminologyManager(
				Environment.DEFAULT_WEB, title);
		Section<?> termDefiningSection = terminologyManager.getTermDefiningSection(new TermIdentifier(
				t1.getName()));
		StringBuilder builder = new StringBuilder();

		termDefiningSection.get().getRenderer().render(termDefiningSection, user, builder);
		return builder.toString() + "<span style=\"color: rgb(125, 80, 102);\"> " + typeDeclaration
				+ " "
				+ property + " </span><br/>\n";
	}

	/**
	 * See above.
	 * 
	 * @param nodes
	 * @param depth
	 * @return String
	 */
	private String getAll(TerminologyObject[] nodes, int depth, String title, UserContext user) {
		return getAll(nodes, new ArrayList<TerminologyObject>(), depth, title, user);
	}

	private class RuleComparator implements Comparator<Rule> {

		@Override
		public int compare(Rule o1, Rule o2) {
			return renderRule(o1, parameterMap).compareTo(renderRule(o2, parameterMap));
		}

	}

}
