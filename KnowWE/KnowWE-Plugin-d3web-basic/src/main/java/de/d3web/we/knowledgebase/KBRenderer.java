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

package de.d3web.we.knowledgebase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.NamedObject;
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
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.utilities.Triple;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.kernel.verbalizer.Verbalizer;
import de.d3web.we.basic.D3webKnowledgeHandler;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.taghandler.AbstractHTMLTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.XCLRelationType;
import de.d3web.xcl.inference.PSMethodXCL;

public class KBRenderer extends AbstractHTMLTagHandler {

	public KBRenderer() {
		super("renderKnowledge");
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString(
				"KnowWE.KBRenderer.description");
	}

	@Override
	public String renderHTML(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		D3webKnowledgeHandler knowledgeRepresentationHandler = D3webModule.getKnowledgeRepresentationHandler(KnowWEEnvironment.DEFAULT_WEB);
		KnowledgeBaseManagement knowledgeBaseManagement = knowledgeRepresentationHandler.getKBM(topic);
		KnowledgeBase kb = knowledgeBaseManagement.getKnowledgeBase();


		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user);

		StringBuilder text = new StringBuilder(
				"<div id=\"knowledge-panel\" class=\"panel\"><h3>"
						+ rb.getString("KnowWE.KBRenderer.header") + "</h3>");
		text.append("<div>");
		text.append("<p>");
		if (kb != null) {

			/*
			 * Render Solutions
			 */

			Solution diagnosis = kb.getRootSolution();
			boolean appendedSolutionsHeadline = false;
			if (diagnosis.getName().equals("P000")) {
				if (!appendedSolutionsHeadline) {
					text.append("<strong>"
							+ rb.getString("KnowWE.KBRenderer.solutions")
							+ ":</strong><p/>");
					appendedSolutionsHeadline = true;
				}
				// Get all Children
				TerminologyObject[] getRoots = diagnosis.getChildren();
				for (TerminologyObject t1 : getRoots) {
					// Look for children @ depth 1
					if (t1.getParents().length == 1) {
						text
								.append("<span style=\"color: rgb(150, 110, 120);\">"
										+ t1.getName() + "</span><br/>");
						// Get their childrens and build up the tree recursively
						text.append(getAll(t1.getChildren(), 1));
						text.append("<br/>");
					}
				}
			}
			text.append("<p/>");

			/*
			 * Render Rules
			 */

			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(Verbalizer.IS_SINGLE_LINE, Boolean.TRUE);
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
								text.append("<br/>");
							}
							text.append("<strong>"
									+ rb.getString("KnowWE.KBRenderer.rules")
									+ ":</strong><p/>");
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
			// TODO:Johannes: Highlighting does not work
			// due to strange JS Problems
			for (de.d3web.core.inference.Rule r : sort) {
				text.append("Rule: "
						+ VerbalizationManager.getInstance().verbalize(
								r.getCondition(), VerbalizationManager.RenderingFormat.PLAIN_TEXT));
				text.append(" --> ");
				text.append(VerbalizationManager.getInstance().verbalize(
						r.getAction(), VerbalizationManager.RenderingFormat.HTML, parameterMap));
				text.append("\n <br />"); // \n only to avoid hmtl-code
				// being cut by JspWiki
				// (String.length > 10000)
			}
			text.append("<p/>");

			/*
			 * Questions
			 */

			KnowledgeBaseManagement kbm = KnowledgeBaseManagement
					.createInstance(kb);
			List<QContainer> questions = kb.getQContainers();
			kbm.sortQContainers(questions);
			boolean appendedQuestionHeadline = false;
			for (QContainer q1 : questions) {
				if (q1.getName() != null && !q1.getName().equals("Q000")) {
					if (!appendedQuestionHeadline) {
						if (appendedSolutionsHeadline || appendedRulesHeadline) {
							text.append("<br/>");
						}
						text.append("<strong>"
								+ rb.getString("KnowWE.KBRenderer.questions")
								+ ":</strong><p/>");
						appendedQuestionHeadline = true;
					}
					if (q1 instanceof QContainer) {
						text.append("<span style=\"color: rgb(128, 128, 0);\">"
								+ q1.getName() + "</span><br/>");
						// Build up question tree recursively
						text.append(getAll(q1.getChildren(), 1));
						text.append("<br/>");
					}
				}
			}
			text.append("<p/>");

			// Covering List
			Collection<KnowledgeSlice> xclRels = kb
					.getAllKnowledgeSlicesFor(PSMethodXCL.class);
			boolean appendedXCLHeadline = false;
			for (KnowledgeSlice slice : xclRels) {
				if (slice instanceof de.d3web.xcl.XCLModel) {
					if (!appendedXCLHeadline) {
						if (appendedSolutionsHeadline || appendedRulesHeadline
								|| appendedQuestionHeadline) {
							text.append("<br/>");
						}
						text.append("<strong>"
								+ rb.getString("KnowWE.KBRenderer.xclModels")
								+ ":</strong>");
						appendedXCLHeadline = true;
					}
					de.d3web.xcl.XCLModel model = ((de.d3web.xcl.XCLModel) slice);

					text.append("<p /> " + model.getSolution().getName()
							+ ": <br />");

					Map<XCLRelationType, Collection<XCLRelation>> relationMap = model
							.getTypedRelations();

					for (Entry<XCLRelationType, Collection<XCLRelation>> entry : relationMap
							.entrySet()) {
						XCLRelationType type = entry.getKey();
						Collection<XCLRelation> relations = entry.getValue();
						for (XCLRelation rel : relations) {
							Condition cond = rel.getConditionedFinding();
							String weight = "";
							String kdomid = rel.getKdmomID();
							if (type == XCLRelationType.explains) {
								weight = "[" + rel.getWeight() + "]";
							}

							// TODO:Johannes: Highlighting does not work
							// due to strange JS Problems
							// if (kdomid != null) {
							// String button =
							// ("<img src=\"KnowWEExtension/images/page_white_find.png\" "
							// + "class=\"highlight-xcl-relation\" "
							// + "rel=\"{kdomid: '"
							// + kdomid
							// + "', topic: '"
							// + topic
							// + "', depth: 0, breadth: 0}\"" + "/></img>");
							// text.append(button);
							// }

							text.append(type.getName() + weight + ": ");
							text.append("&nbsp;&nbsp;&nbsp;"
									+ VerbalizationManager.getInstance()
											.verbalize(cond,
													VerbalizationManager.RenderingFormat.PLAIN_TEXT,
													parameterMap));

							boolean id = false;
							if (id) {
								text.append(" (ID: " + rel.getId() + ")");
							}

							// if (kdomid != null) {
							// String button = ("<input type='button' value='"
							// + "XCL-Generieren"
							// + "'"
							// +
							// " name='TiRexToXCL' class='button' onclick='highlightNode(\""
							// + kdomid + "\",\"" + topic + "\");'/>");
							// text += button;
							// }
							text.append(" \n <br />"); // \n only to avoid
							// hmtl-code being cut
							// by JspWiki
							// (String.length >
							// 10000)

						}
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
			ArrayList<TerminologyObject> save, int depth) {
		StringBuffer result = new StringBuffer();
		StringBuffer prompt = new StringBuffer();
		StringBuffer property = new StringBuffer();
		for (TerminologyObject t1 : nodes) {
			if (t1 instanceof NamedObject
					&& ((NamedObject) t1).getInfoStore() != null) {
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
					result
							.append("<span style=\"color: rgb(0, 128, 0);\">"
									+ t1.toString()
									+ " "
									+ prompt
									+ "</span>"
									+ "<span style=\"color: rgb(125, 80, 102);\"> [mc] "
									+ property + " </span><br/>");
				}
				else {
					result
							.append("<span style=\"color: rgb(0, 128, 0);\">"
									+ t1.toString()
									+ " "
									+ prompt
									+ "</span>"
									+ "<span style=\"color: rgb(125, 80, 102);\"> [oc] "
									+ property + " </span><br/>");
				}
				for (Choice c1 : ((QuestionChoice) t1).getAllAlternatives()) {
					for (int i = 0; i < depth + 1; i++) {
						result.append("-");
					}
					result.append("<span style=\"color: rgb(0, 0, 255);\">"
							+ c1.toString() + "</span><br/>");
				}
			}
			else if (t1 instanceof QuestionText) {
				result.append("<span style=\"color: rgb(0, 128, 0);\">"
						+ t1.getName() + " " + prompt + "</span>"
						+ "<span style=\"color: rgb(125, 80, 102);\"> [text] "
						+ property + " </span><br/>");
			}
			else if (t1 instanceof QuestionNum) {
				result.append("<span style=\"color: rgb(0, 128, 0);\">"
						+ t1.getName() + " " + prompt + "</span>"
						+ "<span style=\"color: rgb(125, 80, 102);\"> [num] "
						+ property + " </span><br/>");
			}
			else if (t1 instanceof QuestionDate) {
				result.append("<span style=\"color: rgb(0, 128, 0);\">"
						+ t1.getName() + " " + prompt
						+ "<span style=\"color: rgb(125, 80, 102);\"> [date] "
						+ property + " </span><br/>");
			}
			else if (t1 instanceof Solution) {
				result.append("<span style=\"color: rgb(150, 110, 120);\">"
						+ VerbalizationManager.getInstance().verbalize(t1,
								VerbalizationManager.RenderingFormat.HTML) + "</span><br/>");
			}
			// Reset the prompt & property buffer for every object
			prompt = new StringBuffer();
			property = new StringBuffer();
			if (t1.getChildren().length > 0 && !save.contains(t1)) {
				save.add(t1);
				depth++;
				result.append(getAll(t1.getChildren(), save, depth));
				depth--;
			}
		}
		return result.toString();
	}

	/**
	 * See above.
	 *
	 * @param nodes
	 * @param depth
	 * @return String
	 */
	private String getAll(TerminologyObject[] nodes, int depth) {
		return getAll(nodes, new ArrayList<TerminologyObject>(), depth);
	}

	private class RuleComparator implements
			Comparator<de.d3web.core.inference.Rule> {

		@Override
		public int compare(de.d3web.core.inference.Rule o1,
				de.d3web.core.inference.Rule o2) {
			try {
				Integer i1 = Integer.parseInt(o1.getId().substring(1));
				Integer i2 = Integer.parseInt(o2.getId().substring(1));
				return i1.compareTo(i2);
			}
			catch (NumberFormatException e) {
				// shouldn't happen, fallback...
				return o1.getId().compareTo(o2.getId());
			}
		}

	}

}
