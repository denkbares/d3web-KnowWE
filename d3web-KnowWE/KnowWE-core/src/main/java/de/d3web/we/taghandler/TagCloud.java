package de.d3web.we.taghandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.TaggingMangler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TagCloud extends AbstractTagHandler {

	public TagCloud() {
		super("tagcloud");
	}

	
	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		HashMap<String,Integer> weightedlist=TaggingMangler.getInstance().getCloudList(8, 20);
		String output="<p>";
		for (Entry<String,Integer> cur:weightedlist.entrySet()){
			output+=" <a href =\"\" style=\"font-size:"+cur.getValue()+"px\">"+cur.getKey()+"</a>";		
			}
		return KnowWEEnvironment.maskHTML(output+"</p>");
	}
}
