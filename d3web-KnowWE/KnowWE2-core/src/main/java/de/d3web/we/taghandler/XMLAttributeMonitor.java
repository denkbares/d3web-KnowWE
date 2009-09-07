package de.d3web.we.taghandler;

import java.util.List;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class XMLAttributeMonitor extends AbstractTagHandler {

	public XMLAttributeMonitor() {
		super("XMLAttributeMonitor");
		// TODO Auto-generated constructor stub
	}

	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		
		String result = "XMLAttributes:"+"<br>";
		
		List<KnowWEObjectType> types = KnowWEEnvironment.getInstance().getAllKnowWEObjectTypes();
		for (KnowWEObjectType knowWEObjectType : types) {
			if(knowWEObjectType instanceof AbstractXMLObjectType) {
//				Map<String, Map<String, Map<String, String>>> allMap = ((AbstractXMLObjectType)knowWEObjectType).getSectionMap();
//				Map<String, Map<String, String>> articleMap = allMap.get(topic);
//				if(articleMap != null) {
//					for(String m : articleMap.keySet()) {
//						Map<String,String> values = articleMap.get(m);
//						result += "<br>"+m+"<br>";
//						for(String val : values.keySet()) {
//							result += val+ " = "+ values.get(val)+"<br>";
//						}
//						
//					}
//					
//				}
				
				
			}
		}
		return result;
	}

}
