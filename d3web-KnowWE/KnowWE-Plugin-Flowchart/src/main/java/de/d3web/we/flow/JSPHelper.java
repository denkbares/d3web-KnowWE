package de.d3web.we.flow;

import java.util.List;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.flow.kbinfo.GetInfoObjects;
import de.d3web.we.flow.kbinfo.SearchInfoObjects;

public class JSPHelper {
	private final KnowWEParameterMap parameterMap;
	
	public JSPHelper(KnowWEParameterMap parameterMap) {
		this.parameterMap = parameterMap;
		if (this.parameterMap.getWeb() == null) {
			this.parameterMap.put(KnowWEAttributes.WEB, KnowWEEnvironment.DEFAULT_WEB);
		}
	}
	
	private List<String> getAllMatches(String className) {
		return SearchInfoObjects.matches(
			de.d3web.we.core.KnowWEEnvironment.getInstance(), 
			this.parameterMap.getWeb(), 
			null, className, 65535);
	}

	public String getArticleIDsAsArray() {
		List<String> matches = getAllMatches("Article");
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		boolean first = true;
		for (String id : matches) {
			if (first) {
				first = false;
			}
			else {
				buffer.append(", ");
			}
			buffer.append("'").append(id).append("'");
		}
		buffer.append("]");
		return buffer.toString();
	}

	public String getArticleInfoObjectsAsXML() {
		// search for matches
		List<String> matches = getAllMatches("Article");

		// fill the response buffer
		StringBuffer buffer = new StringBuffer();
		GetInfoObjects getter = new GetInfoObjects();
		getter.appendHeader(this.parameterMap, buffer);
		for (String id : matches) {
			getter.appendInfoObject(this.parameterMap, id, buffer);
		}
		getter.appendFooter(this.parameterMap, buffer);
		
		// and done
		return buffer.toString();
	}

	public String getReferredInfoObjectsAsXML() {
		// TODO: extract used object ids from flowchart as a list
		// for now we simply use all existing objects
		List<String> matches = getAllMatches(null);

		// fill the response buffer
		StringBuffer buffer = new StringBuffer();
		GetInfoObjects getter = new GetInfoObjects();
		getter.appendHeader(this.parameterMap, buffer);
		for (String id : matches) {
			getter.appendInfoObject(this.parameterMap, id, buffer);
		}
		getter.appendFooter(this.parameterMap, buffer);
		
		// and done
		return buffer.toString();
	}

	public String getKDOMNodeContent(String kdomID) {
		String articleID = this.parameterMap.getTopic();
		return KnowWEEnvironment.getInstance().getNodeData(KnowWEEnvironment.DEFAULT_WEB, articleID, kdomID);
	}
}
