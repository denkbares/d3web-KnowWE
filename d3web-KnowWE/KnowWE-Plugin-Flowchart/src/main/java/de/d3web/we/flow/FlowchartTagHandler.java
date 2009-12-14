package de.d3web.we.flow;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import com.ecyrd.jspwiki.diff.FlowchartDiffProvider.FlowchartChangeType;

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
import de.d3web.we.flow.diff.FlowchartEdge;
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
			String flowPreview = createPreviewWithHighlightedPath(section, theCase);
				
			//Debug
			builder.append(getPathendText(theCase));
			//
			builder.append(FlowchartUtils.createPreview(flowPreview));
			
			builder.append("<p/><p/>");
			
		}
		
		return builder.toString(); 
	}
	
	private String getPathendText(XPSCase theCase) {
		
		if (theCase == null)
			return "";
		
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
		
		
		return builder.toString(); 
	}

	private FlowSet getFlowSet(XPSCase xpsCase) {
		
		List knowledge = (List) xpsCase.getKnowledgeBase().getKnowledge(FluxSolver.class, FluxSolver.DIAFLUX);
		
		if (knowledge == null || knowledge.isEmpty())
			return null;
		
		return (FlowSet) knowledge.get(0);
	}
	

	private String createPreviewWithHighlightedPath(Section section, XPSCase xpsCase) {
		
		String preview = FlowchartUtils.extractPreview(section);
		
		if (xpsCase == null)
			return preview;
		
		String flowID = AbstractXMLObjectType.getAttributeMapFor(section).get("id");
		
		CaseObjectSource flowSet = getFlowSet(xpsCase);
		
		DiaFluxCaseObject caseObject = (DiaFluxCaseObject) xpsCase.getCaseObject(flowSet);
		
	      
       
        
		for (PathEntry entry : caseObject.getPathEnds()) {
		
			preview = highlightPath(preview, flowID, entry);
			
		}		
		
		return preview;
	}

	private String highlightPath(String preview, String flowID, PathEntry startEntry) {
		 // get all the nodes
        String[] nodes = preview.split("<DIV class=\"Node\" id=\"");
        String[] edges = preview.split("<DIV class=\"Rule\" id=\"");

		PathEntry currentEntry = startEntry;
		
		while (currentEntry != null) {

			INode node = currentEntry.getNodeData().getNode();
			
			if (!node.getFlow().getId().equals(flowID))
				return preview;
			
			String nodeId = node.getID();
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].contains(nodeId)) {
					preview = colorNode(nodes[i], preview);
				}
			}
			
			String edgeId = currentEntry.getEdge().getID();
			for (int i = 0; i < edges.length; i++) {
				if (edges[i].contains(edgeId)) {
					preview = colorEdge(edges[i], preview);
				}
			}
			
			currentEntry = currentEntry.getPath();
			
			
		}
		return preview;
	}

	private String colorNode(String node, String preview) {

     	// if yes, add the additional class
     	String inputHelper1 = preview.substring(0, preview.indexOf(node) - 6);
     	String inputHelper2 = preview.substring(preview.indexOf(node));
     	preview = inputHelper1 + " added" + "\" id=\"" + inputHelper2;
		
     	return preview;
	}

	private String colorEdge(String edge, String preview) {
	// set the additional class of the yet to be colored nodes
		String alteration = "added";
		
	    String temp = preview;
	    
		String[] parts = edge.split("<DIV class=\"");
		for (String s : parts) {
			String type = s.substring(0, s.indexOf("\""));
			if (type.equals("h_line") || type.equals("v_line") || type.contains("arrow")) {
				String inputHelper1 = temp.substring(0, temp.indexOf(s));
				String inputHelper2 = temp.substring(temp.indexOf(s));
				temp = inputHelper1 + alteration + "\" id=\"" + inputHelper2;
			} else if (type.equals("GuardPane") || type.equals("value")) {
				String inputHelper1 = temp.substring(0, temp.indexOf(s));
				String inputHelper2 = temp.substring(temp.indexOf(s));
				temp = inputHelper1 + alteration + "Text\" id=\"" + inputHelper2;
			}
		}
	        
	    return temp;
	}
	
	

}
