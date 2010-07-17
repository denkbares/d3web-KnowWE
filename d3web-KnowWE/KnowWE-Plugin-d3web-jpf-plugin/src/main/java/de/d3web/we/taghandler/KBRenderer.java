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

package de.d3web.we.taghandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Map.Entry;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.knowledge.terminology.info.MMInfoSubject;
import de.d3web.core.knowledge.terminology.info.Properties;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.Choice;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.kernel.verbalizer.Verbalizer;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.bulletLists.BulletContentType;
import de.d3web.we.kdom.rules.Rule;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.xcl.XCLRelation;
import de.d3web.xcl.XCLRelationType;
import de.d3web.xcl.inference.PSMethodXCL;

public class KBRenderer extends AbstractTagHandler {

	public KBRenderer() {
		super("renderKnowledge");
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.KBRenderer.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		D3webKnowledgeService service = D3webModule.getAD3webKnowledgeServiceInTopic(web, topic);

		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user);

		StringBuilder text = new StringBuilder("<div id=\"knowledge-panel\" class=\"panel\"><h3>"
				+ rb.getString("KnowWE.KBRenderer.header") + "</h3>");
		text.append("<div>");
		text.append("<p>");
		if (service != null) {
			// text.append("<h4>Knowledge of article:</h4>";
			KnowledgeBase kb = service.getBase();

			// kb.getAllKnowledgeSlices(); // Actions in Verbalizer schmeißen
			// (nach
			// // ProblemSolverMethod sortieren)
			// kb.getQContainers(); // Durchlaufen und schaun ob weiterer
			// Container

			// Solutions
			List<Solution> diagnosis = kb.getSolutions();

			boolean appendedSolutionsHeadline = false;
			for (Solution diagnosis2 : diagnosis) {
				if (!diagnosis2.getName().equals("P000")) {
					if (!appendedSolutionsHeadline) {
						text.append("<strong>" + rb.getString("KnowWE.KBRenderer.solutions")
								+ ":</strong><p/>");
						appendedSolutionsHeadline = true;
					}
					text.append(VerbalizationManager.getInstance().verbalize(
							diagnosis2, RenderingFormat.HTML) + "<br/>");
				}
			}
			text.append("<p/>");

			// text.append("<br /><b>SCRelations: </b><br />");
			//
			// Collection<KnowledgeSlice> scRels = kb
			// .getAllKnowledgeSlicesFor(PSMethodSetCovering.class);
			// for (KnowledgeSlice knowledgeSlice : scRels) {
			// if (knowledgeSlice instanceof SCRelation) {
			// SCRelation screl = ((SCRelation) knowledgeSlice);
			// SCNode targetNode = screl.getTargetNode();
			// if (targetNode instanceof PredictedFinding) {
			// PredictedFinding finding = ((PredictedFinding) targetNode);
			// AbstractCondition cond = finding.getCondition();
			// // Verbalization of the condition
			// text.append(VerbalizationManager.getInstance().verbalize(
			// cond, RenderingFormat.HTML)
			// + "<br>");
			// }
			//
			// screl.getTargetNode().getNamedObject();
			// }
			// }

			// Rules
			// TODO: Fix
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(Verbalizer.IS_SINGLE_LINE, Boolean.TRUE);
			Collection<KnowledgeSlice> rules = kb
					.getAllKnowledgeSlices();

			boolean appendedRulesHeadline = false;
			Map<String, String> idMap = new HashMap<String, String>();
			for (KnowledgeSlice knowledgeSlice : rules) {
				if (knowledgeSlice instanceof RuleSet) {
					RuleSet rs = (RuleSet) knowledgeSlice;
					for (de.d3web.core.inference.Rule r : rs.getRules()) {
						if (!appendedRulesHeadline) {
							if (appendedSolutionsHeadline) {
								text.append("<br/>");
							}
							text.append("<strong>" + rb.getString("KnowWE.KBRenderer.rules")
									+ ":</strong><p/>");
							appendedRulesHeadline = true;
							List<Section<Rule>> allRules = new ArrayList<Section<de.d3web.we.kdom.rules.Rule>>();
							List<Section<BulletContentType>> allBulletContentTypes = new ArrayList<Section<BulletContentType>>();

							KnowWEEnvironment.getInstance().getArticleManager(web).getArticle(topic)
									.getSection().findSuccessorsOfType(BulletContentType.class,
											allBulletContentTypes);
							KnowWEEnvironment.getInstance().getArticleManager(web).getArticle(topic)
									.getSection().findSuccessorsOfType(Rule.class, allRules);
							for (Section<Rule> rule : allRules) {
								String kbRuleId = (String) KnowWEUtils.getStoredObject(
										rule.getWeb(), topic,
										rule.getID(), de.d3web.we.kdom.rules.Rule.KBID_KEY);
								idMap.put(kbRuleId, rule.getID());
							}

							for (Section<BulletContentType> bullet : allBulletContentTypes) {
								String kbRuleId = (String) KnowWEUtils.getStoredObject(
										bullet.getWeb(), topic,
										bullet.getID(), de.d3web.we.kdom.rules.Rule.KBID_KEY);
								idMap.put(kbRuleId, bullet.getID());
							}
						}
						String kdomid = idMap.get(r.getId());
						if (kdomid != null) {
							String button = ("<img src=KnowWEExtension/images/page_white_find.png "
									+ "class=\"highlight-rule\" "
									+ "rel=\"{kdomid: '" + kdomid + "', topic: '" + topic
									+ "', depth: 0, breadth: 0}\""
									+ "/></img>");
							text.append(button);
						}

						text.append("Rule: " + VerbalizationManager.getInstance().verbalize(
								r.getCondition(), RenderingFormat.PLAIN_TEXT));
						text.append(" --> ");
						text.append(VerbalizationManager.getInstance().verbalize(
								r.getAction(), RenderingFormat.HTML, parameterMap));
						text.append("\n <br />"); // \n only to avoid hmtl-code
						// being cut by JspWiki
						// (String.length > 10000)
					}
					text.append("Slices:<br />");
				}
			}
			text.append("<p/>");

			// Questions
			KnowledgeBaseManagement kbm = KnowledgeBaseManagement
					.createInstance(kb);
			List<QContainer> questions = kb.getQContainers();
			kbm.sortQContainers(questions);
			boolean appendedQuestionHeadline = false;
			for (QContainer q1 : questions) {
				if (!q1.getName().equals("Q000")) {
					if (!appendedQuestionHeadline) {
						if (appendedSolutionsHeadline || appendedRulesHeadline) {
							text.append("<br/>");
						}
						text.append("<strong>" + rb.getString("KnowWE.KBRenderer.questions")
								+ ":</strong><p/>");
						appendedQuestionHeadline = true;
					}
					if (q1 instanceof QContainer) {
						text.append("<span style=\"color: rgb(128, 128, 0);\">" + q1.getName()
								+ "</span><br/>");
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
						text.append("<strong>" + rb.getString("KnowWE.KBRenderer.xclModels")
								+ ":</strong>");
						appendedXCLHeadline = true;
					}
					de.d3web.xcl.XCLModel model = ((de.d3web.xcl.XCLModel) slice);

					// adds tresholds if different from default
					// String thresholds = "";
					// if (model.getEstablishedThreshold() !=
					// XCLModel.defaultEstablishedThreshold ||
					// model.getSuggestedThreshold() !=
					// XCLModel.defaultSuggestedThreshold ||
					// model.getMinSupport() != XCLModel.defaultMinSupport) {
					// thresholds = " [" + model.getSuggestedThreshold() + ", "
					// + model.getEstablishedThreshold() + ", " +
					// model.getMinSupport()
					// + "]";
					//
					// }

					text.append("<p /> " + model.getSolution().getName()
							+ ": <br />");

					Map<XCLRelationType, Collection<XCLRelation>> relationMap = model
							.getTypedRelations();

					for (Entry<XCLRelationType, Collection<XCLRelation>> entry : relationMap.entrySet()) {
						XCLRelationType type = entry.getKey();
						Collection<XCLRelation> relations = entry.getValue();
						for (XCLRelation rel : relations) {
							Condition cond = rel
									.getConditionedFinding();
							String weight = "";
							String kdomid = rel.getKdmomID();
							if (type == XCLRelationType.explains) {
								weight = "[" + rel.getWeight() + "]";
							}

							if (kdomid != null) {
								String button = ("<img src=\"KnowWEExtension/images/page_white_find.png\" "
										+ "class=\"highlight-xcl-relation\" "
										+ "rel=\"{kdomid: '"
										+ kdomid
										+ "', topic: '"
										+ topic
										+ "', depth: 0, breadth: 0}\""
										+ "/></img>");
								text.append(button);
							}

							text.append(type.getName() + weight + ": ");
							text.append("&nbsp;&nbsp;&nbsp;"
									+ VerbalizationManager.getInstance()
											.verbalize(cond,
													RenderingFormat.PLAIN_TEXT, parameterMap));

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
			text.append("<p class=\"box error\">" + rb.getString("KnowWE.KBRenderer.error")
					+ "</p>");
		}
		return text.append("</p></div></div>").toString();
	}

	private String getAll(TerminologyObject[] nodes, int depth) {
		StringBuffer result = new StringBuffer();
		StringBuffer properties = new StringBuffer();
		StringBuffer range = new StringBuffer();
		for (TerminologyObject t1 : nodes) {
			if (t1 instanceof NamedObject
					&& ((NamedObject) t1).getProperties() != null) {
				if (t1 instanceof Question && getPrompt((Question) t1) != null) {
					properties.append("&#126; " + getPrompt((Question) t1));
				}
				Properties rUnit = ((NamedObject) t1).getProperties();
				Set<Property> sUnit = rUnit.getKeys();
				for (Property p1 : sUnit) {
					if (p1.getName() != "mminfo"
							&& p1.getName() != "abstractionQuestion") {
						range.append(" " + rUnit.getProperty(p1));
					}
				}
			}
			for (int i = 0; i < depth; i++) {
				result.append("-");
			}
			if (t1 instanceof QuestionChoice) {
				if (t1 instanceof QuestionMC) {
					result.append("<span style=\"color: rgb(0, 128, 0);\">"
							+ t1.toString() + " " + properties + " [mc] "
							+ range + "</span><br/>");
				}
				else {
					result.append("<span style=\"color: rgb(0, 128, 0);\">"
							+ t1.toString() + " " + properties + " [oc] "
							+ range + "</span><br/>");
				}
				for (Choice c1 : ((QuestionChoice) t1).getAllAlternatives()) {
					for (int i = 0; i < depth + 1; i++) {
						result.append("-");
					}
					result.append("<span style=\"color: rgb(0, 0, 255);\">"
							+ c1.toString()
							+ "</span><br/>");
				}
			}
			else if (t1 instanceof QuestionText) {
				result.append("<span style=\"color: rgb(0, 128, 0);\">"
						+ t1.getName() + " " + properties + " [text] " + range
						+ "</span><br/>");
			}
			else if (t1 instanceof QuestionNum) {
				result.append("<span style=\"color: rgb(0, 128, 0);\">"
						+ t1.getName() + " " + properties + " [num] " + range
						+ "</span><br/>");
			}
			else if (t1 instanceof QuestionDate) {
				result.append("<span style=\"color: rgb(0, 128, 0);\">"
						+ t1.getName() + " " + properties + " [date] " + range
						+ "</span><br/>");
			}
			properties = new StringBuffer();
			range = new StringBuffer();
			if (t1.getChildren().length > 0) {
				depth++;
				result.append(getAll(t1.getChildren(), depth));
				depth--;
			}
		}
		return result.toString();
	}

	public static String getPrompt(Question q) {
		MMInfoStorage storage = (MMInfoStorage) q.getProperties()
				.getProperty(Property.MMINFO);
		if (storage != null) {
			DCMarkup dcMarkup = new DCMarkup();
			dcMarkup.setContent(DCElement.SOURCE, q.getId());
			dcMarkup.setContent(DCElement.SUBJECT,
					MMInfoSubject.PROMPT.getName());
			Set<MMInfoObject> info = storage.getMMInfo(dcMarkup);

			if (info != null) {
				Iterator<MMInfoObject> iter = info.iterator();
				while (iter.hasNext()) {
					return iter.next().getContent();
				}
			}
		}
		return null;
	}
}
