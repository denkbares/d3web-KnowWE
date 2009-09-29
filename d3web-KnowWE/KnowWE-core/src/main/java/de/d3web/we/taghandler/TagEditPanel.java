package de.d3web.we.taghandler;

import java.util.ArrayList;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.TaggingMangler;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TagEditPanel extends AbstractTagHandler {

	public TagEditPanel() {
		super("tageditpanel");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		TaggingMangler tm = TaggingMangler.getInstance();
		ArrayList<String> tags = tm.getPageTags(topic);
		String output = "<p>";
		output += "current tags(click to edit):";
		output += "<span id=\"tagspan\">";
		for (String cur : tags) {
			output += cur + " ";
		}
		if (tags.size()==0){
			output+="none";
		}
		output += "</span>";
		output += "<script type=\"text/javascript\">";
		output += "var myIPE=new SilverIPE('tagspan','KnowWE.jsp',{parameterName:'tagtag',highlightColor: '#ffff77',"
				+ "additionalParameters:{tagaction:\"set\",action:\"TagHandlingAction\","
				+ KnowWEAttributes.TOPIC + ":\"" + topic + "\"} });";
		output += "</script>";
		output += "</p>";
		return KnowWEEnvironment.maskHTML(output);
	}

}
