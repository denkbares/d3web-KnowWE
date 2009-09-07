package de.d3web.we.taghandler;

import java.util.List;
import java.util.ResourceBundle;

import org.openrdf.model.Statement;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.SemanticCore;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class PageViewHandler extends AbstractTagHandler {

    public PageViewHandler() {
	super("pageview");
    }

    @Override
    public String getDescription() {
	return KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.PageViewHandler.description");
    }

    @Override
    public String render(String topic, KnowWEUserContext user, String alue, String web) {
	SemanticCore sc = SemanticCore.getInstance();
	String output = "<tr><th>S</th><th>P</th><th>O</th></tr>";
	List<Statement> list = sc.getTopicStatements(topic);
	if (list != null) {
	    for (Statement cur : list) {
		String s = cur.getSubject().stringValue();
		String p = cur.getPredicate().stringValue();
		String o = cur.getObject().stringValue();
		s = s.substring(s.indexOf('#') + 1);
		o = o.substring(o.indexOf('#') + 1);
		p = p.substring(p.indexOf('#') + 1);
		p = p.replaceAll("type", "isA");
		output += "<tr><td>" + s + "</td><td>" + p + "</td><td>" + o
			+ "</td></tr> \n"; // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
	    }
	}
	return "<table>" + output + "</table>";
    }

}
