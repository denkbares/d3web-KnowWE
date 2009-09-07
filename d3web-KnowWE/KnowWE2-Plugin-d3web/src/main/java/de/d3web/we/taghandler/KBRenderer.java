package de.d3web.we.taghandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLRelation;
import de.d3web.kernel.psMethods.xclPattern.XCLRelationType;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.kernel.verbalizer.Verbalizer;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KBRenderer extends AbstractTagHandler {

	public KBRenderer() {
		super("renderKnowledge");
		
	}
	
	@Override
	public String getDescription() {
		return D3webModule.getInstance().getKwikiBundle_d3web().getString("KnowWE.KBRenderer.description");
	}
	

	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		D3webKnowledgeService service = D3webModule.getInstance().getAD3webKnowledgeServiceInTopic(web, topic);		
		String text = "<div id=\"knowledge-panel\" class=\"panel\"><h3>Uebersicht Artikelwissen</h3>";
		text += "<p>";
		if (service != null) {
//			text += "<h4>Knowledge of article:</h4>";
			KnowledgeBase kb = service.getBase();
			text += "<strong>Solution(s):</strong><br />";
			List<Diagnosis> diagnosis = kb.getDiagnoses();
			
			for (Diagnosis diagnosis2 : diagnosis) {
				if (!diagnosis2.getText().equals("P000")) {
					text += VerbalizationManager.getInstance().verbalize(
							diagnosis2, RenderingFormat.HTML) + "; ";
				}
			}
			text += "<br />";

//			text += "<br /><b>SCRelations: </b><br />";
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
//						text += VerbalizationManager.getInstance().verbalize(
//								cond, RenderingFormat.HTML)
//								+ "<br>";
//					}
//
//					screl.getTargetNode().getNamedObject();
//				}
//			}
			text += "<strong>Rules: </strong>";
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(Verbalizer.IS_SINGLE_LINE, Boolean.TRUE);
			Collection<KnowledgeSlice> rules = kb
					.getAllKnowledgeSlices();
			for (KnowledgeSlice knowledgeSlice : rules) {
				if (knowledgeSlice instanceof RuleComplex) {
					RuleComplex rule = ((RuleComplex) knowledgeSlice);
					text += "Rule:" +VerbalizationManager.getInstance().verbalize(
							rule.getCondition(), RenderingFormat.PLAIN_TEXT);
					text += " --> ";
					text += VerbalizationManager.getInstance().verbalize(
							rule.getAction(), RenderingFormat.HTML,parameterMap);
					text += "\n <br />"; // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
				}else {
					text += knowledgeSlice.toString();
					text += "\n <br />"; // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
				}
			}
			text += "<br /><br />";
			text += "<strong>XCL Models:</strong>";
			Collection<KnowledgeSlice> xclRels = kb
					.getAllKnowledgeSlicesFor(PSMethodXCL.class);
			
			for (KnowledgeSlice slice : xclRels) {
				if (slice instanceof de.d3web.kernel.psMethods.xclPattern.XCLModel) {
					de.d3web.kernel.psMethods.xclPattern.XCLModel model = ((de.d3web.kernel.psMethods.xclPattern.XCLModel) slice);
					text += "<br />   " + model.getSolution().getText()
							+ ": <br />";

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
								String button = ("<img src=KnowWEExtension/images/page_white_find.png onclick='highlightNode(\""
										+ kdomid + "\",\""+topic+"\");'/></img>");
								text += button;
							}
							
							text += type.getName()+weight+": ";
							text += "&nbsp;&nbsp;&nbsp;"
									+ VerbalizationManager.getInstance()
											.verbalize(cond,
													RenderingFormat.PLAIN_TEXT, parameterMap);
							
							boolean id = false;
							if(id) {
								text += " (ID: "+rel.getId()+")";
							}
							
							
//							if(kdomid != null) {
//								String button = ("<input type='button' value='"
//										+ "XCL-Generieren"
//										+ "'"
//										+ " name='TiRexToXCL' class='button' onclick='highlightNode(\""
//										+ kdomid + "\",\""+topic+"\");'/>");
//								text += button;
//							}
							text += " \n <br />"; // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
							
							
						}
					}
				}
			}
		} else {
			text += "<p class=\"box error\">renderTag KB: Knowledge Service not found</p>";
		}
		return text + "</p></div>";
	}
}
