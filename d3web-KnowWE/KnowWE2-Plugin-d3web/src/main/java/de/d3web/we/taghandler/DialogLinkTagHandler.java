package de.d3web.we.taghandler;

import java.net.URLEncoder;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DialogLinkTagHandler extends AbstractTagHandler {

	public DialogLinkTagHandler() {
		super("dialogLink");
	}
	

	public String getExampleString() {
		return "[{KnowWEPlugin " + getTagName()+ " = &lt;articleName&gt;" + "}]";
	}

	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		return generateDialogLink(user.getUsername(), topic, value);
	}
	
	@Override
	public String getDescription() {
		return D3webModule.getInstance().getKwikiBundle_d3web().getString("KnowWE.DialogLink.description");
	}
	
	public static String generateDialogLink(String user, String topic, String actualID) {
		if(actualID == null || actualID.length() == 0) {
			actualID = topic+".."+KnowWEEnvironment.generateDefaultID(topic);
		}

		String prefix = KnowWEEnvironment.getInstance().getPathPrefix();
		return KnowWEEnvironment.HTML_ST
				+ "a target=kwiki-dialog href="
				+ prefix
				+ (prefix.length() != 0 ? "/" : "")
				+ "KnowWE.jsp?renderer=KWiki_dialog&KWikisessionid="
				+ URLEncoder.encode(actualID)
				+ "&KWikiWeb=default_web&KWikiUser="
				+ user
				+ ""
				+ KnowWEEnvironment.HTML_GT
				+ KnowWEEnvironment.HTML_ST
				+ "img src=KnowWEExtension/images/run.gif title=Fall im d3web-Dialog starten /"
				+ KnowWEEnvironment.HTML_GT + KnowWEEnvironment.HTML_ST + "/a"
				+ KnowWEEnvironment.HTML_GT;
	}

}
