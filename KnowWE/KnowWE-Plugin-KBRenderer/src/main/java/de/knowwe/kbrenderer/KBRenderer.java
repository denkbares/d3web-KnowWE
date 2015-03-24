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
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.inference.condition.Condition;
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
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.strings.Identifier;
import de.d3web.we.utils.D3webUtils;
import de.d3web.xcl.XCLModel;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.XCLRelationType;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.taghandler.AbstractHTMLTagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.kbrenderer.verbalizer.VerbalizationManager;
import de.knowwe.kbrenderer.verbalizer.Verbalizer;

public class KBRenderer extends AbstractHTMLTagHandler {

	private Map<Rule, String> renderedRulesCache = new HashMap<Rule, String>();

	private final static String KB_NAME = "kbname";

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
	public void renderHTML(String web, String topic,
						   UserContext user, Map<String, String> values, RenderResult result) {
		renderedRulesCache = new HashMap<Rule, String>();
		KnowledgeBase kb = null;
		if (values.containsKey(KB_NAME)) {
			kb = D3webUtils.getKnowledgeBase(web, values.get(KB_NAME));
		}
		else {
			kb = D3webUtils.getKnowledgeBase(web, topic);
		}
		renderHTML(web, topic, user, kb, result);
	}

	public void renderHTML(String web, String topic, UserContext user, KnowledgeBase kb, RenderResult text) {

		ResourceBundle rb = D3webUtils.getD3webBundle(user);

		text.appendHtml(
				"<div id=\"knowledge-panel\" class=\"panel\"><h3>"
						+ rb.getString("KnowWE.KBRenderer.header") + "</h3>\n\n");
		text.appendHtml("<div>");
		text.appendHtml("<p>");
		if (kb != null) {

			/*
			 * Render Solutions
			 */

			Solution diagnosis = kb.getRootSolution();
			boolean appendedSolutionsHeadline = false;
			if (diagnosis.getName().equals("P000")) {
				if (diagnosis.getChildren().length > 0) {
					text.appendHtml("<strong>"
							+ rb.getString("KnowWE.KBRenderer.solutions")
							+ "(" + kb.getManager().getSolutions().size()
							+ "):</strong><p></p>\n\n");
					appendedSolutionsHeadline = true;
				}
				// Get all Children
				TerminologyObject[] getRoots = diagnosis.getChildren();
				for (TerminologyObject t1 : getRoots) {
					// Look for children @ depth 1
					if (t1.getParents().length == 1) {
						text.appendHtml("<span style=\"color: rgb(150, 110, 120);\">");
						text.append(t1.getName());
						text.appendHtml("</span><br/>\n");
						// Get their childrens and build up the tree recursively
						text.append(getAll(t1.getChildren(), 1, topic, user));
						text.appendHtml("<br/>\n");
					}
				}
			}
			text.appendHtml("<p></p>");

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
					}
				}
			}

			if (duplRules.size() > 0) {
				if (!appendedRulesHeadline) {
					if (appendedSolutionsHeadline) {
						text.appendHtml("<br/>\n");
					}
					text.appendHtml("<strong>"
							+ rb.getString("KnowWE.KBRenderer.rules")
							+ "(" + duplRules.size() + "):</strong><p></p>\n\n");
					appendedRulesHeadline = true;
				}

			}

			// List with all Rules (no duplicates)
			List<de.d3web.core.inference.Rule> sort = new ArrayList<de.d3web.core.inference.Rule>(
					duplRules);
			// Sort rules
			Collections.sort(sort, new RuleComparator(user));
			for (de.d3web.core.inference.Rule r : sort) {
				text.append(renderRule(r, parameterMap, user));
			}
			text.appendHtml("<p></p>\n");
			renderedRulesCache = new HashMap<Rule, String>();

			/*
			 * Render DiaFlux Models
			 */
			FlowSet flowSet = DiaFluxUtils.getFlowSet(kb);
			if (flowSet != null && !flowSet.isEmpty()) {
				RenderResult bob = new RenderResult(text);
				int totalEdgeCount = 0, totalNodeCount = 0;
				for (Flow flow : flowSet) {
					int nodeCount = flow.getNodes().size();
					int edgeCount = flow.getEdges().size();
					totalNodeCount += nodeCount;
					totalEdgeCount += edgeCount;
					bob.append("Flow: '" + flow.getName() + "' (" + nodeCount + " Nodes, "
							+ edgeCount + " Edges)");
					bob.appendHtml("<br>");
				}
				text.appendHtml("<p><strong>DiaFlux (" + flowSet.size() + " Flows, "
						+ totalNodeCount
						+ " Nodes, " + totalEdgeCount + " Edges):</strong><p></p>\n\n");
				text.append(bob);
			}

			/*
			 * Questions
			 */
			List<QContainer> questions = new ArrayList<QContainer>(kb.getManager().getQContainers());
			KnowledgeBaseUtils.sortTerminologyObjects(questions);
			boolean appendedQuestionHeadline = false;
			for (QContainer q1 : questions) {
				if (q1.getName() != null && !q1.getName().equals("Q000")) {

					if (!appendedQuestionHeadline) {
						if (appendedSolutionsHeadline || appendedRulesHeadline) {
							text.appendHtml("<br/>\n");
						}
						text.appendHtml("<strong>"
								+ rb.getString("KnowWE.KBRenderer.questions")
								+ "(" + kb.getManager().getQuestions().size()
								+ "):</strong><br/><p></p>");
						appendedQuestionHeadline = true;
					}
					if (q1 instanceof QContainer) {
						text.appendHtml("<span style=\"color: rgb(128, 128, 0);\">");
						text.append(q1.getName());
						text.appendHtml("</span><br/>");
						// Build up question tree recursively
						text.append(getAll(q1.getChildren(), 1, topic, user));
						text.appendHtml("<br/>\n");
					}
				}
			}
			text.appendHtml("<p></p>");

			// Covering List
			Collection<XCLModel> xclRels = kb.getAllKnowledgeSlicesFor(XCLModel.KNOWLEDGE_KIND);
			boolean appendedXCLHeadline = false;
			for (XCLModel model : xclRels) {
				if (!appendedXCLHeadline) {
					if (appendedSolutionsHeadline || appendedRulesHeadline
							|| appendedQuestionHeadline) {
						text.appendHtml("<br/>\n");
					}
					text.appendHtml("<strong>"
							+ rb.getString("KnowWE.KBRenderer.xclModels")
							+ ":</strong>");
					appendedXCLHeadline = true;
				}

				text.appendHtml("<p></p>\n\n");
				text.append(model.getSolution().getName());
				text.appendHtml(":<br/>\n");

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
						text.appendHtml(" <br/>\n");
					}
				}
			}
		}
		else {
			text.appendHtml("<p class=\"box error\">"
					+ rb.getString("KnowWE.KBRenderer.error") + "</p>");
		}

		text.appendHtml("</div></div>");
	}

	private String renderRule(Rule r, Map<String, Object> parameterMap, UserContext user) {
		RenderResult result = new RenderResult(user);
		String renderedRule = renderedRulesCache.get(r);
		if (renderedRule == null) {

			RenderResult text = new RenderResult(result);
			text.append("Rule: " + r.getCondition());
			if (r.getException() != null) {
				text.append(" EXCEPT ");
				text.append(r.getException());
			}
			text.append(" THEN ");
			text.append(r.getAction());

			text.appendHtml("<br/>\n");

			renderedRule = text.toStringRaw();
			renderedRulesCache.put(r, renderedRule);
		}

		result.append(renderedRule);
		return result.toStringRaw();
	}

	/**
	 * Returns all children in an hierarchically view from a given list of
	 * terminology objects.
	 *
	 * @param nodes the nodes from which you want to have all children
	 *              displayed.
	 * @param save  List for saving already visited nodes, to avoid an infinite
	 *              loop in case of periodic appearing objects
	 * @param depth the depth of the recursion, which is needed for the
	 *              hierarchically view.
	 * @return all children from the given objects, including their properties.
	 */
	private String getAll(TerminologyObject[] nodes,
						  ArrayList<TerminologyObject> save, int depth, String title, UserContext user) {
		RenderResult result = new RenderResult(user);
		StringBuffer prompt = new StringBuffer();
		StringBuffer property = new StringBuffer();
		for (TerminologyObject t1 : nodes) {
			if (t1 != null
					&& (t1).getInfoStore() != null) {
				// Append the prompt for questions
				String longname = t1.getInfoStore().getValue(MMInfo.PROMPT);
				if (t1 instanceof Question && longname != null) {
					prompt.append("&#126; ").append(longname);
				}
				// Append the properties
				String unit = t1.getInfoStore().getValue(MMInfo.UNIT);
				if (unit != null) {
					property.append("{").append(unit).append("}");
				}
			}
			for (int i = 0; i < depth; i++) {
				result.append("-");
			}
			if (t1 instanceof QuestionChoice) {
				if (t1 instanceof QuestionMC) {
					result.append(getTermHTML(prompt, property, t1, "[mc]", title, user));
				}
				if (t1 instanceof QuestionYN) {
					result.append(getTermHTML(prompt, property, t1, "[yn]", title, user));
				}
				else {
					result.append(getTermHTML(prompt, property, t1, "[oc]", title, user));
				}
				for (Choice c1 : ((QuestionChoice) t1).getAllAlternatives()) {
					for (int i = 0; i < depth + 1; i++) {
						result.append("-");
					}
					result.appendHtml("<span style=\"color: rgb(0, 0, 255);\">");
					result.append(c1.toString());
					result.appendHtml("</span><br/>\n");
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
				result.appendHtml("<span style=\"color: rgb(150, 110, 120);\">");
				result.append(VerbalizationManager.getInstance().verbalize(t1,
						VerbalizationManager.RenderingFormat.HTML));
				result.appendHtml("</span><br/>\n");
			}
			else if (t1 instanceof QContainer) {
				result.appendHtml("<span style=\"color: rgb(128, 128, 0);\">");
				result.append(t1.getName());
				result.appendHtml("</span><br/>");
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
		return result.toStringRaw();
	}

	private String getTermHTML(StringBuffer prompt, StringBuffer property, TerminologyObject t1, String typeDeclaration, String title, UserContext user) {
		Environment env = Environment.getInstance();
		TerminologyManager manager = env.getTerminologyManager(user.getWeb(), title);
		Section<?> termDefiningSection = manager.getTermDefiningSection(new Identifier(t1.getName()));
		RenderResult builder = new RenderResult(user);

		if (termDefiningSection == null) {
			builder.append(t1.getName());
		}
		else {
			termDefiningSection.get().getRenderer().render(termDefiningSection, user, builder);
		}
		builder.appendHtml("<span style=\"color: rgb(125, 80, 102);\"> ");
		builder.append(typeDeclaration + " " + property);
		builder.appendHtml(" </span><br/>\n");

		return builder.toStringRaw();
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

		private final UserContext context;

		public RuleComparator(UserContext context) {
			this.context = context;
		}

		@Override
		public int compare(Rule o1, Rule o2) {
			return renderRule(o1, parameterMap, context).compareTo(
					renderRule(o2, parameterMap, context));
		}
	}

}
