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

package de.d3web.we.taghandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.Rule;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelation;
import de.d3web.kernel.psMethods.xclPattern.XCLRelationType;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.kernel.verbalizer.Verbalizer;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.bulletLists.BulletContentType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KBRenderer extends AbstractTagHandler {

	public KBRenderer() {
		super("renderKnowledge");		
	}
	
	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.KBRenderer.description");
	}
	
	@Override
	public String render(String topic, KnowWEUserContext user,Map<String,String> values, String web) {
		D3webKnowledgeService service = D3webModule.getAD3webKnowledgeServiceInTopic(web, topic);

		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user);
		
		StringBuilder text = new StringBuilder("<div id=\"knowledge-panel\" class=\"panel\"><h3>" + rb.getString("KnowWE.KBRenderer.header") + "</h3>");
		text.append("<div>");
		text.append("<p>");
		if (service != null) {
//			text.append("<h4>Knowledge of article:</h4>";
			KnowledgeBase kb = service.getBase();
			
			List<Diagnosis> diagnosis = kb.getDiagnoses();
			
			boolean appendedSolutionsHeadline = false;
			for (Diagnosis diagnosis2 : diagnosis) {
				if (!diagnosis2.getText().equals("P000")) {
					if (!appendedSolutionsHeadline) {
						text.append("<strong>" + rb.getString("KnowWE.KBRenderer.solutions") + ":</strong><p/>");
						appendedSolutionsHeadline = true;
					}
					text.append(VerbalizationManager.getInstance().verbalize(
							diagnosis2, RenderingFormat.HTML) + "<br/>");
				}
			}
//			text.append("<br /><b>SCRelations: </b><br />");
//
//			Collection<KnowledgeSlice> scRels = kb
//					.getAllKnowledgeSlicesFor(PSMethodSetCovering.class);
//			for (KnowledgeSlice knowledgeSlice : scRels) {
//				if (knowledgeSlice instanceof SCRelation) {
//					SCRelation screl = ((SCRelation) knowledgeSlice);
//					SCNode targetNode = screl.getTargetNode();
//					if (targetNode instanceof PredictedFinding) {
//						PredictedFinding finding = ((PredictedFinding) targetNode);
//						AbstractCondition cond = finding.getCondition();
//						// Verbalization of the condition
//						text.append(VerbalizationManager.getInstance().verbalize(
//								cond, RenderingFormat.HTML)
//								+ "<br>");
//					}
//
//					screl.getTargetNode().getNamedObject();
//				}
//			}

			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(Verbalizer.IS_SINGLE_LINE, Boolean.TRUE);
			Collection<KnowledgeSlice> rules = kb
					.getAllKnowledgeSlices();
			
			boolean appendedRulesHeadline = false;
			Map<String, String> idMap = new HashMap<String, String>();
			for (KnowledgeSlice knowledgeSlice : rules) {
				if (knowledgeSlice instanceof Rule) {
					if (!appendedRulesHeadline) {
						if (appendedSolutionsHeadline) {
							text.append("<br/>");
						}
						text.append("<strong>" + rb.getString("KnowWE.KBRenderer.rules") + ":</strong><p/>");
						appendedRulesHeadline = true;
						List<Section> allRules = new ArrayList<Section>();
						KnowWEEnvironment.getInstance().getArticleManager(web).getArticle(topic)
								.getSection().findSuccessorsOfType(Rule.class, allRules);
						KnowWEEnvironment.getInstance().getArticleManager(web).getArticle(topic)
								.getSection().findSuccessorsOfType(BulletContentType.class, allRules);
						for (Section rule:allRules) {
							String kbRuleId = (String) KnowWEUtils.getStoredObject(rule.getWeb(), topic, 
									rule.getId(), de.d3web.we.kdom.rules.Rule.KBID_KEY);
							idMap.put(kbRuleId, rule.getId());
						}
					}
					Rule rule = ((Rule) knowledgeSlice);
						
					String kdomid = idMap.get(rule.getId());
		
					if(kdomid != null) {
						String button = ("<img src=KnowWEExtension/images/page_white_find.png " 
								+ "class=\"highlight-rule\" " 
								+ "rel=\"{kdomid: '"+kdomid+"', topic: '"+topic+"', depth: 0, breadth: 0}\""
								+ "/></img>");
						text.append(button);
					}
					
					text.append("Rule: " +VerbalizationManager.getInstance().verbalize(
							rule.getCondition(), RenderingFormat.PLAIN_TEXT));
					text.append(" --> ");
					text.append(VerbalizationManager.getInstance().verbalize(
							rule.getAction(), RenderingFormat.HTML,parameterMap));
					text.append("\n <br />"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
				}
			}
			
			Collection<KnowledgeSlice> xclRels = kb
					.getAllKnowledgeSlicesFor(PSMethodXCL.class);
			boolean appendedXCLHeadline = false;
			for (KnowledgeSlice slice : xclRels) {
				if (slice instanceof de.d3web.kernel.psMethods.xclPattern.XCLModel) {
					if (!appendedXCLHeadline) {
						if (appendedSolutionsHeadline || appendedRulesHeadline) {
							text.append("<br/>");
						}
						text.append("<strong>" + rb.getString("KnowWE.KBRenderer.xclModels") + ":</strong>");
						appendedXCLHeadline = true;
					}
					de.d3web.kernel.psMethods.xclPattern.XCLModel model = ((de.d3web.kernel.psMethods.xclPattern.XCLModel) slice);
					
					//adds tresholds if different from default
					String thresholds = "";
					if (model.getEstablishedThreshold() != XCLModel.defaultEstablishedThreshold || 
							model.getSuggestedThreshold() != XCLModel.defaultSuggestedThreshold ||
							model.getMinSupport() != XCLModel.defaultMinSupport) {
						thresholds = " [" + model.getSuggestedThreshold() + ", " + model.getEstablishedThreshold() + ", " + model.getMinSupport() + "]";
						
					}
						
					
					text.append("<p /> " + model.getSolution().getText() + thresholds
							+ ": <br />");

					Map<XCLRelationType, Collection<XCLRelation>> relationMap = model
							.getTypedRelations();

					for (Entry<XCLRelationType, Collection<XCLRelation>> entry : relationMap.entrySet()) {
						XCLRelationType type = entry.getKey();
						Collection<XCLRelation> relations = entry.getValue();
						for (XCLRelation rel : relations) {
							AbstractCondition cond = rel
									.getConditionedFinding();
							String weight = "";
							String kdomid = rel.getKdmomID();
							if(type == XCLRelationType.explains) {
								weight = "["+rel.getWeight()+"]";
							}
							
							if(kdomid != null) {
								String button = ("<img src=\"KnowWEExtension/images/page_white_find.png\" " 
										+ "class=\"highlight-xcl-relation\" " 
										+ "rel=\"{kdomid: '"+kdomid+"', topic: '"+topic+"', depth: 0, breadth: 0}\"" 
										+ "/></img>");
								text.append(button);
							}
							
							text.append(type.getName()+weight+": ");
							text.append("&nbsp;&nbsp;&nbsp;"
									+ VerbalizationManager.getInstance()
											.verbalize(cond,
													RenderingFormat.PLAIN_TEXT, parameterMap));
							
							boolean id = false;
							if(id) {
								text.append(" (ID: "+rel.getId()+")");
							}
							
							
//							if(kdomid != null) {
//								String button = ("<input type='button' value='"
//										+ "XCL-Generieren"
//										+ "'"
//										+ " name='TiRexToXCL' class='button' onclick='highlightNode(\""
//										+ kdomid + "\",\""+topic+"\");'/>");
//								text += button;
//							}
							text.append(" \n <br />"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)						
							
						}
					}
				}
			}
		} else {
			text.append("<p class=\"box error\">" + rb.getString("KnowWE.KBRenderer.error") + "</p>");
		}
		return text.append("</p></div></div>").toString();
	}
}
