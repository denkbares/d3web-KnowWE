package de.d3web.we.flow;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import util.ResStream;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.CaseObjectSource;
import de.d3web.kernel.dynamicObjects.XPSCaseObject;
import de.d3web.kernel.psMethods.diaFlux.FluxSolver;
import de.d3web.kernel.psMethods.diaFlux.PathEntry;
import de.d3web.kernel.psMethods.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.kernel.psMethods.diaFlux.flow.FlowSet;
import de.d3web.kernel.psMethods.diaFlux.flow.INode;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.flow.diff.FlowchartNode;
import de.d3web.we.flow.type.FlowchartContentType;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.logging.Logging;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;


/**
 * [{KnowWEPlugin Flowchart}]
 * @author Florian Ziegler
 */
public class FlowchartTagHandler extends AbstractTagHandler {
	

	public FlowchartTagHandler() {
		super("flowchart");
		Logging.getInstance().addHandlerToLogger(
				Logging.getInstance().getLogger(), "flowchartTagHandler.txt");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		
		KnowWEArticleManager artManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = artManager.getArticle(topic);
		
		List<Section> flows = article.getSection().findChildrenOfType(FlowchartType.class);

		XPSCase theCase = D3webUtils.getXPSCase(topic, user, web);
		
		StringBuilder builder = new StringBuilder();
		
		
		for (Section section : flows) {
			
			
			String flowPreview = FlowchartUtils.extractPreview(section);

			if (theCase != null) { //without running case no highlighting
				String flowID = AbstractXMLObjectType.getAttributeMapFor(section).get("id");
				
				flowPreview = highlightNode(flowID, flowPreview, theCase);
				
				//Debug
				builder.append(getPathendText(theCase));
				//
			}
			
			
			builder.append(FlowchartUtils.createPreview(flowPreview));
			
			builder.append("<p/><p/>");
			
		}
		
		return builder.toString(); 
	}
	
	private String getPathendText(XPSCase theCase) {
		
		FlowSet set = getFlowSet(theCase);
				
		DiaFluxCaseObject caseObject = (DiaFluxCaseObject) theCase.getCaseObject(set);
		List<PathEntry> pathEnds = caseObject.getPathEnds();
				
		StringBuilder builder = new StringBuilder();
		
		builder.append("<b>Current Pathends:</b>");
		builder.append("<br/>");
		builder.append(pathEnds);
		builder.append("<br/>");
		builder.append("<br/>");
		builder.append("Pathes:");
		
		int i = 0;
		
		for (PathEntry start : pathEnds) {
			
			builder.append(++i + ". Path:");
			builder.append("<br/>");
			PathEntry entry = start;
			
			while (entry != null) {
				builder.append(entry);
				builder.append("<br/>");
				entry = entry.getPath();
				
			}
			
		}
		
		builder.append("<br/>");
		
		
//		builder.append();
		
		
		return builder.toString(); 
	}

	private FlowSet getFlowSet(XPSCase xpsCase) {
		
		List knowledge = (List) xpsCase.getKnowledgeBase().getKnowledge(FluxSolver.class, FluxSolver.DIAFLUX);
		
		if (knowledge == null || knowledge.isEmpty())
			return null;
		
		return (FlowSet) knowledge.get(0);
	}
	

	private String highlightNode(String flowID, String preview, XPSCase xpsCase) {
		
		CaseObjectSource flowSet = getFlowSet(xpsCase);
		
		DiaFluxCaseObject caseObject = (DiaFluxCaseObject) xpsCase.getCaseObject(flowSet);
		
		String result = preview;
		
	      
        // get all the nodes
        String[] nodes = preview.split("<DIV class=\"Node\" id=\"");

        
		for (PathEntry entry : caseObject.getPathEnds()) {
		
			INode node = entry.getNodeData().getNode();
			
			if (!node.getFlow().getId().equals(flowID))
				continue;
			
			String id = node.getID();
			
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].contains(id))
					result = colorNode(nodes[i], preview);
				
			}
			
		}		
		
		return result;
	}

	private String colorNode(String string, String preview) {

     	// if yes, add the additional class
     	String inputHelper1 = preview.substring(0, preview.indexOf(string) - 6);
     	String inputHelper2 = preview.substring(preview.indexOf(string));
     	preview = inputHelper1 + " added" + "\" id=\"" + inputHelper2;
		
     	return preview;
	}


}
